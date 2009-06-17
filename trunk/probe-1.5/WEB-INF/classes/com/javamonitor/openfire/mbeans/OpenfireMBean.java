package com.javamonitor.openfire.mbeans;

/**
 * The MBean interface to the Openfire MBean.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public interface OpenfireMBean {
    /**
     * Find the version of this XMPP server.
     * 
     * @return The version number of this Openfire server.
     */
    String getVersion();

    /**
     * Find the lowest port number for this Openfire server. We prefer to use a
     * client port, but we will happily use any other port of no client XMPP
     * port can be found.
     * 
     * @return The lowest port for this Openfire server.
     */
    Integer getLowestPort();
}
