/*
 * Copyright 2013 JBoss Inc
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
package org.artificer.ui.server.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.artificer.atom.mappers.OntologyToRdfMapper;
import org.artificer.atom.mappers.RdfToOntologyMapper;
import org.artificer.client.ontology.OntologySummary;
import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.common.ontology.ArtificerOntologyClass;
import org.artificer.ui.client.shared.beans.OntologySummaryBean;
import org.artificer.ui.client.shared.services.IOntologyService;
import org.artificer.ui.client.shared.beans.OntologyBean;
import org.artificer.ui.client.shared.beans.OntologyClassBean;
import org.artificer.ui.client.shared.beans.OntologyResultSetBean;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;
import org.artificer.ui.server.api.ArtificerApiClientAccessor;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

/**
 * Concrete implementation of the ontology service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class OntologyService implements IOntologyService {

    private static OntologyToRdfMapper o2rdf = new OntologyToRdfMapper();

    /**
     * Constructor.
     */
    public OntologyService() {
    }

    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#get(java.lang.String)
     */
    @Override
    public OntologyBean get(String uuid) throws ArtificerUiException {
        try {
            RDF rdf = ArtificerApiClientAccessor.getClient().getOntology(uuid);
            ArtificerOntology ontology = RdfToOntologyMapper.rdf2ontology(rdf);
            OntologyBean bean = ontologyToBean(ontology);
            return bean;
        } catch (Exception e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#list()
     */
    @Override
    public OntologyResultSetBean list() throws ArtificerUiException {
        try {
            OntologyResultSetBean rval = new OntologyResultSetBean();
            List<OntologySummaryBean> ontologyBeans = new ArrayList<OntologySummaryBean>();
            List<OntologySummary> ontologies = ArtificerApiClientAccessor.getClient().getOntologies();
            for (OntologySummary ontologySummary : ontologies) {
                ontologyBeans.add(ontologySummaryToBean(ontologySummary));
            }
            rval.setOntologies(ontologyBeans);
            return rval;
        } catch (Exception e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }
    
    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#add(org.artificer.ui.client.shared.beans.OntologyBean)
     */
    @Override
    public void add(OntologyBean ontology) throws ArtificerUiException {
        try {
            RDF rdf = ontologyBeanToRDF(ontology);
            ArtificerApiClientAccessor.getClient().addOntology(rdf);
        } catch (Exception e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#update(org.artificer.ui.client.shared.beans.OntologyBean)
     */
    @Override
    public void update(OntologyBean ontology) throws ArtificerUiException {
        try {
            RDF rdf = ontologyBeanToRDF(ontology);
            ArtificerApiClientAccessor.getClient().updateOntology(ontology.getUuid(), rdf);
        } catch (Exception e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#delete(String)
     */
    @Override
    public void delete(String uuid) throws ArtificerUiException {
        try {
            ArtificerApiClientAccessor.getClient().deleteOntology(uuid);
        } catch (Exception e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    /**
     * Converts an ontology into an {@link OntologyBean}.
     * @param ontology
     */
    private OntologyBean ontologyToBean(ArtificerOntology ontology) {
        OntologyBean bean = new OntologyBean();
        bean.setLastModifiedBy(ontology.getLastModifiedBy());
        bean.setBase(ontology.getBase());
        bean.setComment(ontology.getComment());
        bean.setCreatedBy(ontology.getCreatedBy());
        bean.setCreatedOn(ontology.getCreatedOn());
        bean.setId(ontology.getId());
        bean.setLabel(ontology.getLabel());
        bean.setLastModifiedBy(ontology.getLastModifiedBy());
        bean.setLastModifiedOn(ontology.getLastModifiedOn());
        bean.setUuid(ontology.getUuid());
        List<ArtificerOntologyClass> allClasses = ontology.getAllClasses();
        
        // Create and index all the classes first
        Map<String, OntologyClassBean> classIndexById = new HashMap<String, OntologyClassBean>();
        for (ArtificerOntologyClass cl4ss : allClasses) {
            OntologyClassBean classBean = bean.createClass(cl4ss.getId());
            classIndexById.put(cl4ss.getId(), classBean);
            classBean.setComment(cl4ss.getComment());
            classBean.setLabel(cl4ss.getLabel());
        }
        // Then go back through and set up the tree.
        for (ArtificerOntologyClass cl4ss : allClasses) {
            OntologyClassBean classBean = classIndexById.get(cl4ss.getId());
            if (cl4ss.getParent() != null) {
                OntologyClassBean parentBean = classIndexById.get(cl4ss.getParent().getId());
                if (parentBean != null) {
                    parentBean.getChildren().add(classBean);
                }
            } else {
                bean.getRootClasses().add(classBean);
            }
        }
        return bean;
    }

    /**
     * Converts an ontology summary to an {@link OntologySummaryBean}.
     * @param ontologySummary
     */
    protected OntologySummaryBean ontologySummaryToBean(OntologySummary ontologySummary) {
        OntologySummaryBean bean = new OntologySummaryBean();
        bean.setBase(ontologySummary.getBase());
        bean.setComment(ontologySummary.getComment());
        bean.setCreatedBy(ontologySummary.getCreatedBy());
        bean.setCreatedOn(ontologySummary.getCreatedTimestamp());
        bean.setId(ontologySummary.getId());
        bean.setLabel(ontologySummary.getLabel());
        bean.setLastModifiedOn(ontologySummary.getLastModifiedTimestamp());
        bean.setUuid(ontologySummary.getUuid());
        return bean;
    }

    /**
     * Converts an ontology bean into an RDF.
     * @param ontology
     */
    private RDF ontologyBeanToRDF(OntologyBean ontology) {
        ArtificerOntology sontology = new ArtificerOntology();
        sontology.setBase(ontology.getBase());
        sontology.setId(ontology.getId());
        sontology.setLabel(ontology.getLabel());
        sontology.setComment(ontology.getComment());
        sontology.setUuid(ontology.getUuid());
        
        List<ArtificerOntologyClass> srootClasses = new ArrayList<ArtificerOntologyClass>();
        for (OntologyClassBean ontologyClass : ontology.getRootClasses()) {
            ArtificerOntologyClass c = sontology.createClass(ontologyClass.getId());
            copyOntologyClass(sontology, ontologyClass, c);
            srootClasses.add(c);
        }
        sontology.setRootClasses(srootClasses);
        
        RDF rdf = new RDF();
        o2rdf.map(sontology, rdf);
        return rdf;
    }

    /**
     * Copies the ontology class.
     * @param sontology 
     * @param from
     * @param to
     */
    private void copyOntologyClass(ArtificerOntology sontology, OntologyClassBean from, ArtificerOntologyClass to) {
        to.setComment(from.getComment());
        to.setLabel(from.getLabel());
        
        List<ArtificerOntologyClass> schildren = new ArrayList<ArtificerOntologyClass>();
        for (OntologyClassBean child : from.getChildren()) {
            ArtificerOntologyClass c = sontology.createClass(child.getId());
            copyOntologyClass(sontology, child, c);
            c.setParent(to);
            schildren.add(c);
        }
        to.setChildren(schildren);

    }
    
}
