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

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;

/**
 * Resolves relationships using the target's {@link QName}.  The generated criteria looks for an artifact with a
 * matching "namespace" and "ncName" in the repository.
 * 
 * @author Brett Meyer
 */
public class QNameRelationshipSource extends CriteriaQueryRelationshipSource {

    private final QName qName;
    
    public QNameRelationshipSource(QName qName, Target target, Collection targetCollection, String model,
            String... types) {
        super(target, targetCollection, model, types);
        this.qName = qName;
    }

    @Override
    protected void addCriteria(Map<String, String> criteria) {
        criteria.put("namespace", qName.getNamespaceURI()); //$NON-NLS-1$
        criteria.put("ncName", qName.getLocalPart()); //$NON-NLS-1$
    }

}
