/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javamonitor;

import java.util.logging.Logger;

/**
 *
 * @author barry
 */
public class ProbeConfiguration {
    
    
    
    private static Logger log = Logger.getLogger(ProbeConfiguration.class.getName());
    
    
    public static String getProperty(String key) {
        if(key!=null) {
            try {
                return System.getProperty(key);
            } catch(SecurityException sx) {
                log.warning("Could not instantiate probe configuration due to security constraints; Use different form of configuration or relax policy to allow properties of format com.javamonitor");
            }
        }
        
        return null;
    }
    
    public static boolean isEnabled(String key) {
        if(key!=null) {
            try {
                String val = System.getProperty(key);
                return "true".equalsIgnoreCase(val);
            } catch(SecurityException sx) {
                log.warning("Could not instantiate probe configuration due to security constraints; Use different form of configuration or relax policy to allow properties of format com.javamonitor");
            }
        }
        
        return false;
    }
    
}
