package com.javamonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.management.ObjectName;

import com.javamonitor.mbeans.Server;

/**
 * The data collector and interface to the collector server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class Collector {
    private static String account = null;

    private static String localIp = null;

    private static String lowestPort = null;

    private static String session = null;

    private static String appserver = null;

    private static final Collection<Item> items = new LinkedList<Item>();

    /**
     * Sign in with the collector server and ask for permission to send
     * statistics.
     * 
     * @param url
     *            The URL to push to.
     * @return <code>true</code> if the configuration was stale and we need to
     *         reconfigure. <code>false</code> if we may just reuse the
     *         existing list.
     * @throws IOException
     *             When there was a problem accessing the environment.
     * @throws OnHoldException
     *             When we were put on hold by the server.
     * @throws Exception
     *             When there was a problem.
     */
    public static boolean push(final URL url) throws Exception, OnHoldException {
        try {
            init(url);

            final Properties request = queryItems();
            request.put("account", account);
            request.put("localIp", localIp);
            if (lowestPort != null) {
                request.put("lowestPort", lowestPort);
            }
            if (appserver != null) {
                request.put("appserver", appserver);
            }
            if (session != null) {
                request.put(SESSION, session);
            }

            final Properties response = push(url, request);

            return parse(response);
        } catch (OnHoldException e) {
            session = null;
            throw e;
        } catch (Exception e) {
            session = null;
            throw e;
        }
    }

    /**
     * Retrieve the local IP address. If we just used InetAddress.getLocalhost()
     * we end up with 127.0.0.1 in many cases, so instead we open a connection
     * to the Java-monitor servers and use the local IP address of that
     * connection.
     * <p>
     * We do not reuse the connection, because the JVM may be running on a
     * laptop that moves from network to network.
     * 
     * @param url
     *            The url to base the test off of.
     * @return The local IP address of this JVM.
     * @throws UnknownHostException
     *             When we could not determine the local IP address.
     * @throws IOException
     *             When we could not determine the local IP address.
     */
    private static String getLocalIp(final URL url)
            throws UnknownHostException, IOException {
        Socket s = null;
        try {
            s = new Socket(url.getHost(), 80);
            return s.getLocalAddress().getHostAddress();
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    // ignore errors here...
                }
            }
        }
    }

    private static void init(final URL url) throws Exception {
        if (account == null) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(Collector.class
                        .getClassLoader().getResourceAsStream("uuid")));
                account = in.readLine();
            } finally {
                if (in != null) {
                    in.close();
                }
            }

            lowestPort = JmxHelper.queryString(Server.objectName,
                    Server.httpPortAttribute);
            appserver = JmxHelper.queryString(Server.objectName,
                    Server.nameAttribute);
        }

        if (localIp == null) {
            localIp = getLocalIp(url);
        }
    }

    /**
     * Create a list of items to be pushed out to the server. As we go, we
     * remove items that don't resolve to a value properly.
     * 
     * @return The data to be sent to the server.
     */
    private static Properties queryItems() {
        final Properties data = new Properties();
        final Iterator<Item> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            final Item item = itemIterator.next();

            try {
                int uniquefier = 0;
                for (final ObjectName objectName : JmxHelper.queryNames(item
                        .getObjectName())) {
                    final String key = item.getId()
                            + (uniquefier > 0 ? ":" + uniquefier : "");
                    final String actualObjectName = item.getObjectName()
                            .equals(objectName.toString()) ? "" : objectName
                            .toString();
                    final Object value = JmxHelper.query(objectName, item
                            .getAttribute());

                    if (value == null) {
                        data.put(key, "|0||" + actualObjectName);
                    } else {
                        data.put(key, value + "|" + getClassId(value) + "||"
                                + actualObjectName);
                    }

                    uniquefier++;
                }

                // only push static items once per session
                if (!item.isPeriodic()) {
                    itemIterator.remove();
                }
            } catch (Exception e) {
                final StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                data.put(item.getId(), "||" + e.getClass().getName() + ": "
                        + sw.toString() + "|");
                itemIterator.remove();
            }
        }

        return data;
    }

    private static int getClassId(final Object value) {
        if (value instanceof Byte || value instanceof Short
                || value instanceof Integer || value instanceof Long) {
            return 1;
        }
        if (value instanceof Float || value instanceof Double) {
            return 2;
        }
        if (value instanceof Boolean) {
            return 3;
        }
        return 4; // String
    }

    private static final String ONHOLD = "onhold";

    private static final String SESSION = "session";

    private static boolean parse(final Properties response)
            throws OnHoldException {
        if (response.get(ONHOLD) != null) {
            throw new OnHoldException((String) response.get(ONHOLD));
        }

        if (response.get(SESSION) != null) {
            session = (String) response.remove(SESSION);

            for (final Map.Entry<Object, Object> entry : response.entrySet()) {
                final String[] parts = ((String) entry.getValue()).split("\\|");

                items.add(new Item(entry.getKey().toString(), parts[0],
                        parts[1], Boolean.parseBoolean(parts[2])));
            }

            return true;
        }

        return false;
    }

    private static Properties push(final URL url, final Properties request)
            throws IOException {
        HttpURLConnection connection = null;
        PrintStream out = null;
        InputStream in = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            out = new PrintStream(connection.getOutputStream());
            request.storeToXML(out, null);
            out.flush();

            in = connection.getInputStream();
            final Properties response = new Properties();
            response.loadFromXML(in);

            return response;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // ignore...
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore...
                }
            }

            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    // ignore...
                }
            }
        }
    }
}
