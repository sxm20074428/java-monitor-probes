package com.javamonitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * The starting point for the entire data collection.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class CollectorServlet extends HttpServlet {
    private static final long serialVersionUID = 7230361089078209652L;

    

    private static final Logger log = Logger.getLogger(CollectorServlet.class
            .getName());

    private Thread collectorThread = null;

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {

        final URL url;
        try {
            url = new URL(getServletConfig().getInitParameter("url"));
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE,
                    "Java-monitor collector cannot start: bad url in web.xml: " + e.getMessage(), e);
            return; // bail out, this is going nowhere
        }


        super.init();

       
    }

    /**
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
    }

    
}
