package com.javamonitor;

import static com.javamonitor.JmxHelper.registerCoolMBeans;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.javamonitor.mbeans.Server;

/**
 * The Java-monitor collector class.
 * 
 * @author Barry van Someren
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class JavaMonitorCollector {
    private static final Logger log = Logger
            .getLogger(JavaMonitorCollector.class.getName());

    private Thread collectorThread = null;

    private Collector collector = null;

    private boolean started = false;

    private static final long MINUTES = 60L * 1000L;

    private static final String JAVA_MONITOR_URL = "javamonitor.url";

    private static final String JAVA_MONITOR_ID = "javamonitor.uniqueid";

    private static final Server server = new Server();

    /**
     * Create a new Java-monitor collector, which requires the URL to be
     * specified using the system property &quot;javamonitor.url&quot;.
     * <p>
     * If specified, it will use the value from system property
     * &quot;javamonitor.uniqueid&quot; as the unique ID for this application.
     * Failing that, it will use the MBeans to find the lowest port number, if
     * applicable.
     */
    public JavaMonitorCollector() {
        this(null);
    }

    /**
     * Create a new Java-monitor collector, specifying a unique ID for this
     * application.
     * <p>
     * The collector URL may be overridden from the command line using the
     * system property &quot;javamonitor.url&quot;.
     * <p>
     * The unique ID may be overridden from the command line using the system
     * property &quot;javamonitor.uniqueid&quot;.
     * 
     * @param uniqueId
     *            The unique ID to use for this application, in case system
     *            property &quot;javamonitor.uniqueid&quot; is not set.
     */
    public JavaMonitorCollector(final String uniqueId) {
        String id = uniqueId;
        if (System.getProperty(JAVA_MONITOR_ID) != null) {
            id = System.getProperty(JAVA_MONITOR_ID);
        }
        if (id == null) {
            id = checkForEatJId();
        }

        final String urlString = System.getProperty(JAVA_MONITOR_URL,
                "http://194.109.206.51/lemongrass/1.0/push");

        try {
            collector = new Collector(new URL(urlString), id);
            collectorThread = new Thread(new CollectorDriver(),
                    "java-monitor collector");
        } catch (MalformedURLException e) {
            collector = null;
            collectorThread = null;
            log.log(Level.SEVERE, "unable to parse '" + urlString
                    + "' into a URL: " + e.getMessage(), e);
        }
    }

    /**
     * We have some specific code for eatj.com's hosting service, because they
     * use port dynamically. This causes eatj customers to see a new host after
     * every restart. We check here to see if we are running at eatj. If so, we
     * find the eatj user ID to use as lowest 'port'.
     * 
     * @return The eatj user id, or <code>null</code> if we are not running on
     *         eatj hosts.
     */
    private String checkForEatJId() {
        final String hostname = System.getenv("HOSTNAME");
        if (hostname == null) {
            return null;
        }

        if (!hostname.toLowerCase().endsWith(".eatj.com")) {
            return null;
        }

        return System.getenv("USER") + " (eatj)";
    }

    /**
     * Start the collector, if it was not already started.
     * 
     * @throws Exception
     *             When the helper MBeans could not be registered.
     */
    public synchronized void start() throws Exception {
        if (!started && collectorThread != null) {
            registerCoolMBeans(server);

            collectorThread.start();
            started = true;
        }
    }

    /**
     * Stop the collector, if it was running.
     */
    public synchronized void stop() {
        if (started && collectorThread != null) {
            collectorThread.interrupt();
            try {
                collectorThread.join();
            } catch (InterruptedException e) {
                // ignore, we're going down anyway
            }

            JmxHelper.unregisterCoolMBeans();
            started = false;
        }
    }

    private final class CollectorDriver implements Runnable {
        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                // give the container around us a little time to start up and
                // (more importantly) register its mbeans.
                Thread.sleep(2000L);

                for (;;) {
                    try {
                        for (;;) {
                            if (collector.push()) {
                                collector.push();
                            }
                            server.setLastException(null);

                            Thread.sleep(1L * MINUTES);
                        }
                    } catch (InterruptedException e) {
                        throw e; // it ends up in the outer try block
                    } catch (OnHoldException e) {
                        throw e; // it ends up in the outer try block
                    } catch (Throwable e) {
                        if (server.getLastException() == null) {
                            server.setLastException(e);
                            log.log(Level.SEVERE,
                                    "This probe was hit by an unexpected exception: "
                                            + e.getMessage(), e);
                        }

                        Thread.sleep(1L * MINUTES);
                    }
                }
            } catch (InterruptedException e) {
                // ignore. we're exiting
            } catch (OnHoldException e) {
                log.log(Level.SEVERE,
                        "This probe was put on hold by the collector (redeploy to try again): "
                                + e.getOnHoldBecause());
            }
        }
    }
}
