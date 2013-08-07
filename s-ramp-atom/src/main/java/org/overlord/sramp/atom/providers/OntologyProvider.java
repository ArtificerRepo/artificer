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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jboss.resteasy.plugins.providers.jaxb.JAXBMarshalException;
import org.jboss.resteasy.plugins.providers.jaxb.JAXBUnmarshalException;
import org.overlord.sramp.atom.i18n.Messages;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

/**
 * A RESTEasy provider for reading/writing an S-RAMP ontology. S-RAMP ontologies are defined using a sub-set
 * of the OWL Lite specification, which in turn builds on RDF.
 *
 * @author eric.wittmann@redhat.com
 */
@Provider
@Produces("application/rdf+xml")
@Consumes("application/rdf+xml")
public class OntologyProvider implements MessageBodyReader<RDF>, MessageBodyWriter<RDF> {

	private static JAXBContext rdfContext;
	{
		try {
			rdfContext = JAXBContext.newInstance(RDF.class);
		} catch (JAXBException e) {
			rdfContext = null;
		}
	}

	/**
	 * Constructor.
	 */
	public OntologyProvider() {
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type,
	 *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return RDF.class.isAssignableFrom(type)
				|| org.overlord.sramp.atom.MediaType.APPLICATION_RDF_XML_TYPE.equals(mediaType);
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type,
	 *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return RDF.class.isAssignableFrom(type)
				|| org.overlord.sramp.atom.MediaType.APPLICATION_RDF_XML_TYPE.equals(mediaType);
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object, java.lang.Class,
	 *      java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public long getSize(RDF t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class,
	 *      java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType,
	 *      javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
	 */
	@Override
	public void writeTo(RDF t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		if (rdfContext == null)
			throw new JAXBMarshalException(Messages.i18n.format("UNABLE_TO_MARSHAL", mediaType), //$NON-NLS-1$
			        new NullPointerException(Messages.i18n.format("FAILED_TO_CREATE_ONT_JAXBCTX"))); //$NON-NLS-1$
		try {
			rdfContext.createMarshaller().marshal(t, entityStream);
		} catch (JAXBException e) {
			throw new JAXBMarshalException(Messages.i18n.format("UNABLE_TO_MARSHAL", mediaType), e); //$NON-NLS-1$
		}
	}

	/**
	 * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type,
	 *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
	 *      java.io.InputStream)
	 */
	@Override
	public RDF readFrom(Class<RDF> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
			WebApplicationException {
		if (rdfContext == null)
			throw new JAXBUnmarshalException(Messages.i18n.format("UNABLE_TO_MARSHAL", mediaType), //$NON-NLS-1$
			        new NullPointerException(Messages.i18n.format("FAILED_TO_CREATE_ONT_JAXBCTX"))); //$NON-NLS-1$
		try {
			RDF entry = (RDF) rdfContext.createUnmarshaller().unmarshal(entityStream);
			return entry;
		} catch (JAXBException e) {
			throw new JAXBUnmarshalException(Messages.i18n.format("UNABLE_TO_MARSHAL")); //$NON-NLS-1$
		}
	}

}
