package org.overlord.sramp.atom;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.overlord.sramp.repository.PersistenceFactory;

/**
 * Listener for deploy/undeploy events.
 */
public class SrampLifeCycle implements ServletContextListener {
	private static final long serialVersionUID = 1L;
       	
	

    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        PersistenceFactory.newInstance().shutdown();
    }

}
