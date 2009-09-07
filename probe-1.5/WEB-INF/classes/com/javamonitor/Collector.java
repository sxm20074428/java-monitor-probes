package com.javamonitor;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.net.Proxy.NO_PROXY;
import static java.net.Proxy.Type.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.management.ObjectName;

import com.javamonitor.mbeans.Server;

/**
 * The data collector and interface to the collector server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class Collector {
    private static final Logger log = Logger
            .getLogger(JavaMonitorCollector.class.getName());

    private final URL url;

    private String account = null;

    private String lowestPort = null;

    private String session = null;

    private String appserver = null;

    private final Collection<Item> items = new LinkedList<Item>();

    /**
     * Create a new collector.
     * 
     * @param url
     *            The URL to push to.
     * @param uniqueId
     *            The unique ID to use instead of port number, or
     *            <code>null</code> to use the port number.
     */
    Collector(final URL url, final Integer uniqueId) {
        this.url = url;
        if (uniqueId != null) {
            lowestPort = uniqueId.toString();
        }
    }

    /**
     * Sign in with the collector server and ask for permission to send
     * statistics.
     * 
     * @return <code>true</code> if the configuration was stale and we need to
     *         reconfigure. <code>false</code> if we may just reuse the existing
     *         list.
     * @throws OnHoldException
     *             When we were put on hold by the server.
     * @throws Exception
     *             When there was a problem.
     */
    boolean push() throws Exception, OnHoldException {
        init();

        final Properties request = queryItems();
        request.put("account", account);
        request.put("localIp", getLocalIp(url));
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

    private void init() throws Exception {
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

            if (lowestPort == null) {
                lowestPort = JmxHelper.queryString(Server.objectName,
                        Server.httpPortAttribute);
            }
            appserver = JmxHelper.queryString(Server.objectName,
                    Server.nameAttribute);
        }
    }

    /**
     * Create a list of items to be pushed out to the server. As we go, we
     * remove items that don't resolve to a value properly.
     * 
     * @return The data to be sent to the server.
     */
    private Properties queryItems() {
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

    private boolean parse(final Properties response) throws OnHoldException {
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

    /**
     * We set the timeout on the connection to be a few minutes. This way we are
     * robust against network issues that cause probes to wait for data
     * indefiniedly.
     */
    private static final int TWO_MINUTES = 2 * 60 * 1000;

    private static Properties push(final URL url, final Properties request)
            throws IOException {
        HttpURLConnection connection = null;
        PrintStream out = null;
        InputStream in = null;
        try {
            connection = (HttpURLConnection) url.openConnection(findProxy());
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TWO_MINUTES);
            connection.setReadTimeout(TWO_MINUTES);
            connection.setRequestProperty("Connection", "close");

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

    private static Proxy proxy = null;

    /**
     * Locate and configure the use of an HTTP proxy, if configured. The
     * standard proxy system properties seem to get ignored by our way of
     * opening the connection, so we use the properties to explicitly create and
     * use a proxy.
     * <p>
     * I considered introducing Java-monitor-specific settings, but I like this
     * solution better. It makes the probe follow the normal way to configure
     * proxies.
     * <p>
     * See also
     * http://java.sun.com/javase/6/docs/technotes/guides/net/proxies.html.
     * 
     * @return The configured HTTP proxy, or the no-proxy placeholder if no
     *         proxy is desired.
     */
    private static Proxy findProxy() {
        if (proxy == null) {
            if (getProperty("http.proxyHost") != null) {
                proxy = new Proxy(HTTP, new InetSocketAddress(
                        getProperty("http.proxyHost"), parseInt(getProperty(
                                "http.proxyPort", "80"))));
                log.info("using proxy " + proxy);
            } else {
                proxy = NO_PROXY;
            }
        }
        return proxy;
    }
}
