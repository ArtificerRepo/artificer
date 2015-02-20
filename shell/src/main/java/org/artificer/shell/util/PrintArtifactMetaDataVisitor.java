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
package org.artificer.shell.util;

import org.artificer.common.ArtifactType;
import org.artificer.common.visitors.HierarchicalArtifactVisitor;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;

import java.util.List;

/**
 * Visitor that knows how to print out details of an artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class PrintArtifactMetaDataVisitor extends HierarchicalArtifactVisitor {
	
	private final CommandInvocation commandInvocation;

	/**
	 * Constructor.
	 */
	public PrintArtifactMetaDataVisitor(CommandInvocation commandInvocation) {
		this.commandInvocation = commandInvocation;
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
		ArtifactType artifactType = ArtifactType.valueOf(artifact);
		commandInvocation.getShell().out().println(Messages.i18n.format("PrintArtifact.CoreHeading")); //$NON-NLS-1$
        if (artifactType.isExtendedType())
            printProperty(Messages.i18n.format("PrintArtifact.Type"), artifactType.getExtendedType()); //$NON-NLS-1$
        else
            printProperty(Messages.i18n.format("PrintArtifact.Type"), artifactType.getArtifactType().getType()); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.Model"), artifactType.getArtifactType().getModel()); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.UUID"), artifact.getUuid()); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.Name"), artifact.getName()); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.Version"), artifact.getVersion()); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.Derived"), String.valueOf(artifactType.isDerived())); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.CreatedBy"), artifact.getCreatedBy()); //$NON-NLS-1$
		if (artifact.getCreatedTimestamp() != null)
			printProperty(Messages.i18n.format("PrintArtifact.CreatedOn"), artifact.getCreatedTimestamp().toXMLFormat()); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.ModifiedBy"), artifact.getLastModifiedBy()); //$NON-NLS-1$
		if (artifact.getLastModifiedTimestamp() != null)
			printProperty(Messages.i18n.format("PrintArtifact.ModifiedOn"), artifact.getLastModifiedTimestamp().toXMLFormat()); //$NON-NLS-1$
		if (artifact.getDescription() != null) {
			commandInvocation.getShell().out().println(Messages.i18n.format("PrintArtifact.DescriptionHeading")); //$NON-NLS-1$
			commandInvocation.getShell().out().println(artifact.getDescription());
		}
		if (artifact.getClassifiedBy().size() > 0) {
			commandInvocation.getShell().out().println(Messages.i18n.format("PrintArtifact.ClassifiersHeading")); //$NON-NLS-1$
			for (String classification : artifact.getClassifiedBy()) {
				printProperty(Messages.i18n.format("PrintArtifact.ClassifiedBy"), classification); //$NON-NLS-1$
			}
		}
		if (artifact.getProperty().size() > 0) {
			commandInvocation.getShell().out().println(Messages.i18n.format("PrintArtifact.CustomPropsHeading")); //$NON-NLS-1$
			for (Property property : artifact.getProperty()) {
				printProperty(property.getPropertyName(), property.getPropertyValue());
			}
		}
		if (artifact.getRelationship().size() > 0) {
			commandInvocation.getShell().out().println(Messages.i18n.format("PrintArtifact.GenericRelationshipsHeading")); //$NON-NLS-1$
			for (Relationship relationship : artifact.getRelationship()) {
				List<Target> targets = relationship.getRelationshipTarget();
				printRelationship(relationship.getRelationshipType(), targets);
			}
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType)
	 */
	@Override
	protected void visitDerived(DerivedArtifactType artifact) {
		commandInvocation.getShell().out().println(Messages.i18n.format("PrintArtifact.DerivedArtifactInfoHeading")); //$NON-NLS-1$
		DocumentArtifactTarget relatedDocument = artifact.getRelatedDocument();
		printRelationship(Messages.i18n.format("PrintArtifact.RelatedDoc"), relatedDocument); //$NON-NLS-1$
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType)
	 */
	@Override
	protected void visitDocument(DocumentArtifactType artifact) {
		commandInvocation.getShell().out().println(Messages.i18n.format("PrintArtifact.DocumentInfoHeading")); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.ContentType"), artifact.getContentType()); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.ContentSize"), String.valueOf(artifact.getContentSize())); //$NON-NLS-1$
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitXmlDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument)
	 */
	@Override
	protected void visitXmlDocument(XmlDocument artifact) {
		commandInvocation.getShell().out().println(Messages.i18n.format("PrintArtifact.XmlDocumentHeading")); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.ContentEncoding"), artifact.getContentEncoding()); //$NON-NLS-1$
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitNamedWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType)
	 */
	@Override
	protected void visitNamedWsdlDerived(NamedWsdlDerivedArtifactType artifact) {
		commandInvocation.getShell().out().println(Messages.i18n.format("PrintArtifact.NamedWsdlHeading")); //$NON-NLS-1$
		printProperty(Messages.i18n.format("PrintArtifact.NCName"), artifact.getNCName()); //$NON-NLS-1$
	}

	/**
	 * Prints out a single property from the s-ramp meta-data.
	 * @param propertyName
	 * @param propertyValue
	 */
	private void printProperty(String propertyName, String propertyValue) {
		if (propertyValue != null)
			commandInvocation.getShell().out().printf("  %1$s: %2$s\n", propertyName, propertyValue); //$NON-NLS-1$
	}

	/**
	 * Prints out a single relationship.
	 * @param name
	 * @param target
	 */
	private void printRelationship(String name, Target target) {
		String targetStr = target.getValue();
		printProperty(name, targetStr);
	}

	/**
	 * Prints out a single relationship.
	 * @param name
	 * @param targets
	 */
	private void printRelationship(String name, List<? extends Target> targets) {
		String targetStr = ""; //$NON-NLS-1$
		boolean first = true;
		for (Target target : targets) {
			if (!first) {
				targetStr += ", "; //$NON-NLS-1$
			} else {
				first = false;
			}
			targetStr += target.getValue();
		}
		printProperty(name, targetStr);
	}

}
