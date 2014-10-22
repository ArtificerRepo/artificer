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
package org.overlord.sramp.common.artifactbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;

/**
 * Provides a {@link RelationshipSource} that resolves using a simple criteria-based repository query.  Sub-classes
 * are in charge of defining the actual criteria used in the search.  The query result's artifact is then set as the
 * relationship {@link Target} value.
 * 
 * @see QNameRelationshipSource
 * @see NamespaceRelationshipSource
 * 
 * @author Brett Meyer
 */
public abstract class CriteriaQueryRelationshipSource implements RelationshipSource {
    
    protected Target target;
    
    protected final Collection targetCollection;
    
    protected final String model;
    
    protected final String[] types;

    public CriteriaQueryRelationshipSource(Target target, Collection targetCollection, String model, String... types) {
        this.target = target;
        this.targetCollection = targetCollection;
        this.model = model;
        this.types = types;
    }
    
    @Override
    public void build(RelationshipContext context) {
        // If a specific Target was not provided, assume it's generic and needs created/added.
        if (target == null) {
            target = new Target();
            
            // Only do this if the target is created above.  If the target was created higher up, that code should be
            // responsible for adding it to the collection.
            if (targetCollection != null) {
                targetCollection.add(target);
            }
        }
        
        Map<String, String> criteria = new HashMap<String, String>();
        addCriteria(criteria);
        Collection<BaseArtifactType> artifacts = new ArrayList<BaseArtifactType>();
        
        for (String type : types) {
            Collection<BaseArtifactType> results = context.findArtifacts(model, type, criteria);
            artifacts.addAll(results);
        }
        
        if (!artifacts.isEmpty()) {
            // TODO need a more interesting way to dis-ambiguate the results
            BaseArtifactType artifact = artifacts.iterator().next();
            target.setValue(artifact.getUuid());
        } else {
            notFound();
            if (targetCollection != null) {
                // TODO: This needs evaluated.  May not work for no-target relationships (supported by spec).
                targetCollection.remove(target);
            }
            // Important to null out the ref for non-collection artifact targets.
            target = null;
        }
    }
    
    protected abstract void addCriteria(Map<String, String> criteria);
    
    protected void notFound() {
        
    }
}
