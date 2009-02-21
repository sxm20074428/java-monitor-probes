/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javamonitor.simple;

import com.javamonitor.AbstractCollector;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author goltharnl
 */
public class Launcher {
    public static void main(String args[]) throws MalformedURLException, Exception {
        AbstractCollector collector = new AbstractCollector();
        URL url = new URL("http://java-monitor.com/lemongrass/1.0/push");
        collector.setUrl(url);
        collector.init();
        Thread.sleep(1000000);
    }
}
