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
package org.overlord.sramp.integration.artifactbuilder;

import java.util.Collection;
import java.util.Map;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;

/**
 * Resolves relationships using the target's namespace and filename.  The generated criteria looks for an artifact with a
 * matching "targetNamespace" and filename.  This is especially useful for XML documents importing/including external schemas,
 * allowing relationships to be later defined using that schema's defined types.
 * 
 * @author Brett Meyer
 */
public class NamespaceRelationshipSource extends CriteriaQueryRelationshipSource {
    
    private final String namespace;
    
    private final String filename;

    public NamespaceRelationshipSource(String namespace, String filename, Target target, Collection targetCollection,
            String model, String... types) {
        super(target, targetCollection, model, types);
        this.namespace = namespace;
        this.filename = filename;
    }
    
    @Override
    protected void addCriteria(Map<String, String> criteria) {
        criteria.put("targetNamespace", namespace);
        criteria.put("name", filename);
    }

}
