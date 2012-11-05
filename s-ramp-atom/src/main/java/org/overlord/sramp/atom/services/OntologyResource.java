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

import java.net.URI;
import java.util.Date;
import java.util.List;

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
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.jboss.resteasy.plugins.providers.atom.Source;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.mappers.OntologyToRdfMapper;
import org.overlord.sramp.atom.mappers.RdfToOntologyMapper;
import org.overlord.sramp.ontology.SrampOntology;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
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

	private static OntologyToRdfMapper o2rdf = new OntologyToRdfMapper();
	private static RdfToOntologyMapper rdf2o = new RdfToOntologyMapper();

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
    @Consumes(MediaType.APPLICATION_RDF_XML)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry create(RDF rdf) throws SrampAtomException {
    	try {
			SrampOntology ontology = new SrampOntology();
			rdf2o.map(rdf, ontology);

			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			ontology = persistenceManager.persistOntology(ontology);

			RDF responseRDF = new RDF();
			o2rdf.map(ontology, responseRDF);

			Entry entry = new Entry();
			entry.setId(new URI(ontology.getUuid()));
			entry.setPublished(ontology.getCreatedOn());
			entry.setUpdated(ontology.getLastModifiedOn());
			entry.getAuthors().add(new Person(ontology.getCreatedBy()));
			entry.setTitle(ontology.getLabel());
			entry.setSummary(ontology.getComment());

			entry.setAnyOtherJAXBObject(responseRDF);
			return entry;
        } catch (Exception e) {
			throw new SrampAtomException(e);
        }
    }

    /**
     * Called to update a single ontology by providing a new OWL RDF document.
     * @param uuid
     * @param rdf
     * @throws SrampAtomException
     */
    @PUT
    @Path("ontology/{uuid}")
    @Consumes(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public void update(@PathParam("uuid") String uuid, Entry entry) throws SrampAtomException {
    	try {
    		RDF rdf = SrampAtomUtils.unwrap(entry, RDF.class);
			SrampOntology ontology = new SrampOntology();
			ontology.setUuid(uuid);
			rdf2o.map(rdf, ontology);

			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			persistenceManager.updateOntology(ontology);
        } catch (Exception e) {
			throw new SrampAtomException(e);
        }
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
    	try {
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			SrampOntology ontology = persistenceManager.getOntology(uuid);

			RDF responseRDF = new RDF();
			o2rdf.map(ontology, responseRDF);

			Entry entry = new Entry();
			entry.setId(new URI(ontology.getUuid()));
			entry.setPublished(ontology.getCreatedOn());
			entry.setUpdated(ontology.getLastModifiedOn());
			entry.getAuthors().add(new Person(ontology.getCreatedBy()));
			entry.setTitle(ontology.getLabel());
			entry.setSummary(ontology.getComment());
			entry.setAnyOtherJAXBObject(responseRDF);
			return entry;
        } catch (Exception e) {
			throw new SrampAtomException(e);
        }
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
    	try {
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			persistenceManager.deleteOntology(uuid);
        } catch (Exception e) {
			throw new SrampAtomException(e);
        }
    }

	/**
	 * Gets a feed of ontologies.
	 * @param uri
	 * @throws Exception
	 */
	@GET
	@Path("ontology")
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed list() throws SrampAtomException {
    	try {
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			List<SrampOntology> ontologies = persistenceManager.getOntologies();

			Feed feed = new Feed();
			feed.setTitle("S-RAMP ontology feed");
			feed.setUpdated(new Date());

			for (SrampOntology ontology : ontologies) {
		    	Entry entry = new Entry();
				entry.setId(new URI(ontology.getUuid()));
				entry.setPublished(ontology.getCreatedOn());
				entry.setUpdated(ontology.getLastModifiedOn());
				entry.getAuthors().add(new Person(ontology.getCreatedBy()));
				entry.setTitle(ontology.getLabel());
				entry.setSummary(ontology.getComment());
				Source source = new Source();
				source.setBase(new URI(ontology.getBase()));
				source.setId(new URI(ontology.getId()));
				entry.setSource(source);

				feed.getEntries().add(entry);
			}

			return feed;
        } catch (Exception e) {
			throw new SrampAtomException(e);
        }
	}

}
