package com.javamonitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * The starting point for the entire data collection.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class CollectorServlet extends HttpServlet {
    private static final long serialVersionUID = 7230361089078209652L;

    private static final long MINUTES = 60L * 1000L;

    private static final Logger log = Logger.getLogger(CollectorServlet.class
            .getName());

    private Thread collectorThread = null;

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();

        try {
            JmxHelper.registerCoolBeans();
        } catch (Exception e) {
            throw new ServletException(e);
        }

        collectorThread = new Thread(new CollectorDriver(),
                "java-monitor collector");
        collectorThread.start();
    }

    /**
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        collectorThread.interrupt();
        try {
            collectorThread.join();
        } catch (InterruptedException e) {
            // ignore, we're going down anyway
        }

        JmxHelper.unregisterCoolBeans();

        super.destroy();
    }

    private final class CollectorDriver implements Runnable {
        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                final URL url;
                try {
                    url = new URL(getServletConfig().getInitParameter("url"));
                } catch (MalformedURLException e) {
                    log.log(Level.SEVERE,
                            "Java-monitor collector cannot start: bad url in web.xml: "
                                    + e.getMessage(), e);
                    return; // bail out, this is going nowhere
                }

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
