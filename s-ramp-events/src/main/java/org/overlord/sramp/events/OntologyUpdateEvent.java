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

import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

/**
 * @author Brett Meyer
 */
public class OntologyUpdateEvent {
    
    private RDF updatedOntology = null;
    
    private RDF oldOntology = null;
    
    public OntologyUpdateEvent() {
    }
    
    public OntologyUpdateEvent(RDF updatedOntology, RDF oldOntology) {
        this.updatedOntology = updatedOntology;
        this.oldOntology = oldOntology;
    }

    /**
     * @return the updatedOntology
     */
    public RDF getUpdatedOntology() {
        return updatedOntology;
    }

    /**
     * @param updatedOntology the updatedOntology to set
     */
    public void setUpdatedOntology(RDF updatedOntology) {
        this.updatedOntology = updatedOntology;
    }

    /**
     * @return the oldOntology
     */
    public RDF getOldOntology() {
        return oldOntology;
    }

    /**
     * @param oldOntology the oldOntology to set
     */
    public void setOldOntology(RDF oldOntology) {
        this.oldOntology = oldOntology;
    }

}
