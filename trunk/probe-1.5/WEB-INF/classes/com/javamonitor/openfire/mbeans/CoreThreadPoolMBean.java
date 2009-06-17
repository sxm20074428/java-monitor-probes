package com.javamonitor.openfire.mbeans;

/**
 * MBean definition for collectors that gather statistics of one of the core thread pools in use by the server. 
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public interface CoreThreadPoolMBean {

	public int getCorePoolSize();

	public int getMaximumPoolSize();
	
	public int getActiveCount();
	
	public int getQueueSize();
	
	public long getCompletedTaskCount();
	
	public int getLargestPoolSize();

	public int getPoolSize();

	public long getTaskCount();
	
	public long getMinaBytesRead();

	public long getMinaBytesWritten();
	
	public long getMinaMsgRead();
	
	public long getMinaMsgWritten();
	
	public long getMinaQueuedEvents();
	
	public long getMinaScheduledWrites();

	public long getMinaSessionCount();

	public long getMinaTotalProcessedSessions();
}
