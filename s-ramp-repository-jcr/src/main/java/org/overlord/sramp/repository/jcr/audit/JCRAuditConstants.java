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
package org.overlord.sramp.repository.jcr.audit;

import java.util.HashSet;
import java.util.Set;

/**
 * Some constants used by the auditing component.
 * @author eric.wittmann@redhat.com
 */
public class JCRAuditConstants {

    private static final String [] EXCLUDES = {
        "jcr:lastModified",
        "jcr:lastModifiedBy",
        "sramp:derived",
        "jcr:primaryType",
        "sramp:uuid",
        "sramp:normalizedClassifiedBy",
        "jcr:created",
        "jcr:versionHistory",
        "jcr:baseVersion",
        "jcr:predecessors",
        "jcr:isCheckedOut",
        "jcr:mixinTypes",
        "sramp:artifactType",
        "jcr:uuid",
        "sramp:artifactModel",
        "jcr:createdBy"
    };
    public static final Set<String> propertyExcludes = new HashSet<String>();
    static {
        for (String exc : EXCLUDES) {
            propertyExcludes.add(exc);
        }
    }

    public static final String AUDIT_BUNDLE_AUDITING = "audit:audit";
    public static final String AUDIT_BUNDLE_ARTIFACT_DELETED = "audit:artifact-deleted";
    public static final String AUDIT_BUNDLE_ONTOLOGY_DELETED = "audit:ontology-deleted";
    public static final String AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE1 = "audit:artifact-added-1";
    public static final String AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE2 = "audit:artifact-added-2";
    public static final String AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE3 = "audit:artifact-added-3";
    public static final String AUDIT_BUNDLE_DERIVED_ARTIFACTS_ADDED_PHASE1 = "audit:derived-artifacts-added-1";
    public static final String AUDIT_BUNDLE_DERIVED_ARTIFACTS_ADDED_PHASE2 = "audit:derived-artifacts-added-2";
    public static final String AUDIT_BUNDLE_ONTOLOGY_ADDED = "audit:ontology-added";
    public static final String AUDIT_BUNDLE_ARTIFACT_UPDATED = "audit:artifact-updated";
    public static final String AUDIT_BUNDLE_ARTIFACT_CONTENT_UPDATED = "audit:artifact-content-updated";
    public static final String AUDIT_BUNDLE_ONTOLOGY_UPDATED = "audit:ontology-updated";

}
