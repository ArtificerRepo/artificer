/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.governance;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet is used to initialize SRAMP monitor.
 * 
 */
public class GovernanceServlet extends HttpServlet {
	
    private static final long serialVersionUID = -2902363450855487818L;
    private Logger log = LoggerFactory.getLogger(this.getClass());
	private SRAMPMonitor monitor;

	/**
	 * Create the SRAMP monitor.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
            monitor = new SRAMPMonitor();
        } catch (ConfigurationException e) {
            log.error(e.getMessage(),e);
        }
	}
	
	@Override
	public void destroy() {
	    monitor.cancel();
		super.destroy();
	}

}