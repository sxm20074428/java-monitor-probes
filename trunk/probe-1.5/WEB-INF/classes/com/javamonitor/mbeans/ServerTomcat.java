package com.javamonitor.mbeans;

import java.util.Collection;

import javax.management.ObjectName;

import com.javamonitor.JmxHelper;

/**
 * The tricky bits for Tomcat servers.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class ServerTomcat implements ServerMBean {
    private static final String ATTRIBUTE_TOMCAT_SERVERINFO = "serverInfo";

    private static final String OBJECTNAME_TOMCAT_SERVER = "Catalina:type=Server";

    /**
     * Test if we're maybe running inside a Tomcat instance.
     * 
     * @return <code>true</code> if we're inside Tomcat, or <code>false</code>
     *         if not.
     */
    public static boolean runningInTomcat() {
        return JmxHelper.mbeanExists(OBJECTNAME_TOMCAT_SERVER);
    }

    /**
     * Get the lowest tomcat HTTP port. Since we may be started pretty early in
     * Tomcat's startup cycle, the HTTP handlers may not have been registered
     * yet. So, we sleep until they start.
     * 
     * @see com.javamonitor.mbeans.ServerMBean#getHttpPort()
     */
    public Integer getHttpPort() throws Exception {
        Collection<ObjectName> processors = null;
        do {
            processors = JmxHelper.queryNames("Catalina:type=Connector,*");
            if (processors.size() < 1) {
                try {
                    Thread.sleep(1000L);
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

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getName()
     */
    public String getName() throws Exception {
        return JmxHelper.queryString(OBJECTNAME_TOMCAT_SERVER,
                ATTRIBUTE_TOMCAT_SERVERINFO).replaceAll("/.*", "");
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    public String getVersion() throws Exception {
        return JmxHelper.queryString(OBJECTNAME_TOMCAT_SERVER,
                ATTRIBUTE_TOMCAT_SERVERINFO).replaceAll(".*/", "");
    }
}
