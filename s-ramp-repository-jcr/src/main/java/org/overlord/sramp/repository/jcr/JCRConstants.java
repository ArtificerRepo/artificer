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

import javax.xml.namespace.QName;


public class JCRConstants {

    public static final String JCR_CONTENT = "jcr:content";
    public static final String JCR_CONTENT_DATA = "jcr:content/jcr:data";
    public static final String JCR_CONTENT_MIME_TYPE = "jcr:content/jcr:mimeType";
    public static final String JCR_CREATED = "jcr:created";
    public static final String JCR_CREATED_BY = "jcr:createdBy";
    public static final String JCR_DATA = "jcr:data";
    public static final String JCR_LAST_MODIFIED = "jcr:lastModified";
    public static final String JCR_LAST_MODIFIED_BY = "jcr:lastModifiedBy";
    public static final String JCR_MIME_TYPE = "jcr:mimeType";
    public static final String JCR_MIXIN_TYPES = "jcr:mixinTypes";
    public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
    public static final String JCR_UUID = "jcr:uuid";

    public static final String NT_FILE = "nt:file";
    public static final String NT_FOLDER = "nt:folder";
    public static final String NT_RESOURCE= "nt:resource";
    
    public static final String JCR_SQL2 = "JCR-SQL2";

    public static final String SRAMP                            = "sramp";
	public static final String SRAMP_NS                         = SrampConstants.SRAMP_NS;
	public static final String SRAMP_PROPERTIES                 = "sramp-properties";
	public static final String SRAMP_PROPERTIES_NS              = SRAMP_NS + "#properties";
	public static final String SRAMP_RELATIONSHIPS               = "sramp-relationships";
	public static final String SRAMP_RELATIONSHIPS_NS           = SRAMP_NS + "#relationships";
    public static final String SRAMP_AUDIT                      = "audit";
    public static final String SRAMP_AUDIT_NS                   = SrampConstants.SRAMP_AUDIT_NS;
    public static final String SRAMP_OTHER_ATTRIBUTES           = "sramp-other-attributes";
    public static final String SRAMP_OTHER_ATTRIBUTES_NS        = SRAMP_NS + "#otherAttributes";

    public static final String SRAMP_                           = SRAMP + ":";
    public static final String SRAMP_ARTIFACT_MODEL             = SRAMP_ + "artifactModel";
    public static final String SRAMP_ARTIFACT_TYPE              = SRAMP_ + "artifactType";
    public static final String SRAMP_AUDIT_ENTRY                = SRAMP_AUDIT + ":auditEntry";
    public static final String SRAMP_AUDIT_ITEM                 = SRAMP_AUDIT + ":auditItem";
    public static final String SRAMP_BASE                       = SRAMP_ + "base";
    public static final String SRAMP_BASE_ARTIFACT_TYPE         = SRAMP_ + "baseArtifactType";
    public static final String SRAMP_CLASS                      = SRAMP_ + "class";
    public static final String SRAMP_CLASSIFIED_BY              = SRAMP_ + "classifiedBy";
    public static final String SRAMP_COMMENT                    = SRAMP_ + "comment";
    public static final String SRAMP_CONTENT_TYPE               = SRAMP_ + "contentType";
    public static final String SRAMP_CONTENT_SIZE               = SRAMP_ + "contentSize";
    public static final String SRAMP_CONTENT_HASH               = SRAMP_ + "contentHash";
    public static final String SRAMP_CONTENT_ENCODING           = SRAMP_ + "contentEncoding";
    public static final String SRAMP_DERIVED                    = SRAMP_ + "derived";
    public static final String SRAMP_DERIVED_PRIMARY_TYPE       = SRAMP_ + "derivedArtifactPrimaryType";
    public static final String SRAMP_DESCRIPTION                = SRAMP_ + "description";
    public static final String SRAMP_END                        = SRAMP_ + "end";
    public static final String SRAMP_EXTENDED_TYPE              = SRAMP_ + "extendedType";
    public static final String SRAMP_GENERIC                    = SRAMP_ + "generic";
    public static final String SRAMP_ID                         = SRAMP_ + "id";
    public static final String SRAMP_LABEL                      = SRAMP_ + "label";
    public static final String SRAMP_MAX_CARDINALITY            = SRAMP_ + "maxCardinality";
    public static final String SRAMP_NAME                       = SRAMP_ + "name";
    public static final String SRAMP_NC_NAME                    = SRAMP_ + "ncName";
    public static final String SRAMP_NAMESPACE                  = SRAMP_ + "namespace";
    public static final String SRAMP_NON_DOCUMENT_TYPE          = SRAMP_ + "nonDocumentArtifactType";
    public static final String SRAMP_NORMALIZED_CLASSIFIED_BY   = SRAMP_ + "normalizedClassifiedBy";
    public static final String SRAMP_ONTOLOGY                   = SRAMP_ + "ontology";
    public static final String SRAMP_PROPERTY_NAME              = SRAMP_ + "propertyName";
    public static final String SRAMP_QUERY                      = SRAMP_ + "query";
    public static final String SRAMP_QUERY_NAME                 = SRAMP_ + "queryName";
    public static final String SRAMP_QUERY_EXPRESSION           = SRAMP_ + "queryExpression";
    public static final String SRAMP_RELATIONSHIP               = SRAMP_ + "relationship";
    public static final String SRAMP_RELATIONSHIP_TARGET        = SRAMP_ + "relationshipTarget";
    public static final String SRAMP_RELATIONSHIP_TYPE          = SRAMP_ + "relationshipType";
    public static final String SRAMP_SOAP_LOCATION              = SRAMP_ + "soapLocation";
    public static final String SRAMP_STYLE                      = SRAMP_ + "style";
    public static final String SRAMP_TARGET_NAMESPACE           = SRAMP_ + "targetNamespace";
    public static final String SRAMP_TARGET                     = SRAMP_ + "target";
    public static final String SRAMP_TARGET_ARTIFACT            = SRAMP_ + "targetArtifact";
    public static final String SRAMP_TARGET_TYPE                = SRAMP_ + "targetType";
    public static final String SRAMP_TARGETS                    = "sramp-targets";
    public static final String SRAMP_TARGETS_NS                 = SRAMP_NS + "#targets";
    public static final String SRAMP_TRANSPORT                  = SRAMP_ + "transport";
    public static final String SRAMP_URI                        = SRAMP_ + "uri";
    public static final String SRAMP_URL                        = SRAMP_ + "url";
    public static final String SRAMP_UUID                       = SRAMP_ + "uuid";

    public static final String ROOT_PATH          = "/s-ramp";
    public static final String NOT_DELETED_FILTER = " AND (ISDESCENDANTNODE([sramp:baseArtifactType],'" + ROOT_PATH + "'))";

    public static final String NO_VALUE = "{NO_VALUE}";
}
