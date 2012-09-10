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
package org.overlord.sramp.atom.services;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;


/**
 * Implementation of the S-RAMP query features defined in the Atom Binding document.
 */
@Path("/s-ramp")
public class QueryResource extends AbstractFeedResource {

	/**
	 * Constructor.
	 */
	public QueryResource() {
	}

	/**
	 * Do an s-ramp query from a GET style request.
	 * @param uri
	 * @param query
	 * @param page
	 * @param pageSize
	 * @param orderBy
	 * @param asc
	 * @throws SrampAtomException
	 */
	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed queryFromGet(
			@Context UriInfo uri,
			@QueryParam("query") String query,
			@QueryParam("page") Integer page,
			@QueryParam("pageSize") Integer pageSize,
			@QueryParam("orderBy") String orderBy,
			@QueryParam("ascending") Boolean asc,
			@QueryParam("propertyName") Set<String> propNames) throws SrampAtomException {
		try {
			return query(query, page, pageSize, orderBy, asc, propNames);
		} catch (Throwable e) {
			throw new SrampAtomException(e);
		}
	}

	/**
	 * Handles clients that POST the query to the /s-ramp endpoint.
	 * @param input the multipart form data
	 * @throws SrampAtomException
	 */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed queryFromPost(MultipartFormDataInput input) throws SrampAtomException {
    	try {
			String query = input.getFormDataPart("query", new GenericType<String>() { });
			Integer page = input.getFormDataPart("page", new GenericType<Integer>() { });
			Integer pageSize = input.getFormDataPart("pageSize", new GenericType<Integer>() { });
			String orderBy = input.getFormDataPart("orderBy", new GenericType<String>() { });
			Boolean asc = input.getFormDataPart("ascending", new GenericType<Boolean>() { });
			Set<String> propNames = input.getFormDataPart("propertyName", new GenericType<Set<String>>() { });
			return query(query, page, pageSize, orderBy, asc, propNames);
		} catch (SrampAtomException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampAtomException(e);
		}
    }

    /**
     * Common method that performs the query and returns the atom {@link Feed}.
     * @param query the x-path formatted s-ramp query
     * @param page which page in the results should be returned
     * @param pageSize the size of each page of results
     * @param orderBy the property to sort the results by
     * @param ascending the sort direction
     * @param propNames the set of s-ramp property names - the extra properties that the query should return as part of the {@link Feed}
     * @return an Atom {@link Feed}
     * @throws SrampAtomException
     */
    protected Feed query(String query, Integer page, Integer pageSize, String orderBy, Boolean ascending, Set<String> propNames) throws SrampAtomException {
		if (query == null)
			throw new SrampAtomException("Missing S-RAMP query (param with name 'query').");

		// Add on the "/s-ramp/" if it's missing
		String xpath = query;
		if (!xpath.startsWith("/s-ramp")) {
			if (query.startsWith("/"))
				xpath = "/s-ramp" + query;
			else
				xpath = "/s-ramp/" + query;
		}

		return createArtifactFeed(xpath, page, pageSize, orderBy, ascending, propNames);
    }
}
