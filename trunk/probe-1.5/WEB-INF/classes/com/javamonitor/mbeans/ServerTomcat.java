package com.javamonitor.mbeans;

import static com.javamonitor.JmxHelper.mbeanExists;
import static com.javamonitor.JmxHelper.queryInt;
import static com.javamonitor.JmxHelper.queryNames;
import static com.javamonitor.JmxHelper.queryString;

import java.util.Collection;

import javax.management.AttributeNotFoundException;
import javax.management.ObjectName;

/**
 * The tricky bits for Tomcat servers.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
class ServerTomcat implements ServerMBean {
    private static final String ATTRIBUTE_TOMCAT_SERVERINFO = "serverInfo";

    private static final String OBJECTNAME_TOMCAT_SERVER = "Catalina:type=Server";

    /**
     * Test if we're maybe running inside a Tomcat instance.
     * 
     * @return <code>true</code> if we're inside Tomcat, or <code>false</code>
     *         if not.
     */
    public static boolean runningInTomcat() {
        return mbeanExists(OBJECTNAME_TOMCAT_SERVER);
    }

    /**
     * Get the lowest tomcat HTTP port. Since we may be started pretty early in
     * Tomcat's startup cycle, the HTTP handlers may not have been registered
     * yet. So, we sleep until they start.
     * 
     * @see com.javamonitor.mbeans.ServerMBean#getHttpPort()
     */
    public Integer getHttpPort() throws Exception {
        Collection<ObjectName> connectors = null;
        int slept = 0;
        do {
            connectors = queryNames("*:type=Connector,*");
            if (connectors.size() < 1) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    // won't happen...
                }
            }
        } while (connectors.size() < 1 && slept++ < 30);

        if (connectors.size() < 0) {
            throw new IllegalStateException(
                    getName()
                            + " connector MBeans were not loaded after 30 seconds, aborting");
        }

        int lowest = Integer.MAX_VALUE;
        for (final ObjectName connector : connectors) {
            try {
                final String protocol = queryString(connector, "protocol");
                if (protocol.startsWith("HTTP")) {
                    lowest = Math.min(lowest, queryInt(connector, "port"));
                }
            } catch (AttributeNotFoundException e) {
                // quietly skip this connector, it's probably the wrong kind
            }
        }

        // maybe there are no HTTP connectors?
        if (lowest == Integer.MAX_VALUE) {
            for (final ObjectName connector : connectors) {
                try {
                    lowest = Math.min(lowest, queryInt(connector, "port"));
                } catch (AttributeNotFoundException e) {
                    // quietly skip this connector, it's probably the wrong kind
                }
            }
        }

        if (lowest == Integer.MAX_VALUE) {
            return null;
        }

        return lowest;
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getName()
     */
    public String getName() throws Exception {
        try {
            return queryString(OBJECTNAME_TOMCAT_SERVER,
                    ATTRIBUTE_TOMCAT_SERVERINFO).replaceAll("/.*", "");
        } catch (AttributeNotFoundException e) {
            // hmm, it's an old version of Tomcat
        }

        return "Apache Tomcat";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    public String getVersion() throws Exception {
        try {
            return queryString(OBJECTNAME_TOMCAT_SERVER,
                    ATTRIBUTE_TOMCAT_SERVERINFO).replaceAll(".*/", "");
        } catch (AttributeNotFoundException e) {
            // hmm, it's an old version of Tomcat
        }

        return "older than 5.5.16";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getLastException()
     */
    public Throwable getLastException() {
        // not used, the Server class resolves this for us
        throw new Error("Not implemented...");
    }
}
