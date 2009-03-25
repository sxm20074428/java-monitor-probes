package com.javamonitor.simple;

import com.javamonitor.JavaMonitorCollector;

/**
 * A sample program to demonstrate how to start the Java-monitor probe from a
 * Java program.
 * 
 * @author goltharnl
 */
public class Launcher {
    /**
     * The main application entry point.
     * 
     * @param args
     *            ignored.
     * @throws InterruptedException
     */
    public static void main(final String args[]) throws InterruptedException {
        //
        // Create a new collector, specifying the Java-monitor collector server
        // URL and a unique application ID.
        //
        // The URL can be overridden from the command line by specifying the
        // system property javamonitor.url. The command line option takes
        // precedence over the hardcoded one.
        //
        // The unique ID identifies this JVM on the machine if you have more
        // than one. This ID can be overridden on the command line using the
        // system property javamonitor.uniqueid.
        //
        final JavaMonitorCollector collector = new JavaMonitorCollector(
                "http://194.109.206.50/lemongrass/1.0/push", 2143);

        // start the collector...
        collector.start();

        // sleep for a bit, to see the results...
        Thread.sleep(1000000);

        // and finally clean up.
        collector.stop();
    }
}
