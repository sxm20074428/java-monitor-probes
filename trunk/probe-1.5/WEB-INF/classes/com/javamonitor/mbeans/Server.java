package com.javamonitor.mbeans;

import java.util.Collection;

import javax.management.ObjectName;

import com.javamonitor.JmxHelper;

/**
 * The application server helper mbean. This mbean is responsible for
 * aggregating the various server's mbeans into a single view.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Server implements ServerMBean {
    private static final String ATTRIBUTE_TOMCAT_SERVERINFO = "serverInfo";

    private static final String OBJECTNAME_TOMCAT_SERVER = "Catalina:type=Server";

    private final ServerType serverType;

    /**
     * The object name for the application server helper mbean.
     */
    public static final String objectName = JmxHelper.objectNameBase + "Server";

    /**
     * The attribute name of the lowest HTTP port attribute.
     */
    public static final String httpPortAttribute = "HttpPort";

    /**
     * The attribute name of the application server name attribute.
     */
    public static final String nameAttribute = "Name";

    /**
     * The attribute name of the application server version attribute.
     */
    public static final String versionAttribute = "Version";

    /**
     * Create a new server info aggregator bean.
     */
    public Server() {
        if (JmxHelper.mbeanExists(OBJECTNAME_TOMCAT_SERVER)) {
            serverType = ServerType.TOMCAT;
        } else {
            serverType = ServerType.UNKNOWN;
        }
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getName()
     */
    public String getName() throws Exception {
        switch (serverType) {
        case TOMCAT:
            return JmxHelper.queryString(OBJECTNAME_TOMCAT_SERVER,
                    ATTRIBUTE_TOMCAT_SERVERINFO).replaceAll("/.*", "");
        case UNKNOWN:
            // fall through...
        }
        return "unknown";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    public String getVersion() throws Exception {
        switch (serverType) {
        case TOMCAT:
            return JmxHelper.queryString(OBJECTNAME_TOMCAT_SERVER,
                    ATTRIBUTE_TOMCAT_SERVERINFO).replaceAll(".*/", "");
        case UNKNOWN:
            // fall through...
        }
        return "unknown";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getHttpPort()
     */
    public Integer getHttpPort() throws Exception {
        switch (serverType) {
        case TOMCAT:
            return lowestTomcatPort();
        case UNKNOWN:
            // fall through...
        }
        return null;
    }

    /**
     * Get the lowest tomcat HTTP port. Since we may be started pretty early in
     * Tomcat's startup cycle, the HTTP handlers may not have been registered
     * yet. So, we sleep until they start.
     * 
     * @return The lowest tomcat http port.
     * @throws Exception
     *             When there was a problem querying JMX.
     */
    private Integer lowestTomcatPort() throws Exception {
        Collection<ObjectName> processors = null;
        do {
            processors = JmxHelper.queryNames("Catalina:type=Connector,*");
            if (processors.size() < 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // won't happen...
                }
            }
        } while (processors.size() < 1);

        int lowest = Integer.MAX_VALUE;
        for (final ObjectName processor : processors) {
            lowest = Math.min(lowest, JmxHelper.queryInt(processor, "port"));
        }

        if (lowest == Integer.MAX_VALUE) {
            return null;
        }

        return lowest;
    }
}
