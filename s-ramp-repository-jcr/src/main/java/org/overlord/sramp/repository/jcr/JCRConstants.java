/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository.jcr;

import org.overlord.sramp.common.SrampConstants;


public class JCRConstants {

    public static final String JCR_MIME_TYPE = "jcr:mimeType"; //$NON-NLS-1$
    public static final String JCR_CONTENT = "jcr:content"; //$NON-NLS-1$
    public static final String JCR_SQL2 = "JCR-SQL2"; //$NON-NLS-1$

    public static final String SRAMP                  = "sramp"; //$NON-NLS-1$
	public static final String SRAMP_NS               = SrampConstants.SRAMP_NS;
	public static final String SRAMP_PROPERTIES       = "sramp-properties"; //$NON-NLS-1$
	public static final String SRAMP_PROPERTIES_NS    = SRAMP_NS + "#properties"; //$NON-NLS-1$
	public static final String SRAMP_RELATIONSHIPS    = "sramp-relationships"; //$NON-NLS-1$
	public static final String SRAMP_RELATIONSHIPS_NS = SRAMP_NS + "#relationships"; //$NON-NLS-1$
    public static final String SRAMP_AUDIT            = "audit"; //$NON-NLS-1$
    public static final String SRAMP_AUDIT_NS         = SrampConstants.SRAMP_AUDIT_NS;

    public static final String SRAMP_                    = SRAMP + ":"; //$NON-NLS-1$
    public static final String SRAMP_AUDIT_ENTRY         = SRAMP_AUDIT + ":auditEntry"; //$NON-NLS-1$
    public static final String SRAMP_AUDIT_ITEM          = SRAMP_AUDIT + ":auditItem"; //$NON-NLS-1$
    public static final String SRAMP_BASE_ARTIFACT_TYPE  = SRAMP_ + "baseArtifactType"; //$NON-NLS-1$
    public static final String SRAMP_CONTENT_TYPE        = SRAMP_ + "contentType"; //$NON-NLS-1$
    public static final String SRAMP_CONTENT_SIZE        = SRAMP_ + "contentSize"; //$NON-NLS-1$
    public static final String SRAMP_CONTENT_HASH        = SRAMP_ + "contentHash"; //$NON-NLS-1$
    public static final String SRAMP_CONTENT_ENCODING    = SRAMP_ + "contentEncoding"; //$NON-NLS-1$
    public static final String SRAMP_UUID                = SRAMP_ + "uuid"; //$NON-NLS-1$
    public static final String SRAMP_NAME                = SRAMP_ + "name"; //$NON-NLS-1$
    public static final String SRAMP_DERIVED             = SRAMP_ + "derived"; //$NON-NLS-1$
    public static final String SRAMP_ARTIFACT_MODEL      = SRAMP_ + "artifactModel"; //$NON-NLS-1$
    public static final String SRAMP_ARTIFACT_TYPE       = SRAMP_ + "artifactType"; //$NON-NLS-1$
    public static final String SRAMP_EXTENDED_TYPE       = SRAMP_ + "extendedType"; //$NON-NLS-1$
    public static final String SRAMP_NON_DOCUMENT_TYPE   = SRAMP_ + "nonDocumentArtifactType"; //$NON-NLS-1$
    public static final String SRAMP_DERIVED_PRIMARY_TYPE     = SRAMP_ + "derivedArtifactPrimaryType"; //$NON-NLS-1$
}
