package org.overlord.sramp.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.overlord.sramp.events.EventProducer;
import org.overlord.sramp.events.EventProducerFactory;
import org.overlord.sramp.repository.PersistenceFactory;

/**
 * Listener for deploy/undeploy events.
 */
public class SrampLifeCycle implements ServletContextListener {

	/**
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
    public void contextInitialized(ServletContextEvent sce) {
	    // TODO make this async
	    PersistenceFactory.newInstance().startup();
	    
	    for (EventProducer eventProducer : EventProducerFactory.getEventProducers()) {
	        eventProducer.startup();
	    }
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        PersistenceFactory.newInstance().shutdown();
        
        for (EventProducer eventProducer : EventProducerFactory.getEventProducers()) {
            eventProducer.shutdown();
        }
    }

}
