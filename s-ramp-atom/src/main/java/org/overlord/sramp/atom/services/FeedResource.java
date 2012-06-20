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
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;

/**
 * S-RAMP implementations SHALL return an Atom Publishing Protocol Service
 * Document to clients who perform an HTTP GET on the following URL:</br></br>
 * <code>
 * {base URL}/s-ramp/servicedocument
 * </code> </br></br>
 * 
 * The content of the Service Document that is returned is defined as follows:
 * <ul>
 * <li>MUST contain a workspace for each of the artifact models identified in
 * Section 3 of the SOA Repository Artifact Model & Protocol Specification -
 * Foundation Document.</li>
 * <li>Each workspace MUST contain an app:collection element for each of the
 * artifact types that are defined within the corresponding artifact model for
 * that workspace.
 * <li>Each collection in a workspace MUST specify an atom:categories element
 * that will define the categories that MUST be applied to the member resources
 * of the collection as defined in Section 2.3.1.</li>
 * <li>The workspace for the query artifact model MUST contain an app:collection
 * element for each Stored Query that exists in the S-RAMP implementation.</li>
 * <li>The workspace for the SOA or Service Implementation Artifact Model MUST
 * contain an app:collection element for each user defined type that has been
 * registered in the S-RAMP implementation.</li>
 * </ul>
 */
@Path("/s-ramp")
public class FeedResource
{
    @GET
    @Path("feed")
    @Produces(MediaType.APPLICATION_ATOM_XML)
    
    public Feed getFeed(@Context UriInfo uri) throws Exception
    {
       Feed feed = new Feed();
       feed.setId(new URI("tag:example.org,2007:/foo"));
       feed.setTitle("Test Feed");
       feed.setSubtitle("Feed subtitle");
       feed.setUpdated(new Date());
       feed.getAuthors().add(new Person("James Snell"));
       feed.getLinks().add(new Link("","http://example.com"));


       Entry entry = new Entry();
       entry.setId(new URI("tag:example.org,2007:/foo/entries/1"));
       entry.setTitle("Entry title");
       entry.setUpdated(new Date());
       entry.setPublished(new Date());
       entry.getLinks().add(new Link("", uri.getRequestUri().toString()));
       feed.getEntries().add(entry);

       return feed;

    }

    
  
}
