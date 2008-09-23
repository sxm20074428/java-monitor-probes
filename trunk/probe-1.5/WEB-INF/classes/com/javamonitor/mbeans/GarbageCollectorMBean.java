package com.javamonitor.mbeans;

/**
 * An mbean to make access to the garbage collector data easier.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public interface GarbageCollectorMBean {
    String getName();

    long getCount();

    long getTime();
}
