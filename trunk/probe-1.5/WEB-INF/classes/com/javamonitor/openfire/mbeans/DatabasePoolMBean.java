package com.javamonitor.openfire.mbeans;

/**
 * 
 * TODO Add JavaDoc comment...
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public interface DatabasePoolMBean {

	public int getMinimumConnectionCount();

	public int getMaximumConnectionCount();

	public int getAvailableConnectionCount();

	public int getActiveConnectionCount();

	public long getMaximumActiveTime();

	public long getMaximumConnectionLifetime();

	public long getServedCount();

	public long getRefusedCount();

	public int getOfflineConnectionCount();

	public long getConnectionCount();
}
