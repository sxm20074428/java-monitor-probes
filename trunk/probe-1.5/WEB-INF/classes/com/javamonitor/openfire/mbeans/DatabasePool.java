package com.javamonitor.openfire.mbeans;

import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.admin.SnapshotIF;

/**
 * The database monitor pool.
 * 
 * XXX it makes more sense to register Proxools JMX features directly!
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class DatabasePool implements DatabasePoolMBean {

    private static SnapshotIF getSnapshot() throws ProxoolException {
        return ProxoolFacade.getSnapshot("openfire", true);
    }

    private static ConnectionPoolDefinitionIF getPoolDef()
            throws ProxoolException {
        return ProxoolFacade.getConnectionPoolDefinition("openfire");
    }

    /**
     * Start collecting database packets.
     */
    public void start() {
        // nothing to do...
    }

    /**
     * Stop collecting data.
     */
    public void stop() {
        // nothing to do...
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getActiveConnectionCount()
     */
    public int getActiveConnectionCount() {
        try {
            return getSnapshot().getActiveConnectionCount();
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getAvailableConnectionCount()
     */
    public int getAvailableConnectionCount() {
        try {
            return getSnapshot().getAvailableConnectionCount();
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getConnectionCount()
     */
    public long getConnectionCount() {
        try {
            return getSnapshot().getConnectionCount();
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getOfflineConnectionCount()
     */
    public int getOfflineConnectionCount() {
        try {
            return getSnapshot().getOfflineConnectionCount();
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getRefusedCount()
     */
    public long getRefusedCount() {
        try {
            return getSnapshot().getRefusedCount();
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getServedCount()
     */
    public long getServedCount() {
        try {
            return getSnapshot().getServedCount();
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getMaximumConnectionCount()
     */
    public int getMaximumConnectionCount() {
        try {
            return getPoolDef().getMaximumConnectionCount();
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getMinimumConnectionCount()
     */
    public int getMinimumConnectionCount() {
        try {
            return getPoolDef().getMinimumConnectionCount();
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getMaximumActiveTime()
     */
    public long getMaximumActiveTime() {
        try {
            return getPoolDef().getMaximumActiveTime();
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * @see com.javamonitor.openfire.mbeans.DatabasePoolMBean#getMaximumConnectionLifetime()
     */
    public long getMaximumConnectionLifetime() {
        try {
            return getPoolDef().getMaximumConnectionLifetime();
        } catch (Exception ex) {
            return -1;
        }
    }
}
