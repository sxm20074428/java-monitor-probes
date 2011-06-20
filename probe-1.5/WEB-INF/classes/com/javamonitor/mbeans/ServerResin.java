package com.javamonitor.mbeans;

import static com.javamonitor.JmxHelper.mbeanExists;
import static com.javamonitor.JmxHelper.queryInt;
import static com.javamonitor.JmxHelper.queryNames;
import static com.javamonitor.JmxHelper.queryString;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;
import static java.util.regex.Pattern.compile;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ObjectName;

/**
 * The tricky bits for Resin servers.
 * 
 * @author Guus der Kinderen &lt;guus.der.kinderen@gmail.com&gt;
 */
final class ServerResin implements ServerMBean {
    private static final String OBJECTNAME_RESIN_SERVER = "resin:type=Resin";
    private static final Pattern PATTERN_VERSION = compile("\\d+\\.\\d+\\.\\d+");

    /**
     * Test if we're maybe running inside a Resin instance.
     * 
     * @return <code>true</code> if we're inside Resin, or <code>false</code> if
     *         not.
     */
    public static boolean runningInResin() {
        return mbeanExists(OBJECTNAME_RESIN_SERVER);
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getName()
     */
    public String getName() throws Exception {
        return "Resin";
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getVersion()
     */
    public String getVersion() throws Exception {
        final String version = queryString(OBJECTNAME_RESIN_SERVER, "Version");
        if (version == null) {
            return null;
        }

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
            selectors = queryNames("resin:type=Port,*");
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

        int lowestHttp = MAX_VALUE;
        int lowestOther = MAX_VALUE;

        for (final ObjectName selector : selectors) {
            final Integer port = queryInt(selector, "Port");
            if (port == null) {
                continue;
            }

            final String protocol = queryString(selector, "ProtocolName");
            if (protocol == null || !"http".equalsIgnoreCase(protocol.trim())) {
                lowestOther = min(lowestOther, port.intValue());
            } else {
                lowestHttp = min(lowestHttp, port.intValue());
            }
        }

        // maybe there are no HTTP connectors?
        if (lowestHttp != MAX_VALUE) {
            return lowestHttp;
        } else if (lowestOther != MAX_VALUE) {
            return lowestOther;
        } else {
            return null;
        }
    }

    /**
     * @see com.javamonitor.mbeans.ServerMBean#getLastException()
     */
    public Throwable getLastException() {
        // not used, the Server class resolves this for us
        throw new Error("Not implemented...");
    }
}
