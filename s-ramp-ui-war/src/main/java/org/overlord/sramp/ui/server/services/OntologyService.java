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
package org.overlord.sramp.ui.server.services;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.overlord.sramp.atom.mappers.RdfToOntologyMapper;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.common.ontology.SrampOntology.Class;
import org.overlord.sramp.ui.client.shared.beans.OntologyBean;
import org.overlord.sramp.ui.client.shared.beans.OntologyClassBean;
import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;
import org.overlord.sramp.ui.client.shared.services.IOntologyService;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

/**
 * Concrete implementation of the ontology service.
 *
 * @author eric.wittmann@redhat.com
 */
@Service
public class OntologyService implements IOntologyService {

    @Inject
    private SrampApiClientAccessor clientAccessor;

    /**
     * Constructor.
     */
    public OntologyService() {
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IOntologyService#get(java.lang.String)
     */
    @Override
    public OntologyBean get(String uuid) throws SrampUiException {
        try {
            RDF rdf = clientAccessor.getClient().getOntology(uuid);
            SrampOntology ontology = RdfToOntologyMapper.rdf2ontology(rdf);
            OntologyBean bean = ontologyToBean(ontology);
            return bean;
        } catch (Exception e) {
            throw new SrampUiException(e.getMessage());
        }
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IOntologyService#list()
     */
    @Override
    public List<OntologySummaryBean> list() throws SrampUiException {
        try {
            List<OntologySummaryBean> rval = new ArrayList<OntologySummaryBean>();
            List<OntologySummary> ontologies = clientAccessor.getClient().getOntologies();
            for (OntologySummary ontologySummary : ontologies) {
                rval.add(ontologySummaryToBean(ontologySummary));
            }
            return rval;
        } catch (Exception e) {
            throw new SrampUiException(e.getMessage());
        }
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IOntologyService#update(org.overlord.sramp.ui.client.shared.beans.OntologyBean)
     */
    @Override
    public void update(OntologyBean ontology) throws SrampUiException {
        // TODO Implement updating of an ontology
    }

    /**
     * Converts an ontology into an {@link OntologyBean}.
     * @param ontology
     */
    private OntologyBean ontologyToBean(SrampOntology ontology) {
        OntologyBean bean = new OntologyBean();
        bean.setLastModifiedBy(ontology.getLastModifiedBy());
        List<Class> allClasses = ontology.getAllClasses();
        // Create and index all the classes first
        for (Class cl4ss : allClasses) {
            bean.createClass(cl4ss.getId());
        }
        // Then go back through and set up the tree.
        for (Class cl4ss : allClasses) {
            OntologyClassBean classBean = bean.findClassById(cl4ss.getId());
            if (cl4ss.getParent() != null) {
                OntologyClassBean parentBean = bean.findClassById(cl4ss.getParent().getId());
                if (parentBean != null) {
                    classBean.setParent(parentBean);
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

}
