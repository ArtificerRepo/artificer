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
package org.overlord.sramp.atom.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

/**
 * The JAX-RS resource that handles ontology specific tasks, including:
 *
 * <ul>
 *   <li>Add an ontology (upload)</li>
 *   <li>Get an ontolgy (full Atom Entry containing an Owl {@link RDF})</li>
 *   <li>Update an ontology</li>
 *   <li>Delete an ontology</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
@Path("/s-ramp")
public class OntologyResource {

	/**
	 * Constructor.
	 */
	public OntologyResource() {
	}

    /**
     * S-RAMP atom POST to add an ontology to the repository.
     * @param fileName
     * @param model
     * @param type
     * @param content
     * @throws SrampAtomException
     */
    @POST
    @Path("ontology")
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry create(Entry atomEntry) throws SrampAtomException {
    	throw new SrampAtomException("Not yet implemented.");
    }

    /**
     * Called to update a single ontology by providing a new OWL RDF document wrapped
     * in an Atom Entry.
     * @param model
     * @param type
     * @param uuid
     * @param atomEntry
     * @throws SrampAtomException
     */
    @PUT
    @Path("ontology/{uuid}")
    @Consumes(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public void update(@PathParam("uuid") String uuid, Entry atomEntry) throws SrampAtomException {
    	throw new SrampAtomException("Not yet implemented.");
    }

    /**
     * Called to get a single ontology by its UUID.  This returns an Atom Entry document
     * wrapping the OWL RDF.
     * @param model
     * @param type
     * @param uuid
     * @throws SrampAtomException
     */
    @GET
    @Path("ontology/{uuid}")
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry get(@PathParam("uuid") String uuid) throws SrampAtomException {
    	throw new SrampAtomException("Not yet implemented.");
    }

    /**
     * Called to delete a single s-ramp ontology.
     * @param model
     * @param type
     * @param uuid
     * @throws SrampAtomException
     */
    @DELETE
    @Path("ontology/{uuid}")
	public void delete(@PathParam("uuid") String uuid) throws SrampAtomException {
    	throw new SrampAtomException("Not yet implemented.");
    }

	/**
	 * Gets a feed of ontologies.
	 * @param uri
	 * @throws Exception
	 */
	@GET
	@Path("ontology")
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed getArtifactFeed() throws SrampAtomException {
    	throw new SrampAtomException("Not yet implemented.");
	}

}
