package com.javamonitor.mbeans;

import java.util.Collection;

import javax.management.ObjectName;

import com.javamonitor.JmxHelper;

/**
 * The tricky bits for Jetty servers.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class ServerJetty implements ServerMBean {
    private static final String OBJECTNAME_JETTY_SERVER = "org.mortbay.jetty:type=server,id=0";

    /**
     * Test if we're maybe running inside a Glassfish instance.
     * 
     * @return <code>true</code> if we're inside Glassfish, or
     *         <code>false</code> if not.
     */
    public static boolean runningInJetty() {
        return JmxHelper.mbeanExists(OBJECTNAME_JETTY_SERVER);
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getName()
     */
    public String getName() throws Exception {
        return "Jetty";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    public String getVersion() throws Exception {
        return JmxHelper.queryString(OBJECTNAME_JETTY_SERVER, "version");
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getHttpPort()
     */
    public Integer getHttpPort() throws Exception {
        // XXX for Jetty 7, see http://jira.codehaus.org/browse/JETTY-976
        // System.err.println(((ObjectName[])JmxHelper.query(OBJECTNAME_JETTY_SERVER,
        // "connectors"))[0]);

        Collection<ObjectName> selectors = null;
        int slept = 0;
        do {
            selectors = JmxHelper
                    .queryNames("org.mortbay.jetty.nio:type=selectchannelconnector,*");
            if (selectors.size() < 1) {
                try {
                    Thread.sleep(1000L);
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

        int lowest = Integer.MAX_VALUE;
        for (final ObjectName selector : selectors) {
            lowest = JmxHelper.queryInt(selector, "port");
        }

        if (lowest == Integer.MAX_VALUE) {
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
