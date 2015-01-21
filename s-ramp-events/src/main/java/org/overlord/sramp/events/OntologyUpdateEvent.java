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
package org.overlord.sramp.events;

import org.overlord.sramp.common.ontology.SrampOntology;

/**
 * @author Brett Meyer
 */
public class OntologyUpdateEvent {
    
    private SrampOntology updatedOntology = null;
    
    private SrampOntology oldOntology = null;
    
    public OntologyUpdateEvent() {
    }
    
    public OntologyUpdateEvent(SrampOntology updatedOntology, SrampOntology oldOntology) {
        this.updatedOntology = updatedOntology;
        this.oldOntology = oldOntology;
    }

    /**
     * @return the updatedOntology
     */
    public SrampOntology getUpdatedOntology() {
        return updatedOntology;
    }

    /**
     * @param updatedOntology the updatedOntology to set
     */
    public void setUpdatedOntology(SrampOntology updatedOntology) {
        this.updatedOntology = updatedOntology;
    }

    /**
     * @return the oldOntology
     */
    public SrampOntology getOldOntology() {
        return oldOntology;
    }

    /**
     * @param oldOntology the oldOntology to set
     */
    public void setOldOntology(SrampOntology oldOntology) {
        this.oldOntology = oldOntology;
    }

}
