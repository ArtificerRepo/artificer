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
package org.overlord.sramp.atom.err;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;

/**
 * An exception handler for sramp atom exceptions.
 *
 * @author eric.wittmann@redhat.com
 */
@Provider
@ServerInterceptor
public class SrampAtomExceptionMapper implements ExceptionMapper<SrampAtomException>, MessageBodyWriter<SrampAtomException> {
	
	/**
	 * Constructor.
	 */
	public SrampAtomExceptionMapper() {
	}

	/**
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	@Override
	public Response toResponse(SrampAtomException exception) {
		ResponseBuilder builder = Response.status(500);
		builder.header("S-RAMP-Exception-Message", getRootCause(exception).getMessage());
		builder.type("application/stacktrace");
		builder.entity(exception);
		return builder.build();
	}
	
	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type == SrampAtomException.class;
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
	public void writeTo(SrampAtomException t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		String stack = getRootStackTrace(t);
		entityStream.write(stack.getBytes("UTF-8"));
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

}
