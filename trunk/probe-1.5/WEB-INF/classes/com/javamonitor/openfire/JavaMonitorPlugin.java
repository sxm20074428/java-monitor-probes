package com.javamonitor.openfire;

import java.io.File;

import org.jivesoftware.openfire.ServerPort;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.spi.ConnectionManagerImpl;
import org.jivesoftware.util.Log;

import com.javamonitor.JavaMonitorCollector;
import com.javamonitor.JmxHelper;
import com.javamonitor.openfire.mbeans.CoreThreadPool;
import com.javamonitor.openfire.mbeans.DatabasePool;
import com.javamonitor.openfire.mbeans.Openfire;
import com.javamonitor.openfire.mbeans.PacketCounter;

/**
 * This plugin provides data for the monitoring service that's provided by
 * http://www.java-monitor.com
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class JavaMonitorPlugin implements Plugin {
    /**
     * The base name for all mbeans we register from the plugin.
     */
    private static final String NAMEBASE = "com.javamonitor.openfire.plugin:";

    /**
     * The JMX ObjectName for the Openfire server mbean.
     */
    public final static String OBJECTNAME_OPENFIRE = NAMEBASE + "type=Openfire";

    private final static String OBJECTNAME_PACKET_COUNTER = NAMEBASE
            + "type=packetCounter";

    private final static String OBJECTNAME_DATABASEPOOL = NAMEBASE
            + "type=databasepool";

    private final static String OBJECTNAME_CORE_CLIENT_THREADPOOL = NAMEBASE
            + "type=coreThreadpool,poolname=client";

    private final Openfire openfire = new Openfire();

    private final PacketCounter packetCounter = new PacketCounter();

    private final DatabasePool database = new DatabasePool();

    private final CoreThreadPool client = new CoreThreadPool(
            ((ConnectionManagerImpl) XMPPServer.getInstance()
                    .getConnectionManager()).getSocketAcceptor());

    private JavaMonitorCollector collector = null;

    /**
     * @see org.jivesoftware.openfire.container.Plugin#initializePlugin(org.jivesoftware
     *      .openfire.container.PluginManager, java.io.File)
     */
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        Log.info("Start collecting data");

        Log.debug("... starting openfire ...");
        openfire.start();
        JmxHelper.register(openfire, OBJECTNAME_OPENFIRE);

        Log.debug("... starting stanza counter ...");
        packetCounter.start();
        JmxHelper.register(packetCounter, OBJECTNAME_PACKET_COUNTER);

        Log.debug("... starting core client threadpool stats collector ...");
        client.start();
        JmxHelper.register(client, OBJECTNAME_CORE_CLIENT_THREADPOOL);

        Log.debug("... exposing database-pool stats ...");
        database.start();
        JmxHelper.register(database, OBJECTNAME_DATABASEPOOL);

        Log
                .debug("Activate the java-monitor collector that forwards data to that service.");
        // use the lowest server port to uniquely identify this instance on this
        // host. Prefer client ports.
        int lowestClientPort = Integer.MAX_VALUE;
        int lowestPort = Integer.MAX_VALUE;
        for (final ServerPort port : XMPPServer.getInstance().getServerInfo()
                .getServerPorts()) {
            final int number = port.getPort();

            if (number < lowestPort) {
                lowestPort = number;
            }

            if (ServerPort.Type.client.equals(port.getType())
                    && number < lowestClientPort) {
                lowestClientPort = number;
            }
        }

        if (lowestClientPort != Integer.MAX_VALUE) {
            collector = new JavaMonitorCollector(lowestClientPort);
        } else if (lowestPort != Integer.MAX_VALUE) {
            collector = new JavaMonitorCollector(lowestPort);
        } else {
            collector = new JavaMonitorCollector();
        }

        collector.start();
        Log.info("Java-Monitor plugin fully initialized.");
    }

    /**
     * @see org.jivesoftware.openfire.container.Plugin#destroyPlugin()
     */
    public void destroyPlugin() {
        Log.info("Stop sending data to Java-monitor.com.");
        collector.stop();

        Log.info("Stop collecting data.");
        database.stop();
        JmxHelper.unregister(OBJECTNAME_DATABASEPOOL);

        client.stop();
        JmxHelper.unregister(OBJECTNAME_CORE_CLIENT_THREADPOOL);

        packetCounter.stop();
        JmxHelper.unregister(OBJECTNAME_PACKET_COUNTER);

        openfire.stop();
        JmxHelper.unregister(OBJECTNAME_OPENFIRE);

        Log.info("Java-Monitor plugin fully destroyed.");
    }
}