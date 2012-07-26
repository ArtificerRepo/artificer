/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.ui.server.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.JNDIConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.overlord.sramp.ui.server.api.SrampAtomApiClient;

/**
 * A simple context listener used to initialize the s-ramp UI application.
 * 
 * @author eric.wittmann@redhat.com
 */
public class Initializer implements ServletContextListener {

	/**
	 * Default constructor.
	 */
	public Initializer() {
	}
	
	/**
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			CompositeConfiguration config = new CompositeConfiguration();
			config.addConfiguration(new SystemConfiguration());
			config.addConfiguration(new PropertiesConfiguration(Initializer.class.getResource("/META-INF/config/org.overlord.sramp.ui.server.api.properties")));
			config.addConfiguration(new JNDIConfiguration("java:comp/env/overlord/s-ramp-ui"));
			System.out.println("S-RAMP user interface configuration loaded.  S-RAMP Atom API endpoint: " + config.getString("s-ramp.atom-api.endpoint"));
			
			// Now initialize the S-RAMP Atom API Client singleton
			SrampAtomApiClient.init(config);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

}
