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
package org.overlord.sramp.server.atom.workspaces;

import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.common.ArtifactTypeEnum;


/**
 * Models the S-RAMP policy workspace.
 * @author eric.wittmann@redhat.com
 */
public class PolicyWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = -3830253469253740165L;

	/**
	 * Constructor.
	 * @param hrefBase
	 */
	public PolicyWorkspace(String hrefBase) {
		super(hrefBase, "Policy Model"); //$NON-NLS-1$
	}

	/**
	 * @see org.overlord.sramp.common.server.atom.workspaces.AbstractWorkspace#configureWorkspace()
	 */
	@Override
	protected void configureWorkspace() {
        AppCollection policyCollection = addCollection("/s-ramp/policy", "Policy Model Objects", MediaType.APPLICATION_ZIP); //$NON-NLS-1$ //$NON-NLS-2$
        AppCollection policyDocumentCollection = addCollection("/s-ramp/policy/PolicyDocument", "Policy Documents", MediaType.APPLICATION_XML); //$NON-NLS-1$ //$NON-NLS-2$
        AppCollection policyExpressionCollection = addCollection("/s-ramp/policy/PolicyExpression", "Policy Expressions", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection policyAttachmentCollection = addCollection("/s-ramp/policy/PolicyAttachment", "Policy Attachments", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        addTypeCategory(policyCollection, ArtifactTypeEnum.PolicyDocument);
        addTypeCategory(policyCollection, ArtifactTypeEnum.PolicyExpression);
        addTypeCategory(policyCollection, ArtifactTypeEnum.PolicyAttachment);
        addTypeCategory(policyDocumentCollection, ArtifactTypeEnum.PolicyDocument);
        addTypeCategory(policyExpressionCollection, ArtifactTypeEnum.PolicyExpression);
        addTypeCategory(policyAttachmentCollection, ArtifactTypeEnum.PolicyAttachment);

    }
}
