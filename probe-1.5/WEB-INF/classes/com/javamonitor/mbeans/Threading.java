package com.javamonitor.mbeans;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.javamonitor.JmxHelper;

/**
 * A threading bean.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Threading implements ThreadingMBean {
    /**
     * The object name for the threading helper mbean.
     */
    public static final String objectName = JmxHelper.objectNameBase
            + "Threading";

    private static final ThreadMXBean threadMXBean = ManagementFactory
            .getThreadMXBean();

    private static Method findDeadlockMethod = null;
    static {
        try {
            findDeadlockMethod = ThreadMXBean.class
                    .getMethod("findDeadlockedThreads");
        } catch (Exception ignored) {
            // woops, well I guess this is a 1.5 JVM then

            try {
                findDeadlockMethod = ThreadMXBean.class
                        .getMethod("findMonitorDeadlockedThreads");
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private ThreadInfo[] findDeadlock() throws IllegalAccessException,
            InvocationTargetException {
        final long[] threadIds = (long[]) findDeadlockMethod.invoke(
                threadMXBean, (Object[]) null);

        if (threadIds == null || threadIds.length < 1) {
            // no deadlock, we're done
            return null;
        }

        final ThreadInfo[] threads = threadMXBean.getThreadInfo(threadIds,
                Integer.MAX_VALUE);
        return threads;
    }

    /**
     * @see com.javamonitor.mbeans.ThreadingMBean#getDeadlockStacktraces()
     */
    public String getDeadlockStacktraces() {
        try {
            final ThreadInfo[] threads = findDeadlock();
            if (threads == null) {
                // no deadlock, we're done
                return null;
            }

            return stacktraces(threads, 0);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static final int MAX_STACK = 10;

    private String stacktraces(final ThreadInfo[] threads, final int i) {
        if (i >= threads.length) {
            return "";
        }
        final ThreadInfo thread = threads[i];

        String trace = "";
        for (int stack_i = 0; stack_i < Math.min(thread.getStackTrace().length,
                MAX_STACK); stack_i++) {
            if (stack_i == (MAX_STACK - 1)) {
                trace += "    ...";
            } else {
                trace += "    at " + thread.getStackTrace()[stack_i] + "\n";
            }
        }

        return "\"" + thread.getThreadName() + "\", id " + thread.getThreadId()
                + " is " + thread.getThreadState() + " on "
                + thread.getLockName() + ", owned by "
                + thread.getLockOwnerName() + ", id " + thread.getLockOwnerId()
                + "\n" + trace + "\n\n" + stacktraces(threads, i + 1);
    }
}
