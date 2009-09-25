package com.javamonitor.openfire;

import java.io.File;

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

    private Openfire openfire = null;

    private final static String OBJECTNAME_PACKET_COUNTER = NAMEBASE
            + "type=packetCounter";

    private PacketCounter packetCounter = null;

    private final static String OBJECTNAME_CORE_CLIENT_THREADPOOL = NAMEBASE
            + "type=coreThreadpool,poolname=client";

    private CoreThreadPool client = null;

    private final static String OBJECTNAME_DATABASEPOOL = NAMEBASE
            + "type=databasepool";

    private DatabasePool database = null;

    private JavaMonitorCollector collector = null;

    /**
     * @see org.jivesoftware.openfire.container.Plugin#initializePlugin(org.jivesoftware
     *      .openfire.container.PluginManager, java.io.File)
     */
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        Log.info("Start collecting data for Java-monitor.");

        try {
            openfire = new Openfire();
            openfire.start();
            JmxHelper.register(openfire, OBJECTNAME_OPENFIRE);

            Log.debug(".. started openfire server detector.");
        } catch (Exception e) {
            Log.debug("cannot start openfire server detector: "
                    + e.getMessage(), e);
        }

        try {
            packetCounter = new PacketCounter();
            packetCounter.start();
            JmxHelper.register(packetCounter, OBJECTNAME_PACKET_COUNTER);

            Log.debug(".. started stanza counter.");
        } catch (Exception e) {
            Log.debug("cannot start stanza counter: " + e.getMessage(), e);
        }

        try {
            client = new CoreThreadPool(((ConnectionManagerImpl) XMPPServer
                    .getInstance().getConnectionManager()).getSocketAcceptor());
            client.start();
            JmxHelper.register(client, OBJECTNAME_CORE_CLIENT_THREADPOOL);

            Log.debug(".. started client thread pool monitor.");
        } catch (Exception e) {
            Log.debug("cannot start client thread pool monitor: "
                    + e.getMessage(), e);
        }

        try {
            database = new DatabasePool();
            database.start();
            JmxHelper.register(database, OBJECTNAME_DATABASEPOOL);

            Log.debug(".. started database pool monitor.");
        } catch (Exception e) {
            Log.debug("cannot start database pool monitor: " + e.getMessage(),
                    e);
        }

        collector = new JavaMonitorCollector();
        try {
            collector.start();
            Log.info("Java-Monitor plugin fully initialized.");
        } catch (Exception e) {
            Log.error("cannot start java-monitor plugin: " + e.getMessage(), e);
        }
    }

    /**
     * @see org.jivesoftware.openfire.container.Plugin#destroyPlugin()
     */
    public void destroyPlugin() {
        Log.info("Stop sending data to Java-monitor.com.");
        collector.stop();

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