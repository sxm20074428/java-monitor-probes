package com.javamonitor.mbeans;

import com.javamonitor.JmxHelper;

/**
 * The tricky bits for JBoss servers.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class ServerJBoss extends ServerTomcat {
    private static final String OBJECTNAME_JBOSS_SERVER = "jboss.system:type=Server";

    /**
     * Test if we're maybe running inside a JBoss instance.
     * 
     * @return <code>true</code> if we're inside JBoss, or <code>false</code> if
     *         not.
     */
    public static boolean runningInJBoss() {
        return JmxHelper.mbeanExists(OBJECTNAME_JBOSS_SERVER);
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getName()
     */
    @Override
    public String getName() throws Exception {
        return "JBoss";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    @Override
    public String getVersion() throws Exception {
        return JmxHelper.queryString(OBJECTNAME_JBOSS_SERVER, "Version");
    }
}
