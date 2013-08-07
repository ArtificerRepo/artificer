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
        "jcr:lastModified", //$NON-NLS-1$
        "jcr:lastModifiedBy", //$NON-NLS-1$
        "sramp:derived", //$NON-NLS-1$
        "jcr:primaryType", //$NON-NLS-1$
        "sramp:uuid", //$NON-NLS-1$
        "sramp:normalizedClassifiedBy", //$NON-NLS-1$
        "jcr:created", //$NON-NLS-1$
        "jcr:versionHistory", //$NON-NLS-1$
        "jcr:baseVersion", //$NON-NLS-1$
        "jcr:predecessors", //$NON-NLS-1$
        "jcr:isCheckedOut", //$NON-NLS-1$
        "jcr:mixinTypes", //$NON-NLS-1$
        "sramp:artifactType", //$NON-NLS-1$
        "jcr:uuid", //$NON-NLS-1$
        "sramp:artifactModel", //$NON-NLS-1$
        "jcr:createdBy" //$NON-NLS-1$
    };
    public static final Set<String> propertyExcludes = new HashSet<String>();
    static {
        for (String exc : EXCLUDES) {
            propertyExcludes.add(exc);
        }
    }

    public static final String AUDIT_BUNDLE_AUDITING = "audit:audit"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE1 = "audit:artifact-added-1"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE2 = "audit:artifact-added-2"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE3 = "audit:artifact-added-3"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_DERIVED_ARTIFACTS_ADDED_PHASE1 = "audit:derived-artifacts-added-1"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_DERIVED_ARTIFACTS_ADDED_PHASE2 = "audit:derived-artifacts-added-2"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_ARTIFACT_DELETED = "audit:artifact-deleted"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_ARTIFACT_UPDATED = "audit:artifact-updated"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_ARTIFACT_CONTENT_UPDATED = "audit:artifact-content-updated"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_ONTOLOGY_ADDED = "audit:ontology-added"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_ONTOLOGY_UPDATED = "audit:ontology-updated"; //$NON-NLS-1$
    public static final String AUDIT_BUNDLE_ONTOLOGY_DELETED = "audit:ontology-deleted"; //$NON-NLS-1$

}
