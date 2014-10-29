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


/**
 * Information about the KIE model defined by the KIE artifact builder(s). KIE
 * is used by Drools and jBPM.
 *
 */
public class KieJarModel {

    public static final String TYPE_ARCHIVE = "KieJarArchive"; //$NON-NLS-1$

    public static final String KieXmlDocument = "KieXmlDocument"; //$NON-NLS-1$
    public static final String BpmnDocument   = "BpmnDocument"; //$NON-NLS-1$
    public static final String DroolsDocument = "DroolsDocument"; //$NON-NLS-1$
 
}
