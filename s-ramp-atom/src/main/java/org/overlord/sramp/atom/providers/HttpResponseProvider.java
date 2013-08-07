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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.jboss.resteasy.util.HttpHeaderNames;
import org.overlord.sramp.atom.beans.HttpResponseBean;
import org.overlord.sramp.atom.i18n.Messages;

/**
 * A RESTEasy provider for reading/writing an HTTP Response.  This is used in the batch
 * processing support in s-ramp.  In the batch operations, the return value is always a
 * multipart/related HTTP response, made up of a list of individual HTTP responses (one
 * for each entry in the batch).
 *
 * @author eric.wittmann@redhat.com
 */
@Provider
@Produces("message/http")
@Consumes("message/http")
public class HttpResponseProvider implements MessageBodyReader<HttpResponseBean>,
		MessageBodyWriter<HttpResponseBean> {

	@Context
	protected Providers providers;

	/**
	 * Constructor.
	 */
	public HttpResponseProvider() {
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type,
	 *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return HttpResponseBean.class.isAssignableFrom(type);
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type,
	 *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return HttpResponseBean.class.isAssignableFrom(type);
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object, java.lang.Class,
	 *      java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public long getSize(HttpResponseBean t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class,
	 *      java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType,
	 *      javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void writeTo(HttpResponseBean t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {

		Object entity = t.getBody();
		Class<?> entityType = entity.getClass();
		MessageBodyWriter entityWriter = providers.getMessageBodyWriter(entityType, null, null,
				t.getBodyType());
		long size = entityWriter.getSize(entity, entityType, null, null, t.getBodyType());
		if (size > -1) {
			t.setHeader(HttpHeaderNames.CONTENT_LENGTH, Integer.toString((int) size));
		}
		t.setHeader("Content-Classname", entityType.getName()); //$NON-NLS-1$

		PrintWriter writer = new PrintWriter(entityStream);
		writer.print("HTTP/1.1 "); //$NON-NLS-1$
		writer.print(t.getCode());
		writer.print(" "); //$NON-NLS-1$
		writer.println(t.getStatus());
		for (Entry<String, String> entry : t.getHeaders().entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			writer.print(name);
			writer.print(": "); //$NON-NLS-1$
			writer.println(value);
		}
		writer.println(""); //$NON-NLS-1$
		writer.flush();

		entityWriter.writeTo(entity, entityType, null, null, t.getBodyType(), null, entityStream);
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type,
	 *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
	 *      java.io.InputStream)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public HttpResponseBean readFrom(Class<HttpResponseBean> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException, WebApplicationException {
		String httpStuff = consumeHttpHeaders(entityStream);
		BufferedReader reader = new BufferedReader(new StringReader(httpStuff));

		// Read the prolog: "HTTP/1.1 201 Created"
		String line1 = reader.readLine();
		if (!line1.startsWith("HTTP/1.1")) { //$NON-NLS-1$
			throw new IOException(Messages.i18n.format("MISSING_HTTP_PROLOG")); //$NON-NLS-1$
		}
		int idx1 = line1.indexOf(' ');
		int idx2 = line1.indexOf(' ', idx1 + 1);
		int code = Integer.valueOf(line1.substring(idx1 + 1, idx2));
		String status = line1.substring(idx2 + 1);

		HttpResponseBean rval = new HttpResponseBean(code, status);

		// Now read the headers.
		String line = reader.readLine();
		while (line != null && !"".equals(line)) { //$NON-NLS-1$
			int idx = line.indexOf(':');
			String key = line.substring(0, idx).trim();
			String val = line.substring(idx + 1).trim();
			rval.setHeader(key, val);
			line = reader.readLine();
		}

		// Now read the body, using the content-type header to determine the provider to use
		String contentType = rval.getHeaders().get("Content-Type"); //$NON-NLS-1$
		String contentClassName = rval.getHeaders().get("Content-Classname"); //$NON-NLS-1$
		Class<?> entityClass = String.class;
		try {
			if (contentClassName != null)
				entityClass = Class.forName(contentClassName);
		} catch (ClassNotFoundException e) {
		}
		MediaType bodyMediaType = MediaType.valueOf(contentType);
		MessageBodyReader entityReader = providers.getMessageBodyReader(entityClass,
				null, null, bodyMediaType);
		Object bodyEntity = entityReader.readFrom(entityClass,
				null, null, bodyMediaType, null, entityStream);
		rval.setBody(bodyEntity, bodyMediaType);

		return rval;
	}

	/**
	 * Read all of the HTTP header information (including the HTTP/ prolog) from the
	 * stream and return that content as a String.  When this method returns, the stream
	 * should be properly positioned to read the body entity.
	 * @param entityStream the entity stream
	 * @return a string of all the http headers (and first line prolog)
	 * @throws IOException
	 */
	private String consumeHttpHeaders(InputStream entityStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int newlinecount = 0;
		while (newlinecount != 2) {
			int b = entityStream.read();
			if (b == '\n') {
				newlinecount++;
			} else if (b == '\r') {
				// ignore
			} else {
				newlinecount = 0;
			}
			baos.write(b);
		}
		return new String(baos.toByteArray(), "UTF-8"); //$NON-NLS-1$
	}

}
