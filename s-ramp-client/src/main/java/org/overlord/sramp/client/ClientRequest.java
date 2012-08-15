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
package org.overlord.sramp.client;

import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * Extends the RESTEasy {@link org.jboss.resteasy.client.ClientRequest} class in order to provide a
 * {@link ClientExecutor} and {@link ResteasyProviderFactory} without requiring clients to pass them in.
 * 
 * @author eric.wittmann@redhat.com
 */
public class ClientRequest extends org.jboss.resteasy.client.ClientRequest {
	
	private static final ResteasyProviderFactory providerFactory = new ResteasyProviderFactory();
	static {
		RegisterBuiltin.register(providerFactory);
	}

	/**
	 * Constructor.
	 * @param uriTemplate
	 */
	public ClientRequest(String uriTemplate) {
		super(getBuilder(uriTemplate), getDefaultExecutor(), providerFactory);
	}

	/**
	 * Creates a {@link UriBuilder} for the given URI template.
	 * @param uriTemplate
	 */
	private static UriBuilder getBuilder(String uriTemplate) {
		return new UriBuilderImpl().uriTemplate(uriTemplate);
	}

}
