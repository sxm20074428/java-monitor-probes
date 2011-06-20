package com.javamonitor.mbeans;

import static com.javamonitor.JmxHelper.mbeanExists;
import static com.javamonitor.JmxHelper.queryNames;
import static com.javamonitor.JmxHelper.queryString;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static java.lang.Thread.sleep;
import static java.util.regex.Pattern.compile;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ObjectName;

/**
 * The tricky bits for Glassfish servers.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class ServerGlassfish implements ServerMBean {
    private static final String OBJECTNAME_GLASSFISH_SERVER_AMX = "amx:pp=,type=domain-root";
    private static final String OBJECTNAME_GLASSFISH_SERVER_JMX = "com.sun.appserv:j2eeType=J2EEServer,category=runtime,*";
    private static final Pattern PATTERN_VERSION = compile("\\d+\\.\\d+(\\.\\d+)*");

    /**
     * Test if we're maybe running inside a Glassfish instance.
     * 
     * @return <code>true</code> if we're inside Glassfish, or
     *         <code>false</code> if not.
     */
    public static boolean runningInGlassfish() {
        try {
            // Glassfish 3.x
            if (mbeanExists(OBJECTNAME_GLASSFISH_SERVER_AMX)) {
                return true;
            }

            // Glassfish 2.x
            final Set<ObjectName> serverNames = queryNames(OBJECTNAME_GLASSFISH_SERVER_JMX);
            return !serverNames.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getName()
     */
    public String getName() throws Exception {
        return "Glassfish";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    public String getVersion() throws Exception {
        final String versionAMX = getVersion(OBJECTNAME_GLASSFISH_SERVER_AMX,
                "ApplicationServerFullVersion");
        if (versionAMX != null) {
            return versionAMX;
        }

        return getVersion(OBJECTNAME_GLASSFISH_SERVER_JMX, "serverVersion");
    }

    private String getVersion(String query, String attribute) throws Exception {
        final Set<ObjectName> serverNames = queryNames(query);
        if (serverNames.isEmpty()) {
            return null;
        }

        final String version = queryString(serverNames.iterator().next(),
                attribute);

        final Matcher matcher = PATTERN_VERSION.matcher(version);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getHttpPort()
     */
    public Integer getHttpPort() throws Exception {
        Collection<ObjectName> selectors = null;
        int slept = 0;
        do {
            selectors = queryNames("com.sun.appserv:type=Selector,*");
            if (selectors.size() < 1) {
                try {
                    sleep(1000L);
                } catch (InterruptedException e) {
                    // won't happen...
                }
            }
        } while (selectors.size() < 1 && slept++ < 30);

        if (selectors.size() < 0) {
            throw new IllegalStateException(
                    getName()
                            + " selector MBeans were not loaded after 30 seconds, aborting");
        }

        int lowest = MAX_VALUE;
        for (final ObjectName selector : selectors) {
            final String name = selector.toString();
            if (name.contains("http")) {
                lowest = min(lowest, parseInt(name.replaceAll("[^0-9]", "")));
            }
        }

        // maybe there are no HTTP connectors?
        if (lowest == MAX_VALUE) {
            for (final ObjectName selector : selectors) {
                final String name = selector.toString();
                lowest = min(lowest, parseInt(name.replaceAll("[0-9]", "")));
            }
        }

        if (lowest == MAX_VALUE) {
            return null;
        }

        return lowest;
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getLastException()
     */
    public Throwable getLastException() {
        // not used, the Server class resolves this for us
        throw new Error("Not implemented...");
    }
}
