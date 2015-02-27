/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.atom.providers;

import org.artificer.common.error.ArtificerNotFoundException;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * The spec requires that a 404 is returned when an artifact is not found.
 * 
 * @author Brett Meyer
 */
@Provider
@ServerInterceptor
@Produces(org.artificer.common.MediaType.APPLICATION_ARTIFICER_NOTFOUND_EXCEPTION)
@Consumes(org.artificer.common.MediaType.APPLICATION_ARTIFICER_NOTFOUND_EXCEPTION)
public class ArtificerNotFoundExceptionProvider extends AbstractArtificerExceptionProvider
        implements ExceptionMapper<ArtificerNotFoundException>, MessageBodyWriter<ArtificerNotFoundException>,
        MessageBodyReader<ArtificerNotFoundException> {

    /**
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
     */
    @Override
    public Response toResponse(ArtificerNotFoundException exception) {
        return super.toResponse(exception, Response.Status.NOT_FOUND,
                org.artificer.common.MediaType.APPLICATION_ARTIFICER_NOTFOUND_EXCEPTION);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType) {
        return ArtificerNotFoundException.class.equals(type);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public long getSize(ArtificerNotFoundException t, Class<?> type, Type genericType, Annotation[] annotations,
                        javax.ws.rs.core.MediaType mediaType) {
        return -1l;
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
     */
    @Override
    public void writeTo(ArtificerNotFoundException error, Class<?> type, Type genericType, Annotation[] annotations,
                        javax.ws.rs.core.MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        super.writeTo(error, httpHeaders, entityStream);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
                              javax.ws.rs.core.MediaType mediaType) {
        return ArtificerNotFoundException.class.isAssignableFrom(type)
                || mediaType.equals(org.artificer.common.MediaType.APPLICATION_ARTIFICER_NOTFOUND_EXCEPTION_TYPE);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
     */
    @Override
    public ArtificerNotFoundException readFrom(Class<ArtificerNotFoundException> type, Type genericType, Annotation[] annotations,
                                             javax.ws.rs.core.MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        String stackTrace = getStacktrace(entityStream);
        String msg = getMessage(httpHeaders);
        return new ArtificerNotFoundException(msg, stackTrace);
    }

}
