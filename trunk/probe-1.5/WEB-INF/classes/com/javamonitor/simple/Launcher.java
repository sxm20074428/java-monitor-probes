package com.javamonitor.simple;

import com.javamonitor.JavaMonitorCollector;

/**
 * A sample program to demonstrate how to start the Java-monitor probe from a
 * Java program.
 * 
 * @author Barry van Someren
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Launcher {
    /**
     * The main application entry point.
     * 
     * @param args
     *            ignored.
     * @throws Exception
     *             When the probe could not be started.
     */
    public static void main(final String args[]) throws Exception {
        //
        // Create a new collector, specifying a unique application ID.
        // 
        // Don't read anything in the number, it is just something I made up.
        // You
        // can just pick anything you like. As long as you have a different one
        // for each application that runs on your machine.
        //
        // The unique ID identifies this JVM on the machine if you have more
        // than one. This ID can be overridden on the command line using the
        // system property javamonitor.uniqueid.
        //
        // If you run the application inside Tomcat, JBoss or SpringSource DM
        // Server, you can leave the number out. The probe will then pick one
        // for you.
        //
        final JavaMonitorCollector collector = new JavaMonitorCollector(
                "21435489");

        // start the collector...
        collector.start();

        // sleep for a bit, go to http://java-monitor.com/forum/ to see the
        // collected data...
        Thread.sleep(1000000L);

        // and finally clean up.
        collector.stop();
    }
}
