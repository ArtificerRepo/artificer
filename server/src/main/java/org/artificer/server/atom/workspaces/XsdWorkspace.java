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
package org.artificer.server.atom.workspaces;

import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.artificer.atom.MediaType;
import org.artificer.common.ArtifactTypeEnum;

/**
 * Models the S-RAMP XSD workspace.
 * @author eric.wittmann@redhat.com
 */
public class XsdWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = -5458032950037142746L;

	/**
	 * Constructor.
	 * @param hrefBase
	 */
	public XsdWorkspace(String hrefBase) {
		super(hrefBase, "XSD Model"); //$NON-NLS-1$
	}

	@Override
	protected void configureWorkspace() {
        AppCollection xsdCollection = addCollection("/s-ramp/xsd", "XSD Model Objects", MediaType.APPLICATION_ZIP); //$NON-NLS-1$ //$NON-NLS-2$
        AppCollection xsdDocumentCollection = addCollection("/s-ramp/xsd/XsdDocument", "XSD Documents", MediaType.APPLICATION_XML); //$NON-NLS-1$ //$NON-NLS-2$

        AppCollection attributeDeclarationCollection =
        		addCollection("/s-ramp/xsd/AttributeDeclaration", "Attribute Declarations", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection elementDeclarationCollection =
        		addCollection("/s-ramp/xsd/ElementDeclaration", "Element Declarations", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection simpleTypeDeclarationCollection =
        		addCollection("/s-ramp/xsd/SimpleTypeDeclaration", "Simple Type Declarations", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection complexTypeDeclarationCollection =
        		addCollection("/s-ramp/xsd/ComplexTypeDeclaration", "Complex Type Declarations", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        addTypeCategory(xsdCollection, ArtifactTypeEnum.XsdDocument);
        addTypeCategory(xsdCollection, ArtifactTypeEnum.AttributeDeclaration);
        addTypeCategory(xsdCollection, ArtifactTypeEnum.ElementDeclaration);
        addTypeCategory(xsdCollection, ArtifactTypeEnum.SimpleTypeDeclaration);
        addTypeCategory(xsdCollection, ArtifactTypeEnum.ComplexTypeDeclaration);

        addTypeCategory(xsdDocumentCollection, ArtifactTypeEnum.XsdDocument);
        addTypeCategory(elementDeclarationCollection, ArtifactTypeEnum.ElementDeclaration);
        addTypeCategory(attributeDeclarationCollection, ArtifactTypeEnum.AttributeDeclaration);
        addTypeCategory(simpleTypeDeclarationCollection, ArtifactTypeEnum.SimpleTypeDeclaration);
        addTypeCategory(complexTypeDeclarationCollection, ArtifactTypeEnum.ComplexTypeDeclaration);
    }
}
