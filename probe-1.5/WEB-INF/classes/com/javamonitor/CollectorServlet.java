package com.javamonitor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * The starting point for the JEE aplication server data collection.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class CollectorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private JavaMonitorCollector collector;

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();

        collector = new JavaMonitorCollector();
        collector.start();
    }

    /**
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        collector.stop();

        super.destroy();
    }
}
