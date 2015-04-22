package org.artificer.server;

import org.artificer.events.EventProducer;
import org.artificer.events.EventProducerFactory;
import org.artificer.repository.RepositoryProviderFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Listener for deploy/undeploy events.
 */
public class ArtificerLifeCycle implements ServletContextListener {

	/**
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
    public void contextInitialized(ServletContextEvent sce) {
	    // TODO make this async
        RepositoryProviderFactory.persistenceManager().startup();
	    
	    for (EventProducer eventProducer : EventProducerFactory.getEventProducers()) {
	        eventProducer.startup();
	    }
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        RepositoryProviderFactory.persistenceManager().shutdown();
        
        for (EventProducer eventProducer : EventProducerFactory.getEventProducers()) {
            eventProducer.shutdown();
        }
    }

}
