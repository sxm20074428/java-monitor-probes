package com.javamonitor.mbeans;

import java.util.Collection;

import javax.management.ObjectName;

import com.javamonitor.JmxHelper;

/**
 * The tricky bits for JBoss servers.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ServerJBoss implements ServerMBean {
    private static final String OBJECTNAME_JBOSS_SERVER = "jboss.system:type=Server";

    /**
     * Test if we're maybe running inside a JBoss instance.
     * 
     * @return <code>true</code> if we're inside JBoss, or <code>false</code>
     *         if not.
     */
    public static boolean runningInJBoss() {
        return JmxHelper.mbeanExists(OBJECTNAME_JBOSS_SERVER);
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getHttpPort()
     */
    public Integer getHttpPort() throws Exception {
        Collection<ObjectName> processors = null;
        int slept = 0;
        do {
            processors = JmxHelper.queryNames("jboss.web:type=ThreadPool,*");
            if (processors.size() < 1) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    // won't happen...
                }
            }
        } while (processors.size() < 1 && slept++ < 30);

        if (processors.size() < 0) {
            throw new IllegalStateException(
                    "JBoss connector MBeans were not loaded after 30 seconds, aborting");
        }

        int lowest = Integer.MAX_VALUE;
        for (final ObjectName processor : processors) {
            if (JmxHelper.queryString(processor, "name").startsWith("http")) {
                lowest = Math
                        .min(lowest, JmxHelper.queryInt(processor, "port"));
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
        return "JBoss";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    public String getVersion() throws Exception {
        return JmxHelper.queryString(OBJECTNAME_JBOSS_SERVER, "VersionNumber");
    }
}
