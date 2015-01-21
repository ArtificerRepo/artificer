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
package org.overlord.sramp.server.atom.services;

import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.common.SrampConfig;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.Set;

/**
 * A jax-rs implementation that handles all s-ramp feeds. There are a number of feeds described by the s-ramp
 * specification, including (but not limited to) the following:
 *
 * <ul>
 *   <li>Artifacts</li>
 *   <li>Relationships</li>
 *   <li>Properties</li>
 * </ul>
 */
@Path("/s-ramp")
public class FeedResource extends AbstractFeedResource {

	/**
	 * Constructor.
	 */
	public FeedResource() {
	}

	/**
	 * Gets a feed of artifacts.
	 */
	@GET
	@Path("{model}")
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed getArtifactFeed(
			@Context HttpServletRequest request,
			@PathParam("model") String model,
			@QueryParam("startPage") Integer startPage,
			@QueryParam("startIndex") Integer startIndex,
			@QueryParam("count") Integer count,
			@QueryParam("orderBy") String orderBy,
			@QueryParam("ascending") Boolean asc,
			@QueryParam("propertyName") Set<String> propNames) throws Exception {
		String xpath = String.format("/s-ramp/%1$s", model); //$NON-NLS-1$
		String baseUrl = SrampConfig.getBaseUrl(request.getRequestURL().toString());
		return createArtifactFeed(xpath, startPage, startIndex, count, orderBy, asc, propNames, baseUrl);
	}

	/**
	 * Gets a feed of artifacts.
	 */
	@GET
	@Path("{model}/{type}")
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed getArtifactFeed(
			@Context HttpServletRequest request,
			@PathParam("model") String model,
			@PathParam("type") String type,
			@QueryParam("startPage") Integer startPage,
			@QueryParam("startIndex") Integer startIndex,
			@QueryParam("count") Integer count,
			@QueryParam("orderBy") String orderBy,
			@QueryParam("ascending") Boolean asc,
			@QueryParam("propertyName") Set<String> propNames) throws Exception {
		String xpath = String.format("/s-ramp/%1$s/%2$s", model, type); //$NON-NLS-1$
		String baseUrl = SrampConfig.getBaseUrl(request.getRequestURL().toString());
		return createArtifactFeed(xpath, startPage, startIndex, count, orderBy, asc, propNames, baseUrl);
	}

}
