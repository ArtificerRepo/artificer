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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.overlord.sramp.atom.err.SrampAtomExceptionMapper;

/**
 * A RESTEasy message body reader that is capable of reading an exception from the
 * response body.
 *
 * TODO merge this and {@link SrampAtomExceptionMapper} - don't need both
 *
 * @author eric.wittmann@redhat.com
 */
@Provider
public class SrampClientExceptionReader implements MessageBodyReader<SrampServerException> {

	/**
	 * Constructor.
	 */
	public SrampClientExceptionReader() {
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SrampServerException.class.equals(type) || org.overlord.sramp.atom.MediaType.APPLICATION_STACKTRACE_TYPE.equals(mediaType);
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
	 */
	@Override
	public SrampServerException readFrom(Class<SrampServerException> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		List<String> lines = IOUtils.readLines(entityStream);
		StringBuilder buffer = new StringBuilder();
		for (String line : lines) {
			buffer.append(line).append("\n");
		}
		String remoteStackTrace = buffer.toString();
		SrampServerException error = new SrampServerException("An unexpected error was thrown by the S-RAMP repository.");
		error.setRemoteStackTrace(remoteStackTrace);
		return error;
	}

}
