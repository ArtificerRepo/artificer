/*
 * Copyright 2011 JBoss Inc
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
package org.artificer.server.atom.services;

import org.artificer.common.error.ArtificerServerException;
import org.artificer.server.i18n.Messages;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;
import org.artificer.common.MediaType;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.common.ArtificerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Implementation of the S-RAMP query features defined in the Atom Binding document.
 */
@Path("/s-ramp")
public class QueryResource extends AbstractFeedResource {

	private static Logger logger = LoggerFactory.getLogger(QueryResource.class);

	/**
	 * Constructor.
	 */
	public QueryResource() {
	}

	/**
	 * Do an s-ramp query from a GET style request.
	 * @param query
	 * @param startPage
	 * @param startIndex
	 * @param count
	 * @param orderBy
	 * @param asc
	 * @throws org.artificer.atom.err.ArtificerAtomException
	 */
	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed queryFromGet(
			@Context HttpServletRequest request,
			@QueryParam("query") String query,
			@QueryParam("startPage") Integer startPage,
			@QueryParam("startIndex") Integer startIndex,
			@QueryParam("count") Integer count,
			@QueryParam("orderBy") String orderBy,
			@QueryParam("ascending") Boolean asc,
			@QueryParam("propertyName") Set<String> propNames) throws ArtificerAtomException {
		try {
			String baseUrl = ArtificerConfig.getBaseUrl(request.getRequestURL().toString());
			return createArtifactFeed(query, startPage, startIndex, count, orderBy, asc, propNames, baseUrl);
		} catch (Throwable e) {
			logError(logger, Messages.i18n.format("ERROR_EXECUTING_QUERY", query), e);
			throw new ArtificerAtomException(e);
		}
	}

	/**
	 * Handles clients that POST the query to the /s-ramp endpoint.
	 * @param input the multipart form data
	 * @throws org.artificer.atom.err.ArtificerAtomException
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed queryFromPost(@Context HttpServletRequest request, MultipartFormDataInput input) throws ArtificerServerException {
		String query = null;
		try {
			String baseUrl = ArtificerConfig.getBaseUrl(request.getRequestURL().toString());
			query = input.getFormDataPart("query", new GenericType<String>() { });
			Integer startPage = input.getFormDataPart("startPage", new GenericType<Integer>() { });
			Integer startIndex = input.getFormDataPart("startIndex", new GenericType<Integer>() { });
			Integer count = input.getFormDataPart("count", new GenericType<Integer>() { });
			String orderBy = input.getFormDataPart("orderBy", new GenericType<String>() { });
			Boolean asc = input.getFormDataPart("ascending", new GenericType<Boolean>() { });
            Set<String> propNames = new HashSet<String>();
			List<InputPart> list = input.getFormDataMap().get("propertyName");
			if (list != null) {
    			for (InputPart inputPart : list) {
    			    propNames.add(inputPart.getBodyAsString());
                }
			}

			return createArtifactFeed(query, startPage, startIndex, count, orderBy, asc, propNames, baseUrl);
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			logError(logger, Messages.i18n.format("ERROR_EXECUTING_QUERY", query), e);
			throw new ArtificerAtomException(e);
		}
	}
}
