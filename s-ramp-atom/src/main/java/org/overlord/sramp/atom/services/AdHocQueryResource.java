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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.QueryManagerFactory;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;


/**
 * Implementation of the S-RAMP Ad-Hoc query features defined in the Atom Binding document (section 3).
 */
@Path("/s-ramp")
public class AdHocQueryResource {
	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed queryFromGet(@QueryParam("query") String query, @Context UriInfo uri) throws Exception {
//		MultivaluedMap<String,String> queryParameters = uri.getQueryParameters();
//		System.out.println(queryParameters);
		return query(query);
	}

	/**
	 * Handles clients that POST the query to the /s-ramp endpoint.
	 * @param input the multipart form data 
	 * @return
	 * @throws Exception
	 */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed queryFromPost(MultipartFormDataInput input) throws Exception {
    	String query = input.getFormDataPart("query", new GenericType<String>() { });
    	return query(query);
    }

    /**
     * Common method that performs the query and returns the atom {@link Feed}.
     * @param query the query
     * @return an Atom {@link Feed}
     * @throws Exception
     */
    protected Feed query(String query) throws Exception {
		if (query == null)
			throw new IllegalArgumentException("Missing S-RAMP query (param with name 'query').");

		String xpath = query;
		if (!xpath.startsWith("/s-ramp")) {
			if (query.startsWith("/"))
				xpath = "/s-ramp" + query;
			else
				xpath = "/s-ramp/" + query;
		}

		QueryManager queryManager = QueryManagerFactory.newInstance();
		SrampQuery srampQuery = queryManager.createQuery(xpath);
		ArtifactSet artifactSet = null;
		
		try {
			artifactSet = srampQuery.executeQuery();
			Feed feed = createFeed(artifactSet);
			return feed;
		} finally {
			if (artifactSet != null)
				artifactSet.close();
		}
    }
    
	/**
	 * Creates the Atom {@link Feed} from the given artifact set (query result set).
	 * @param artifactSet the set of artifacts that matched the query
	 * @return an Atom {@link Feed}
	 * @throws URISyntaxException 
	 */
	private Feed createFeed(ArtifactSet artifactSet) throws URISyntaxException {
		Feed feed = new Feed();
		feed.setId(new URI(UUID.randomUUID().toString()));
		feed.setTitle("S-RAMP Feed");
		feed.setSubtitle("Ad Hoc query feed");
		feed.setUpdated(new Date());
		feed.getAuthors().add(new Person("anonymous"));

		for (BaseArtifactType artifact : artifactSet) {
			Entry entry = new Entry();
			entry.setId(new URI(artifact.getUuid()));
			entry.setTitle(artifact.getName());
			entry.setUpdated(artifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
			entry.setPublished(artifact.getCreatedTimestamp().toGregorianCalendar().getTime());
			feed.getEntries().add(entry);
		}

		return feed;
	}

}
