package com.javamonitor.mbeans;

import com.javamonitor.JmxHelper;

/**
 * The tricky bits for SpringSource's DM Server OSGi-based application server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class ServerSpringDM extends ServerTomcat {
    /**
     * Test if we're maybe running inside a DM server instance.
     * 
     * @return <code>true</code> if we're inside DM server, or
     *         <code>false</code> if not.
     */
    public static boolean runningInSpringDM() {
        return JmxHelper.mbeanExists("com.springsource.server:type=Deployer");
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getName()
     */
    @Override
    public String getName() throws Exception {
        return "SpringSource dm Server";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    @Override
    public String getVersion() throws Exception {
        // The SpringSource developers seem to have forgotten to include a
        // server version mbean. Thus, we can only guess at this stage. Perhaps
        // someone can help us find the internal source file we need to get at
        // the version number?

        return "unknown";
    }
}
