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

    public static final String JCR_MIME_TYPE = "jcr:mimeType";
    public static final String JCR_CONTENT = "jcr:content";
    public static final String JCR_SQL2 = "JCR-SQL2";

    public static final String SRAMP                  = "sramp";
	public static final String SRAMP_NS               = SrampConstants.SRAMP_NS;
	public static final String SRAMP_PROPERTIES       = "sramp-properties";
	public static final String SRAMP_PROPERTIES_NS    = SRAMP_NS + "#properties";
	public static final String SRAMP_RELATIONSHIPS    = "sramp-relationships";
	public static final String SRAMP_RELATIONSHIPS_NS = SRAMP_NS + "#relationships";
    public static final String SRAMP_AUDIT            = "audit";
    public static final String SRAMP_AUDIT_NS         = SrampConstants.SRAMP_AUDIT_NS;

    public static final String SRAMP_                    = SRAMP + ":";
    public static final String SRAMP_AUDIT_ENTRY         = SRAMP_AUDIT + ":auditEntry";
    public static final String SRAMP_AUDIT_ITEM          = SRAMP_AUDIT + ":auditItem";
    public static final String SRAMP_BASE_ARTIFACT_TYPE  = SRAMP_ + "baseArtifactType";
    public static final String SRAMP_CONTENT_TYPE        = SRAMP_ + "contentType";
    public static final String SRAMP_CONTENT_SIZE        = SRAMP_ + "contentSize";
    public static final String SRAMP_CONTENT_HASH        = SRAMP_ + "contentHash";
    public static final String SRAMP_CONTENT_ENCODING    = SRAMP_ + "contentEncoding";
    public static final String SRAMP_UUID                = SRAMP_ + "uuid";
    public static final String SRAMP_NAME                = SRAMP_ + "name";
    public static final String SRAMP_DERIVED             = SRAMP_ + "derived";
    public static final String SRAMP_ARTIFACT_MODEL      = SRAMP_ + "artifactModel";
    public static final String SRAMP_ARTIFACT_TYPE       = SRAMP_ + "artifactType";
    public static final String SRAMP_EXTENDED_TYPE       = SRAMP_ + "extendedType";
    public static final String SRAMP_NON_DOCUMENT_TYPE   = SRAMP_ + "nonDocumentArtifactType";
    public static final String SRAMP_DERIVED_PRIMARY_TYPE     = SRAMP_ + "derivedArtifactPrimaryType";
}
