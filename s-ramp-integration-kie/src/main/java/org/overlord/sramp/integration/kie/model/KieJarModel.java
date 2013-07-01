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
package org.overlord.sramp.integration.kie.model;

import org.overlord.sramp.common.ArtifactType;


/**
 * Information about the KIE model defined by the KIE deriver(s). KIE
 * is used by Drools and jBPM.
 *
 */
public class KieJarModel {

    public static final String TYPE_ARCHIVE = "KieJarArchive";
    public static final String TYPE_BEANS_XML = "BeanArchiveDescriptor";

    public static final String KieXmlDocument = "KieXmlDocument";
    public static final ArtifactType KieXmlDocumentType = ArtifactType.valueOf(KieXmlDocument);

}
