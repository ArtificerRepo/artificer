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
package org.overlord.sramp.shell.util;

import java.util.List;

import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;

/**
 * Visitor that knows how to print out details of an artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class PrintArtifactMetaDataVisitor extends HierarchicalArtifactVisitorAdapter {

	/**
	 * Constructor.
	 */
	public PrintArtifactMetaDataVisitor() {
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
		ArtifactType artifactType = ArtifactType.valueOf(artifact);
		System.out.println("  -- Core S-RAMP Info --");
        if (artifactType.isExtendedType())
            printProperty("Type", artifactType.getExtendedType());
        else
            printProperty("Type", artifactType.getArtifactType().getType());
		printProperty("Model", artifactType.getArtifactType().getModel());
		printProperty("UUID", artifact.getUuid());
		printProperty("Name", artifact.getName());
		printProperty("Version", artifact.getVersion());
		printProperty("Derived", String.valueOf(artifactType.isDerived()));
		printProperty("Created By", artifact.getCreatedBy());
		if (artifact.getCreatedTimestamp() != null)
			printProperty("Created On", artifact.getCreatedTimestamp().toXMLFormat());
		printProperty("Modified By", artifact.getLastModifiedBy());
		if (artifact.getLastModifiedTimestamp() != null)
			printProperty("Modified On", artifact.getLastModifiedTimestamp().toXMLFormat());
		if (artifact.getDescription() != null) {
			System.out.println("  -- Description --");
			System.out.println(artifact.getDescription());
		}
		if (artifact.getClassifiedBy().size() > 0) {
			System.out.println("  -- Classifications --");
			for (String classification : artifact.getClassifiedBy()) {
				printProperty("Classified By", classification);
			}
		}
		if (artifact.getProperty().size() > 0) {
			System.out.println("  -- Custom Properties --");
			for (Property property : artifact.getProperty()) {
				printProperty(property.getPropertyName(), property.getPropertyValue());
			}
		}
		if (artifact.getRelationship().size() > 0) {
			System.out.println("  -- Generic Relationships --");
			for (Relationship relationship : artifact.getRelationship()) {
				List<Target> targets = relationship.getRelationshipTarget();
				printRelationship(relationship.getRelationshipType(), targets);
			}
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType)
	 */
	@Override
	protected void visitDerived(DerivedArtifactType artifact) {
		System.out.println("  -- Derived Artifact Info --");
		DocumentArtifactTarget relatedDocument = artifact.getRelatedDocument();
		printRelationship("Related Document", relatedDocument);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType)
	 */
	@Override
	protected void visitDocument(DocumentArtifactType artifact) {
		System.out.println("  -- Document Info --");
		printProperty("Content Type", artifact.getContentType());
		printProperty("Content Size", String.valueOf(artifact.getContentSize()));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitXmlDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument)
	 */
	@Override
	protected void visitXmlDocument(XmlDocument artifact) {
		System.out.println("  -- XML Document Info --");
		printProperty("Content Encoding", artifact.getContentEncoding());
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitNamedWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType)
	 */
	@Override
	protected void visitNamedWsdlDerived(NamedWsdlDerivedArtifactType artifact) {
		System.out.println("  -- Named WSDL Info --");
		printProperty("NCName", artifact.getNCName());
	}

	/**
	 * Prints out a single property from the s-ramp meta-data.
	 * @param propertyName
	 * @param propertyValue
	 */
	private static void printProperty(String propertyName, String propertyValue) {
		if (propertyValue != null)
			System.out.printf("  %1$s: %2$s\n", propertyName, propertyValue);
	}

	/**
	 * Prints out a single relationship.
	 * @param name
	 * @param target
	 */
	private static void printRelationship(String name, Target target) {
		String targetStr = target.getValue();
		printProperty(name, targetStr);
	}

	/**
	 * Prints out a single relationship.
	 * @param name
	 * @param targets
	 */
	private static void printRelationship(String name, List<? extends Target> targets) {
		String targetStr = "";
		boolean first = true;
		for (Target target : targets) {
			if (!first) {
				targetStr += ", ";
			} else {
				first = false;
			}
			targetStr += target.getValue();
		}
		printProperty(name, targetStr);
	}

}
