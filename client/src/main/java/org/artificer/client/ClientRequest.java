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

import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.atom.i18n.Messages;
import org.artificer.atom.providers.ArtificerConflictExceptionProvider;
import org.artificer.atom.providers.ArtificerNotFoundExceptionProvider;
import org.artificer.atom.providers.ArtificerServerExceptionProvider;
import org.artificer.atom.providers.ArtificerWrongModelExceptionProvider;
import org.artificer.atom.providers.HttpResponseProvider;
import org.artificer.common.MediaType;
import org.artificer.common.error.ArtificerConflictException;
import org.artificer.common.error.ArtificerNotFoundException;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.common.error.ArtificerWrongModelException;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpHeaderNames;

import javax.ws.rs.core.Response;
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
		providerFactory.registerProvider(ArtificerServerExceptionProvider.class);
		providerFactory.registerProvider(ArtificerConflictExceptionProvider.class);
		providerFactory.registerProvider(ArtificerNotFoundExceptionProvider.class);
		providerFactory.registerProvider(ArtificerWrongModelExceptionProvider.class);
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
		Response.Status status = Response.Status.fromStatusCode(response.getStatus());

		if (MediaType.APPLICATION_ARTIFICER_SERVER_EXCEPTION.equals(contentType)) {
			throw response.getEntity(ArtificerServerException.class);
		}

		if (MediaType.APPLICATION_ARTIFICER_CONFLICT_EXCEPTION.equals(contentType)) {
			throw response.getEntity(ArtificerConflictException.class);
		}

		if (MediaType.APPLICATION_ARTIFICER_NOTFOUND_EXCEPTION.equals(contentType)) {
			throw response.getEntity(ArtificerNotFoundException.class);
		}

		if (MediaType.APPLICATION_ARTIFICER_WRONGMODEL_EXCEPTION.equals(contentType)) {
			throw response.getEntity(ArtificerWrongModelException.class);
		}

		switch (status) {
			case INTERNAL_SERVER_ERROR:
				throw new Exception(Messages.i18n.format("UNKNOWN_ARTIFICER_ERROR")); //$NON-NLS-1$
			case NOT_FOUND:
			case UNSUPPORTED_MEDIA_TYPE:
				throw new ArtificerAtomException(Messages.i18n.format("ENDPOINT_NOT_FOUND")); //$NON-NLS-1$
			case FORBIDDEN:
				throw new ArtificerAtomException(Messages.i18n.format("AUTHORIZATION_FAILED")); //$NON-NLS-1$
			case UNAUTHORIZED:
				throw new ArtificerAtomException(Messages.i18n.format("AUTHENTICATION_FAILED")); //$NON-NLS-1$
		}
	}

}
