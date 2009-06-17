package com.javamonitor.openfire.mbeans;

/**
 * MBean definition for a collector that keeps track of the amount (and types)
 * of stanzas that have been processed by the server.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public interface PacketCounterMBean {

	/**
	 * Gets the total number of stanzas (including message, IQ and presence
	 * stanzas) that have been processed by the service, since the monitor
	 * service was activated last.
	 * 
	 * @return the number of processed stanzas.
	 */
	public long getStanzaCount();

	/**
	 * Gets the number of messages stanzas that have been processed by the
	 * service, since the monitor service was activated last.
	 * 
	 * @return the number of processed message stanzas.
	 */
	public long getMessageCount();

	/**
	 * Gets the number of presence stanzas that have been processed by the
	 * service, since the monitor service was activated last.
	 * 
	 * @return the number of processed presence stanzas.
	 */
	public long getPresenceCount();

	/**
	 * Gets the number of IQ stanzas that have been processed by the service,
	 * since the monitor service was activated last.
	 * 
	 * @return the number of processed IQ stanzas.
	 */
	public long getIQCount();

	/**
	 * Gets the number of IQ stanzas of type GET that have been processed by the
	 * service, since the monitor service was activated last.
	 * 
	 * @return the number of processed IQ-get stanzas.
	 */
	public long getIQGetCount();

	/**
	 * Gets the number of IQ stanzas of type SET that have been processed by the
	 * service, since the monitor service was activated last.
	 * 
	 * @return the number of processed IQ-set stanzas.
	 */
	public long getIQSetCount();

	/**
	 * Gets the number of IQ stanzas of type RESULT that have been processed by
	 * the service, since the monitor service was activated last.
	 * 
	 * @return the number of processed IQ-result stanzas.
	 */
	public long getIQResultCount();

	/**
	 * Gets the number of IQ stanzas of type ERROR that have been processed by
	 * the service, since the monitor service was activated last.
	 * 
	 * @return the number of processed IQ-error stanzas.
	 */
	public long getIQErrorCount();

}
