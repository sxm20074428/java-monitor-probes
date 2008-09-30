package com.javamonitor.mbeans;

/**
 * A thread deadlock reporter. This mbean makes the thread deadlocks simpler to
 * read.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public interface ThreadingMBean {
    /**
     * Get a more detailed report on the deadlocked threads.
     * 
     * @return A list of threads that were deadlocked, with their stack traces,
     *         in developer readable format, or <code>null</code> if there are
     *         no deadlocks.
     */
    public String getDeadlockStacktraces();
}
