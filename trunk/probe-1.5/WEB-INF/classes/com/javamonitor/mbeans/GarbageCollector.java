package com.javamonitor.mbeans;

import javax.management.ObjectName;

import com.javamonitor.JmxHelper;

/**
 * An mbean to make access to the garbage collector data easier. This object can
 * run as one of two types: quick and thorough. Running as quick, it will act as
 * a frontend for whatever GC the JVM picked as the quick collector. Likewise
 * for thorough.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class GarbageCollector implements GarbageCollectorMBean {
    private final String objectName;

    private ObjectName actualGC;

    /**
     * Construct a new garbage collector facade.
     * 
     * @param quick
     *            <code>true</code> if this instance is to facade for the
     *            quick gc, or <code>false</code> if it is to facade for the
     *            thorough one.
     */
    public GarbageCollector(final boolean quick) {
        objectName = JmxHelper.objectNameBase + "GarbageCollector,name="
                + (quick ? "Quick" : "Thorough");

        actualGC = null;
        if (quick) {
            try {
                final ObjectName gc = new ObjectName(
                        "java.lang:type=GarbageCollector,name=Copy");
                JmxHelper.query(gc, "Name");
                actualGC = gc;
            } catch (Exception e) {
                // nope, does not exist
            }

            try {
                final ObjectName gc = new ObjectName(
                        "java.lang:type=GarbageCollector,name=PS Scavenge");
                JmxHelper.query(gc, "Name");
                actualGC = gc;
            } catch (Exception e) {
                // nope, does not exist
            }

            try {
                final ObjectName gc = new ObjectName(
                        "java.lang:type=GarbageCollector,name=ParNew");
                JmxHelper.query(gc, "Name");
                actualGC = gc;
            } catch (Exception e) {
                // nope, does not exist
            }
        } else {
            // not quick, thorough

            try {
                final ObjectName gc = new ObjectName(
                        "java.lang:type=GarbageCollector,name=MarkSweepCompact");
                JmxHelper.query(gc, "Name");
                actualGC = gc;
            } catch (Exception e) {
                // nope, does not exist
            }

            try {
                final ObjectName gc = new ObjectName(
                        "java.lang:type=GarbageCollector,name=PS MarkSweep");
                JmxHelper.query(gc, "Name");
                actualGC = gc;
            } catch (Exception e) {
                // nope, does not exist
            }

            try {
                final ObjectName gc = new ObjectName(
                        "java.lang:type=GarbageCollector,name=Concurrent MarkSweep");
                JmxHelper.query(gc, "Name");
                actualGC = gc;
            } catch (Exception e) {
                // nope, does not exist
            }
        }
    }

    /**
     * Get the correct object name to register this mbean under.
     * 
     * @return The object name.
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * @see com.javamonitor.mbeans.GarbagCollectorMBean#getName()
     */
    public String getName() {
        if (actualGC == null) {
            return "unknown garbage collection scheme";
        }
        try {
            return JmxHelper.queryString(actualGC, "Name");
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * @see com.javamonitor.mbeans.GarbagCollectorMBean#getCount()
     */
    public long getCount() {
        if (actualGC == null) {
            return -1L;
        }
        try {
            return JmxHelper.queryLong(actualGC, "CollectionCount");
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.mbeans.GarbagCollectorMBean#getTime()
     */
    public long getTime() {
        if (actualGC == null) {
            return -1L;
        }
        try {
            return JmxHelper.queryLong(actualGC, "CollectionTime");
        } catch (Exception e) {
            return -1;
        }
    }
}
