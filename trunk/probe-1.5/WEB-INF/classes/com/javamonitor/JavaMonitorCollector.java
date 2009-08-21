package com.javamonitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.javamonitor.mbeans.Server;

/**
 * The Java-monitor collector class.
 * 
 * @author goltharnl
 */
public class JavaMonitorCollector {
    private static final Logger log = Logger
            .getLogger(JavaMonitorCollector.class.getName());

    private final Thread collectorThread;

    private final Collector collector;

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
     * &quot;javamonitor.uniqueid&quot; as the unique ID for this applicaiton.
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
    public JavaMonitorCollector(final Integer uniqueId) {
        final String urlString = System.getProperty(JAVA_MONITOR_URL,
                "http://194.109.206.50/lemongrass/1.0/push");
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "unable to parse '" + urlString
                    + "' into a URL: " + e.getMessage(), e);
        }

        Integer id = uniqueId;
        if (System.getProperty(JAVA_MONITOR_ID) != null) {
            try {
                id = Integer.parseInt(System.getProperty(JAVA_MONITOR_ID));
            } catch (NumberFormatException e) {
                log.log(Level.WARNING, "unable to parse '" + id
                        + "' into a number, using '" + id + "' instead: "
                        + e.getMessage(), e);
            }
        }

        if (url != null) {
            collector = new Collector(url, id);
            collectorThread = new Thread(new CollectorDriver(),
                    "java-monitor collector");
        } else {
            collector = null;
            collectorThread = null;
        }
    }

    /**
     * Start the collector, if it was not already started.
     */
    public synchronized void start() {
        if (!started && collectorThread != null) {
            JmxHelper.registerCoolMBeans(server);

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
