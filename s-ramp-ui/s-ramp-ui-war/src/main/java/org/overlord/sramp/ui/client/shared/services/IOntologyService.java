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
package org.overlord.sramp.ui.client.shared.services;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.overlord.sramp.ui.client.shared.beans.OntologyBean;
import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;

/**
 * Provides a way to get/set ontology data.
 *
 * @author eric.wittmann@redhat.com
 */
@Remote
public interface IOntologyService {

    /**
     * Gets the list of all ontologies.
     * @throws SrampUiException
     */
    public List<OntologySummaryBean> list() throws SrampUiException;

    /**
     * Gets the full meta data for an ontology, including its full tree of classes.
     * @param uuid
     * @throws SrampUiException
     */
    public OntologyBean get(String uuid) throws SrampUiException;

    /**
     * Called to update the given ontology.
     * @param ontology
     * @throws SrampUiException
     */
    public void update(OntologyBean ontology) throws SrampUiException;

}
