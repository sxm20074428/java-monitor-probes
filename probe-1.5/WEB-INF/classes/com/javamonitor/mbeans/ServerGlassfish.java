package com.javamonitor.mbeans;

import java.util.Collection;

import javax.management.ObjectName;

import com.javamonitor.JmxHelper;

/**
 * The tricky bits for Glassfish servers.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class ServerGlassfish implements ServerMBean {
    private static final String OBJECTNAME_GLASSFISH_SERVER = "com.sun.appserv:j2eeType=J2EEServer,name=server,category=runtime";

    /**
     * Test if we're maybe running inside a Glassfish instance.
     * 
     * @return <code>true</code> if we're inside Glassfish, or
     *         <code>false</code> if not.
     */
    public static boolean runningInGlassfish() {
        return JmxHelper.mbeanExists(OBJECTNAME_GLASSFISH_SERVER);
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
        return JmxHelper.queryString(OBJECTNAME_GLASSFISH_SERVER,
                "serverVersion").replaceFirst("^[^0-9]*", "").trim();
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getHttpPort()
     */
    public Integer getHttpPort() throws Exception {
        Collection<ObjectName> selectors = null;
        int slept = 0;
        do {
            selectors = JmxHelper.queryNames("com.sun.appserv:type=Selector,*");
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
        for (final ObjectName processor : selectors) {
            final String name = processor.toString();
            if (name.contains("http")) {
                lowest = Math.min(lowest, Integer.parseInt(name.replaceAll(
                        "[^0-9]", "")));
            }
        }

        // maybe there are no HTTP connectors?
        if (lowest == Integer.MAX_VALUE) {
            for (final ObjectName processor : selectors) {
                final String name = processor.toString();
                lowest = Math.min(lowest, Integer.parseInt(name.replaceAll(
                        "[0-9]", "")));
            }
        }

        if (lowest == Integer.MAX_VALUE) {
            return null;
        }

        return lowest;
    }
}
