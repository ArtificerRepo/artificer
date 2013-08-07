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
package org.overlord.sramp.atom.providers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.i18n.Messages;

/**
 * A RESTEasy provider for reading/writing an S-RAMP Exception.
 *
 * @author eric.wittmann@redhat.com
 */
@Provider
@ServerInterceptor
@Produces(org.overlord.sramp.atom.MediaType.APPLICATION_SRAMP_ATOM_EXCEPTION)
@Consumes(org.overlord.sramp.atom.MediaType.APPLICATION_SRAMP_ATOM_EXCEPTION)
public class SrampAtomExceptionProvider implements ExceptionMapper<SrampAtomException>,
		MessageBodyWriter<SrampAtomException>, MessageBodyReader<SrampAtomException> {

	/**
	 * Constructor.
	 */
	public SrampAtomExceptionProvider() {
	}

	/**
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	@Override
	public Response toResponse(SrampAtomException exception) {
		ResponseBuilder builder = Response.status(500);
		builder.header("Error-Message", getRootCause(exception).getMessage()); //$NON-NLS-1$
		builder.type(org.overlord.sramp.atom.MediaType.APPLICATION_SRAMP_ATOM_EXCEPTION);
		String stack = getRootStackTrace(exception);
		builder.entity(stack);
		return builder.build();
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SrampAtomException.class.equals(type);
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public long getSize(SrampAtomException t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1l;
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
	 */
	@Override
	public void writeTo(SrampAtomException error, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		if (httpHeaders != null)
			httpHeaders.putSingle("Error-Message", getRootCause(error).getMessage()); //$NON-NLS-1$
		String stack = getRootStackTrace(error);
		entityStream.write(stack.getBytes("UTF-8")); //$NON-NLS-1$
		entityStream.flush();
	}

	/**
	 * Gets the root stack trace as a string.
	 * @param t
	 */
	public static String getRootStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		getRootCause(t).printStackTrace(writer);
		return sw.getBuffer().toString();
	}

	/**
	 * Gets the root exception from the given {@link Throwable}.
	 * @param t
	 */
	public static Throwable getRootCause(Throwable t) {
		Throwable root = t;
		while (root.getCause() != null && root.getCause() != root)
			root = root.getCause();
		return root;
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SrampAtomException.class.equals(type)
				|| org.overlord.sramp.atom.MediaType.APPLICATION_SRAMP_ATOM_EXCEPTION_TYPE.equals(mediaType);
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
	 */
	@Override
	public SrampAtomException readFrom(Class<SrampAtomException> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		List<String> lines = IOUtils.readLines(entityStream);
		StringBuilder buffer = new StringBuilder();
		for (String line : lines) {
			buffer.append(line).append("\n"); //$NON-NLS-1$
		}
		String stackTrace = buffer.toString();
		String msg = httpHeaders == null ? null : httpHeaders.getFirst("Error-Message"); //$NON-NLS-1$
		if (msg == null) {
			msg = Messages.i18n.format("UNKNOWN_SRAMP_ERROR"); //$NON-NLS-1$
		}
		SrampAtomException error = new SrampAtomException(msg, stackTrace);
		return error;
	}

}
