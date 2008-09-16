/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javamonitor;

/**
 *
 * @author barry
 */
public enum ConfigProperties {
    
    SSLPUSH("com.javamonitor.ssl"), UNKNOWN("");
    private String key;
    private String value = "";
    
    private ConfigProperties(String key)
    {
        this.key = key;
        this.value = ProbeConfiguration.getProperty(key);
    }
    
    public String getValue() {
        return value;
    }
    
    public boolean enabled() {
        return "true".equalsIgnoreCase(value);
    }
}
