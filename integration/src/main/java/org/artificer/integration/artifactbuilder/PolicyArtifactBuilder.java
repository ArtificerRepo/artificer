/*
 * Copyright 2012 JBoss Inc
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
package org.artificer.integration.artifactbuilder;

import org.artificer.common.query.xpath.StaticNamespaceContext;

/**
 * Creates derived content from a Policy document.  This will create the derived content as
 * defined in the Policy model found in the s-ramp specification.  The following derived
 * artifact types will (potentially) be created:
 *
 * <ul>
 *   <li>PolicyExpression</li>
 *   <li>PolicyAttachment</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyArtifactBuilder extends XmlArtifactBuilder {

	@Override
	protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
		super.configureNamespaceMappings(namespaceContext);
		namespaceContext.addMapping("wsp", "http://www.w3.org/ns/ws-policy"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
