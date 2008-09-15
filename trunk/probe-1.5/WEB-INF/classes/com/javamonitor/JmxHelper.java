package com.javamonitor;

import java.lang.management.ManagementFactory;
import java.util.Collection;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import com.javamonitor.mbeans.Server;

/**
 * The JMX facade, making JMX easy.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class JmxHelper {
    private static MBeanServer mbeanserver = null;

    /**
     * The base of all the helper object names.
     */
    public static final String objectNameBase = "com.javamonitor:type=";

    /**
     * Locate the mbean server for this JVM instance. We try to look for the
     * JBoss specific mbean server. Failing that, we just use the JVM's platorm
     * mbean server.
     * 
     * @return An appropriate mbean server.
     */
    private static MBeanServer getMBeanServer() {
        if (mbeanserver == null) {
            // first, we try to see if we are running in JBoss
            try {
                mbeanserver = (MBeanServer) Class.forName(
                        "org.jboss.mx.util.MBeanServerLocator").getMethod(
                        "locateJBoss", (Class[]) null).invoke(null,
                        (Object[]) null);
            } catch (Exception e) {
                // woops: not JBoss. Use the platform mbean server instead
                mbeanserver = ManagementFactory.getPlatformMBeanServer();
            }
        }

        return mbeanserver;
    }

    /**
     * Check that an mbean exists.
     * 
     * @param objectName
     *            The bean to check for.
     * @return <code>true</code> if the bean exists, or <code>false</code>
     *         otherwise.
     */
    public static boolean mbeanExists(final String objectName) {
        try {
            getMBeanServer().getObjectInstance(new ObjectName(objectName));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Register a new mbean.
     * 
     * @param mbean
     *            The mbean to register.
     * @param objectNameString
     *            The object name to register it under.
     */
    private static void register(final Object mbean,
            final String objectNameString) {
        try {
            final ObjectName objectName = new ObjectName(objectNameString);
            getMBeanServer().registerMBean(mbean, objectName);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Query for a string, based on the object name as string. Convenience
     * method that does the casts.
     * 
     * @param objectName
     *            The object name to query.
     * @param attribute
     *            The attribute to query.
     * @return The value of the attribute, as string.
     * @throws Exception
     *             When there was a problem querying.
     */
    public static String queryString(final String objectName,
            final String attribute) throws Exception {
        final Object value = query(objectName, attribute);
        return value == null ? null : value.toString();
    }

    /**
     * Query for an integer, based on the object name. Convenience method that
     * does the casts.
     * 
     * @param objectName
     *            The object name to query.
     * @param attribute
     *            The attribute to query.
     * @return The value of the attribute, as string.
     * @throws Exception
     *             When there was a problem querying.
     */
    public static Integer queryInt(final ObjectName objectName,
            final String attribute) throws Exception {
        final Object value = query(objectName, attribute);
        return value == null ? null : Integer.parseInt(value.toString());
    }

    /**
     * Query for a value, based on the object name as string. Convenience method
     * that does the casts.
     * 
     * @param objectName
     *            The object name to query.
     * @param attribute
     *            The attribute to query.
     * @return The value of the attribute, as string.
     * @throws Exception
     *             When there was a problem querying.
     */
    public static Object query(final String objectName, final String attribute)
            throws Exception {
        return query(new ObjectName(objectName), attribute);
    }

    /**
     * Query a JMX attribute.
     * 
     * @param objectName
     *            The name of the mbean to query.
     * @param attribute
     *            The attribute to query on that mbean.
     * @return The value of the attribute on the named object.
     * @throws Exception
     *             When there was a problem querying.
     */
    public static Object query(final ObjectName objectName,
            final String attribute) throws Exception {
        final int dot = attribute.indexOf('.');
        if (dot < 0) {
            return getMBeanServer().getAttribute(objectName, attribute);
        }

        return resolveFields((CompositeData) getMBeanServer().getAttribute(
                objectName, attribute.substring(0, dot)), attribute
                .substring(dot + 1));
    }

    private static Object resolveFields(final CompositeData attribute,
            final String field) {
        final int dot = field.indexOf('.');
        if (dot < 0) {
            final Object ret = attribute.get(field);
            return ret == null ? null : ret;
        }

        return resolveFields((CompositeData) attribute.get(field.substring(0,
                dot)), field.substring(dot + 1));
    }

    /**
     * Find a list of object names.
     * 
     * @param query
     *            The wildcarded object name to list.
     * @return A list of matching object names.
     */
    @SuppressWarnings("unchecked")
    public static Collection<ObjectName> queryNames(final String query) {
        try {
            return getMBeanServer().queryNames(new ObjectName(query), null);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register the cool beans we need to find our way in the JMX jungle.
     */
    public static void registerCoolBeans() {
        register(new Server(), Server.objectName);
    }

    /**
     * Unregister all the useful mbeans from the JMX registry.
     */
    public static void unregisterCoolBeans() {
        try {
            getMBeanServer().unregisterMBean(new ObjectName(Server.objectName));
        } catch (InstanceNotFoundException e) {
            // ignored...
        } catch (MBeanRegistrationException e) {
            // ignored...
        } catch (MalformedObjectNameException e) {
            // ignored...
        }
    }
}
