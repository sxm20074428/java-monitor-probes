package com.javamonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import com.javamonitor.mbeans.Server;

/**
 * The data collector and interface to the collector server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class Collector {
    private static String account = null;

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
        init();

        final Properties request = queryItems();
        request.put("account", account);
        request.put("localIp", InetAddress.getLocalHost().getHostAddress());
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

    private static void init() throws Exception {
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
                    Server.nameAttribute)
                    + " "
                    + JmxHelper.queryString(Server.objectName,
                            Server.versionAttribute);
        }
    }

    /**
     * Push a list of items out to the server. As we go, we remove items that
     * don't resolve to a value properly.
     */
    private static Properties queryItems() {
        final Properties data = new Properties();
        final Iterator<Item> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            final Item item = itemIterator.next();

            try {
                final Object value = JmxHelper.query(item.getObjectName(), item
                        .getAttribute());
                if (value == null) {
                    data.put(item.getId(), "|0|");
                } else {
                    data.put(item.getId(), value + "|" + getClassId(value)
                            + "|");
                }

                // only push static items once per session
                if (!item.isPeriodic()) {
                    itemIterator.remove();
                }
            } catch (Exception e) {
                final StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                data.put(item.getId(), "||" + e.getClass().getName() + ": "
                        + sw.toString());
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

            for (final Object key : response.keySet()) {
                final String[] parts = ((String) response.get(key))
                        .split("\\|");

                items.add(new Item(key.toString(), parts[0], parts[1], Boolean
                        .parseBoolean(parts[2])));
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
