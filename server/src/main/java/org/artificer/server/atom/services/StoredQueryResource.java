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
package org.artificer.server.atom.services;

import org.artificer.atom.ArtificerAtomConstants;
import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.common.ArtificerConfig;
import org.artificer.common.MediaType;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.server.i18n.Messages;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * A JAX-RS resource that provides Stored Query support.
 *
 * @author Brett Meyer
 */
@Path("/s-ramp/query")
public class StoredQueryResource extends AbstractFeedResource {

	private static Logger logger = LoggerFactory.getLogger(StoredQueryResource.class);

	public StoredQueryResource() {
	}

	@POST
    @Consumes(MediaType.APPLICATION_ATOM_XML_ENTRY)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public Entry create(@Context HttpServletRequest request, Entry atomEntry) throws ArtificerServerException {
        try {
            String baseUrl = ArtificerConfig.getBaseUrl(request.getRequestURL().toString());
            StoredQuery storedQuery = ArtificerAtomUtils.unwrapStoredQuery(atomEntry);
            storedQuery = queryService.createStoredQuery(storedQuery);

            return wrapStoredQuery(storedQuery, baseUrl);
        } catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a
            // unique HTTP response type.
            throw e;
        } catch (Exception e) {
            logError(logger, Messages.i18n.format("ERROR_CREATING_STOREDQUERY"), e);
            throw new ArtificerAtomException(e);
        }
    }

	@PUT
	@Path("{queryName}")
	@Consumes(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public void update(@PathParam("queryName") String queryName, Entry atomEntry) throws ArtificerServerException {
		try {
		    StoredQuery storedQuery = ArtificerAtomUtils.unwrapStoredQuery(atomEntry);
            queryService.updateStoredQuery(queryName, storedQuery);
		} catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Throwable e) {
			logError(logger, Messages.i18n.format("ERROR_UPDATING_STOREDQUERY", queryName), e);
			throw new ArtificerAtomException(e);
		}
	}

	@GET
	@Path("{queryName}")
	@Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry get(@Context HttpServletRequest request, @PathParam("queryName") String queryName)
	        throws ArtificerServerException {
		try {
		    String baseUrl = ArtificerConfig.getBaseUrl(request.getRequestURL().toString());
            StoredQuery storedQuery = queryService.getStoredQuery(queryName);

            return wrapStoredQuery(storedQuery, baseUrl);
		} catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Throwable e) {
			logError(logger, Messages.i18n.format("ERROR_GETTING_STOREDQUERY", queryName), e);
			throw new ArtificerAtomException(e);
		}
	}

    @GET
    @Produces(MediaType.APPLICATION_ATOM_XML_FEED)
    public Feed list(@Context HttpServletRequest request) throws ArtificerAtomException {
        try {
            String baseUrl = ArtificerConfig.getBaseUrl(request.getRequestURL().toString());
            List<StoredQuery> storedQueries = queryService.getStoredQueries();

            Feed feed = new Feed();
            feed.setTitle("S-RAMP Stored Queries Feed");
            feed.setUpdated(new Date());

            for (StoredQuery storedQuery : storedQueries) {
                feed.getEntries().add(wrapStoredQuery(storedQuery, baseUrl));
            }

            return feed;
        } catch (Exception e) {
            logError(logger, Messages.i18n.format("ERROR_GETTING_STOREDQUERIES"), e);
            throw new ArtificerAtomException(e);
        }
    }

    @GET
    @Path("{queryName}/results")
    @Produces(MediaType.APPLICATION_ATOM_XML_FEED)
    public Feed getResults(@Context HttpServletRequest request,
            @PathParam("queryName") String queryName,
            @QueryParam("startPage") Integer startPage,
            @QueryParam("startIndex") Integer startIndex,
            @QueryParam("count") Integer count,
            @QueryParam("orderBy") String orderBy,
            @QueryParam("ascending") Boolean asc)
            throws ArtificerServerException {
        try {
            String baseUrl = ArtificerConfig.getBaseUrl(request.getRequestURL().toString());
            StoredQuery storedQuery = queryService.getStoredQuery(queryName);
            
            // TODO: It may be possible to introduce certain optimizations...
            
            return createArtifactFeed(storedQuery.getQueryExpression(), startPage, startIndex, count, orderBy, asc,
                    new HashSet<String>(storedQuery.getPropertyName()), baseUrl);
        } catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Throwable e) {
            logError(logger, Messages.i18n.format("ERROR_EXECUTING_STOREDQUERY", queryName), e);
            throw new ArtificerAtomException(e);
        }
    }

	@DELETE
	@Path("{queryName}")
	public void delete(@PathParam("queryName") String queryName) throws ArtificerServerException {
		try {
            queryService.deleteStoredQuery(queryName);
		} catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Throwable e) {
			logError(logger, Messages.i18n.format("ERROR_DELETING_STOREDQUERY", queryName), e);
			throw new ArtificerAtomException(e);
		}
	}
    
    private Entry wrapStoredQuery(StoredQuery storedQuery, String baseUrl) throws Exception {
        Entry entry = ArtificerAtomUtils.wrapStoredQuery(storedQuery);
        // TODO
//        entry.setPublished();
//        entry.setUpdated();
        
        String atomLink = baseUrl + "/s-ramp/query/" + storedQuery.getQueryName();
        
        // self link
        Link linkToSelf = new Link();
        linkToSelf.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        linkToSelf.setRel("self");
        linkToSelf.setHref(new URI(atomLink));
        entry.getLinks().add(linkToSelf);
        
        // edit link
        Link linkToEdit = new Link();
        linkToEdit.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        linkToEdit.setRel("edit");
        linkToEdit.setHref(new URI(atomLink));
        entry.getLinks().add(linkToEdit);
        
        // results link
        // Note: The spec technically requires this for a POST, but it seems useful in other contexts.
        Link linkToResults = new Link();
        linkToResults.setType(MediaType.APPLICATION_ATOM_XML_FEED_TYPE);
        linkToResults.setRel(ArtificerAtomConstants.X_S_RAMP_QUERY_RESULTS);
        linkToResults.setHref(new URI(atomLink + "/results"));
        entry.getLinks().add(linkToResults);
        
        return entry;
    }
}
