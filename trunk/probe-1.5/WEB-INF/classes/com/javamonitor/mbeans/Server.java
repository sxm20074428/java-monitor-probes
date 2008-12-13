package com.javamonitor.mbeans;

import com.javamonitor.JmxHelper;

/**
 * The application server helper mbean. This mbean is responsible for
 * aggregating the various server's mbeans into a single view.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Server implements ServerMBean {

    private final ServerMBean actualServer;

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
     * Create a new server info aggregator bean. Here we try to find out in what
     * server we're running and instantiate the correct server-specific MBean.
     * 
     * Note that we have to test for Tomcat after the others, because Tomcat is
     * used here and there as an embedded server. In such a case, we want to
     * detect the outer server, not the embedded Tomcat.
     */
    public Server() {
        if (ServerJBoss.runningInJBoss()) {
            actualServer = new ServerJBoss();
        } else if (ServerSpringDM.runningInSpringDM()) {
            actualServer = new ServerSpringDM();
        } else if (ServerTomcat.runningInTomcat()) {
            actualServer = new ServerTomcat();
        } else {
            actualServer = null;
        }
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getName()
     */
    public String getName() throws Exception {
        if (actualServer == null) {
            return "Java VM";
        }

        return actualServer.getName();
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    public String getVersion() throws Exception {
        if (actualServer == null) {
            return System.getProperty("java.version");
        }

        return actualServer.getVersion();
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getHttpPort()
     */
    public Integer getHttpPort() throws Exception {
        if (actualServer == null) {
            return null;
        }

        return actualServer.getHttpPort();
    }
}
