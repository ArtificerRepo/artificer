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
package org.artificer.server.atom.services;

import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.atom.mappers.OntologyToRdfMapper;
import org.artificer.atom.mappers.RdfToOntologyMapper;
import org.artificer.common.MediaType;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.server.OntologyServiceImpl;
import org.artificer.server.core.api.OntologyService;
import org.artificer.server.i18n.Messages;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.net.URI;
import java.util.Date;
import java.util.List;

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
public class OntologyResource extends AbstractResource {

	private static Logger logger = LoggerFactory.getLogger(OntologyResource.class);

	private static OntologyToRdfMapper o2rdf = new OntologyToRdfMapper();
	private static RdfToOntologyMapper rdf2o = new RdfToOntologyMapper();

    private final OntologyService ontologyService = new OntologyServiceImpl();

    /**
     * S-RAMP atom POST to add an ontology to the repository.
     * @param rdf
     * @throws org.artificer.atom.err.ArtificerAtomException
     */
    @POST
    @Path("ontology")
    @Consumes(MediaType.APPLICATION_RDF_XML)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry create(RDF rdf) throws ArtificerServerException {
        ArtificerOntology ontology;
        try {
            ontology = new ArtificerOntology();
            rdf2o.map(rdf, ontology);

            ontology = ontologyService.create(ontology);

			RDF responseRDF = new RDF();
			o2rdf.map(ontology, responseRDF);

			return ArtificerAtomUtils.wrapOntology(ontology, responseRDF);
        } catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a
            // unique HTTP response type.
            throw e;
        } catch (Exception e) {
        	logError(logger, Messages.i18n.format("ERROR_CREATING_ONTOLOGY"), e); //$NON-NLS-1$
			throw new ArtificerAtomException(e);
        }
    }

    /**
     * Called to update a single ontology by providing a new OWL RDF document.
     * @param uuid
     * @param rdf
     * @throws org.artificer.atom.err.ArtificerAtomException
     */
    @PUT
    @Path("ontology/{uuid}")
    @Consumes(MediaType.APPLICATION_RDF_XML)
    public void update(@PathParam("uuid") String uuid, RDF rdf) throws ArtificerServerException {
        ArtificerOntology ontology;
        try {
            ontology = new ArtificerOntology();
            rdf2o.map(rdf, ontology);
            ontology.setUuid(uuid);

            ontologyService.update(uuid, ontology);
			
			RDF updatedRDF = new RDF();
            o2rdf.map(ontology, updatedRDF);
        } catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Exception e) {
        	logError(logger, Messages.i18n.format("ERROR_UPDATING_ONTOLOGY", uuid), e); //$NON-NLS-1$
			throw new ArtificerAtomException(e);
        }
    }

    /**
     * Called to get a single ontology by its UUID.  This returns an Atom Entry document
     * wrapping the OWL RDF.
     * @param uuid
     * @throws org.artificer.atom.err.ArtificerAtomException
     */
    @GET
    @Path("ontology/{uuid}")
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry get(@PathParam("uuid") String uuid) throws ArtificerServerException {
    	try {
			ArtificerOntology ontology = ontologyService.get(uuid);

			RDF responseRDF = new RDF();
			o2rdf.map(ontology, responseRDF);

			return ArtificerAtomUtils.wrapOntology(ontology, responseRDF);
        } catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Exception e) {
        	logError(logger, Messages.i18n.format("ERROR_GETTING_ONTOLOGY", uuid), e); //$NON-NLS-1$
			throw new ArtificerAtomException(e);
        }
    }

    /**
     * Called to delete a single s-ramp ontology.
     * @param uuid
     * @throws org.artificer.atom.err.ArtificerAtomException
     */
    @DELETE
    @Path("ontology/{uuid}")
	public void delete(@PathParam("uuid") String uuid) throws ArtificerServerException {
    	try {
    	    ontologyService.delete(uuid);
        } catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Exception e) {
        	logError(logger, Messages.i18n.format("ERROR_DELETING_ONTOLOGY", uuid), e); //$NON-NLS-1$
			throw new ArtificerAtomException(e);
        }
    }

	/**
	 * Gets a feed of ontologies.
	 * @throws Exception
	 */
	@GET
	@Path("ontology")
	@Produces(MediaType.APPLICATION_ATOM_XML_FEED)
	public Feed list() throws ArtificerAtomException {
    	try {
			List<ArtificerOntology> ontologies = ontologyService.get();

			Feed feed = new Feed();
			feed.setTitle("S-RAMP Ontology Feed"); //$NON-NLS-1$
			feed.setUpdated(new Date());

			for (ArtificerOntology ontology : ontologies) {
		    	Entry entry = ArtificerAtomUtils.wrapOntology(ontology, null);
				Source source = new Source();
				source.setBase(new URI(ontology.getBase()));
				source.setId(new URI(ontology.getId()));
				entry.setSource(source);

				feed.getEntries().add(entry);
			}

			return feed;
        } catch (Exception e) {
        	logError(logger, Messages.i18n.format("ERROR_GETTING_ONTOLOGIES"), e); //$NON-NLS-1$
			throw new ArtificerAtomException(e);
        }
	}

}
