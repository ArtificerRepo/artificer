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
package org.artificer.client;

import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.artificer.atom.MediaType;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.atom.i18n.Messages;
import org.artificer.atom.providers.HttpResponseProvider;
import org.artificer.atom.providers.ArtificerAtomExceptionProvider;

import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Method;

/**
 * Extends the RESTEasy {@link org.jboss.resteasy.client.ClientRequest} class in order to provide a
 * {@link org.jboss.resteasy.client.ClientExecutor} and {@link org.jboss.resteasy.spi.ResteasyProviderFactory} without requiring clients to pass them in.
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
		providerFactory.registerProvider(ArtificerAtomExceptionProvider.class);
		providerFactory.registerProvider(HttpResponseProvider.class);
	}

    private static Method uriBuilderMethod = null;
    static {
        // Extremely hacky way of doing this.  However, for now, I want to support both EAP 6 (RESTEasy 2) and
        // Wildfly (RESTEasy 3).  Rather than introducing a new UriBuilderProvider service, just keep it simple.
        // Note that I'm naively assuming it will always been one or the other...
        try {
            // RE 3
            Class<?> uriBuilderClass = ClientRequest.class.getClassLoader().loadClass("org.jboss.resteasy.specimpl.ResteasyUriBuilder");
            uriBuilderMethod = uriBuilderClass.getMethod("fromUri", String.class);
        } catch (Exception e) {
            try {
                // RE 2
                Class<?> uriBuilderClass = ClientRequest.class.getClassLoader().loadClass("org.jboss.resteasy.specimpl.UriBuilderImpl");
                uriBuilderMethod = uriBuilderClass.getMethod("fromUri", String.class);
            } catch (Exception e1) {
            }
        }
    }

	/**
	 * Creates a {@link javax.ws.rs.core.UriBuilder} for the given URI template.
	 * @param uriTemplate
	 */
	private static UriBuilder getBuilder(String uriTemplate) {
        try {
            return (UriBuilder) uriBuilderMethod.invoke(null, uriTemplate);
        } catch (Exception e) {
            // TODO
            return null;
        }
	}

    /**
     * Constructor.
     * @param uriTemplate
     */
    public ClientRequest(String uriTemplate) {
        super(getBuilder(uriTemplate), getDefaultExecutor(), providerFactory);
    }

    /**
     * Constructor.
     * @param uriTemplate
     * @param clientExecutor
     */
    public ClientRequest(String uriTemplate, ClientExecutor clientExecutor) {
        super(getBuilder(uriTemplate), clientExecutor, providerFactory);
    }

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#post(Class)
	 */
	@Override
	public <T> ClientResponse<T> post(Class<T> returnType) throws Exception {
		ClientResponse<T> response = super.post(returnType);
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#post()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ClientResponse post() throws Exception {
		ClientResponse response = super.post();
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#get(Class)
	 */
	@Override
	public <T> ClientResponse<T> get(Class<T> returnType) throws Exception {
		ClientResponse<T> response = super.get(returnType);
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#get()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ClientResponse get() throws Exception {
		ClientResponse response = super.get();
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#put(Class)
	 */
	@Override
	public <T> ClientResponse<T> put(Class<T> returnType) throws Exception {
		ClientResponse<T> response = super.put(returnType);
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#put()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ClientResponse put() throws Exception {
		ClientResponse response = super.put();
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#delete(Class)
	 */
	@Override
	public <T> ClientResponse<T> delete(Class<T> returnType) throws Exception {
		ClientResponse<T> response = super.delete(returnType);
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * @see org.jboss.resteasy.client.ClientRequest#delete()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ClientResponse delete() throws Exception {
		ClientResponse response = super.delete();
		handlePotentialServerError(response);
		return response;
	}

	/**
	 * Handles the possibility of an error found in the response.
	 * @param response
	 * @throws Exception
	 */
	private <T> void handlePotentialServerError(ClientResponse<T> response) throws Exception {
		String contentType = String.valueOf(response.getMetadata().getFirst(HttpHeaderNames.CONTENT_TYPE));
		if (response.getStatus() == 500) {
			Exception error = new Exception(Messages.i18n.format("UNKNOWN_ARTIFICER_ERROR")); //$NON-NLS-1$
			if (MediaType.APPLICATION_SRAMP_ATOM_EXCEPTION.equals(contentType)) {
				try {
					ArtificerAtomException entity = response.getEntity(ArtificerAtomException.class);
					if (entity != null)
						error = entity;
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			throw error;
		}
		if (response.getStatus() == 404 || response.getStatus() == 415) {
			ArtificerAtomException error = new ArtificerAtomException(Messages.i18n.format("ENDPOINT_NOT_FOUND")); //$NON-NLS-1$
			throw error;
		}
		if (response.getStatus() == 403) {
			ArtificerAtomException error = new ArtificerAtomException(Messages.i18n.format("AUTHORIZATION_FAILED")); //$NON-NLS-1$
			throw error;
		}
		if (response.getStatus() == 401) {
            ArtificerAtomException error = new ArtificerAtomException(Messages.i18n.format("AUTHENTICATION_FAILED")); //$NON-NLS-1$
            throw error;
		}
	}

}
