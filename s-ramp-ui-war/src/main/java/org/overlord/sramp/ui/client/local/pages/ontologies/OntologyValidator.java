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

package org.overlord.sramp.ui.client.local.pages.ontologies;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.overlord.sramp.ui.client.local.ClientMessages;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.shared.beans.OntologyBean;
import org.overlord.sramp.ui.client.shared.beans.OntologyClassBean;

/**
 * Used to validate an ontology, typically prior to modifying it.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class OntologyValidator {
    
    private OntologyBean ontology;
    
    @Inject
    ClientMessages i18n;
    @Inject
    NotificationService notificationService;
    
    /**
     * Constructor.
     */
    public OntologyValidator() {
    }

    /**
     * @return the ontology
     */
    public OntologyBean getOntology() {
        return ontology;
    }

    /**
     * @param ontology the ontology to set
     */
    public void setOntology(OntologyBean ontology) {
        this.ontology = ontology;
    }

    /**
     * Returns true if the given ontology class bean is OK to add to the
     * ontology.
     * @param bean
     */
    public boolean canAddClass(OntologyClassBean bean) {
        String id = bean.getId();
        if (classAlreadyExists(id, getOntology().getRootClasses())) {
            notificationService.sendWarningNotification(
                    i18n.format("ontology-validator.edit-error.title"), //$NON-NLS-1$
                    i18n.format("ontology-validator.edit-error.message", bean.getId())); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    /**
     * Checks for an already existing class with the given id.  Ontologies can only have
     * a single class with a given id (all classes must have unique IDs) even though the
     * ontology is hierarchical!
     * @param id
     */
    private boolean classAlreadyExists(String id, List<OntologyClassBean> classes) {
        for (OntologyClassBean classBean : classes) {
            if (classBean.getId().equals(id) || classAlreadyExists(id, classBean.getChildren())) {
                return true;
            }
        }
        return false;
    }

}
