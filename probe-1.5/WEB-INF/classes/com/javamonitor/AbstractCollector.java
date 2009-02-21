/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javamonitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author goltharnl
 */
public class AbstractCollector {

    private URL url;

    private Thread collectorThread = null;

    protected Logger log;

    private static final long MINUTES = 60L * 1000L;
    public static final String JAVA_MONITOR_URL_PROPERTYNAME = "javamonitor.url";

    public AbstractCollector() {
        log = Logger.getLogger(this.getClass().getName());
        String urlstring = System.getProperty(JAVA_MONITOR_URL_PROPERTYNAME);
        if(urlstring!=null) {
            try {
                url = new URL(urlstring);
            } catch (MalformedURLException ex) {
                Logger.getLogger(AbstractCollector.class.getName()).log(Level.SEVERE, "Warning the system property set is not a valid URL for java-monitor!", ex);
            }
        }
    }

    public void init() throws Exception {

        JmxHelper.registerCoolMBeans();


        collectorThread = new Thread(new CollectorDriver(),
                "java-monitor collector");
        collectorThread.start();
    }

    public void destroy() {
        collectorThread.interrupt();
        try {
            collectorThread.join();
        } catch (InterruptedException e) {
            // ignore, we're going down anyway
        }

        JmxHelper.unregisterCoolMBeans();
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
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
                            if (Collector.push(url)) {
                                Collector.push(url);
                            }

                            Thread.sleep(1L * MINUTES);
                        }
                    } catch (InterruptedException e) {
                        throw e; // it ends up in the outer try block
                    } catch (OnHoldException e) {
                        throw e; // it ends up in the outer try block
                    } catch (Throwable e) {
                        log
                                .log(
                                        Level.SEVERE,
                                        "This probe was hit by an unexpected exception (it will retry in fifteen minutes): "
                                                + e.getMessage(), e);
                        Thread.sleep(15 * MINUTES);
                        log.log(Level.INFO, "resuming operation");
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
