package com.javamonitor.mbeans;

/**
 * The interface to the server mbean.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public interface ServerMBean {
    /**
     * The name of the application server that we run in.
     * 
     * @return The name of the application server.
     * @throws Exception
     *             When there was a problem querying JMX.
     */
    String getName() throws Exception;

    /**
     * The version of the application server that we run in.
     * 
     * @return The version of the application server.
     * @throws Exception
     *             When there was a problem querying JMX.
     */
    String getVersion() throws Exception;

    /**
     * The lowest HTTP port in use in this application server that we run in.
     * 
     * @return The lowest HTTP port in use in this application server, or -1 if
     *         no port is known.
     * @throws Exception
     *             When there was a problem querying JMX.
     */
    Integer getHttpPort() throws Exception;
}
