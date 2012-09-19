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
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.overlord.sramp.atom.providers.HttpResponseProvider;

/**
 * Extends the RESTEasy {@link org.jboss.resteasy.client.ClientRequest} class in order to provide a
 * {@link ClientExecutor} and {@link ResteasyProviderFactory} without requiring clients to pass them in.
 *
 * Additionally, this class overrides the various http methods (post, get, put) in order to implement
 * some error handling.  These methods will throw an appropriate exception now (when possible), rather
 * than a less meaningful RESTEasy generic exception.  When communicating with the JBoss s-ramp
 * implementation, this error handling should work well (it should throw an exception that also includes
 * the server-side root-cause stack trace).  When connecting to some other s-ramp implementation, your
 * mileage may vary.
 *
 * @author eric.wittmann@redhat.com
 */
public class ClientRequest extends org.jboss.resteasy.client.ClientRequest {

	private static final ResteasyProviderFactory providerFactory = new ResteasyProviderFactory();
	static {
		RegisterBuiltin.register(providerFactory);
		providerFactory.registerProvider(SrampClientExceptionReader.class);
		providerFactory.registerProvider(HttpResponseProvider.class);
	}

	/**
	 * Creates a {@link UriBuilder} for the given URI template.
	 * @param uriTemplate
	 */
	private static UriBuilder getBuilder(String uriTemplate) {
		return new UriBuilderImpl().uriTemplate(uriTemplate);
	}


	/**
	 * Constructor.
	 * @param uriTemplate
	 */
	public ClientRequest(String uriTemplate) {
		super(getBuilder(uriTemplate), getDefaultExecutor(), providerFactory);
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#post(java.lang.Class)
	 */
	@Override
	public <T> ClientResponse<T> post(Class<T> returnType) throws Exception {
		ClientResponse<T> response = super.post(returnType);
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#get(java.lang.Class)
	 */
	@Override
	public <T> ClientResponse<T> get(Class<T> returnType) throws Exception {
		ClientResponse<T> response = super.get(returnType);
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#put(java.lang.Class)
	 */
	@Override
	public <T> ClientResponse<T> put(Class<T> returnType) throws Exception {
		ClientResponse<T> response = super.put(returnType);
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#delete(java.lang.Class)
	 */
	@Override
	public <T> ClientResponse<T> delete(Class<T> returnType) throws Exception {
		ClientResponse<T> response = super.delete(returnType);
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * Handles the possibility of an error found in the response.
	 * @param response
	 */
	private <T> void handlePotentialServerError(ClientResponse<T> response) {
		String contentType = String.valueOf(response.getMetadata().getFirst(HttpHeaderNames.CONTENT_TYPE));
		if (response.getStatus() == 500) {
			SrampServerException error = new SrampServerException("An unexpected (and unknown) error was sent by the S-RAMP repository.");
			if ("application/stacktrace".equals(contentType)) {
				try {
					SrampServerException entity = response.getEntity(SrampServerException.class);
					if (entity != null)
						error = entity;
				} catch (Throwable t) {}
			}
			throw error;
		}
		if (response.getStatus() == 404) {
			SrampServerException error = new SrampServerException("The S-RAMP endpoint and/or method could not be found.");
			throw error;
		}
	}

}
