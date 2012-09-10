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
package org.overlord.sramp.repository.jcr.mapper;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.ValueFormatException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.repository.jcr.JCRConstants;
import org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdType;

/**
 * A visitor for going from a JCR node to an S-RAMP artifact instance.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRNodeToArtifactVisitor extends HierarchicalArtifactVisitorAdapter {

	private Node jcrNode;

	/**
	 * Constructor.
	 */
	public JCRNodeToArtifactVisitor(Node jcrNode) {
		this.jcrNode = jcrNode;
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitBase(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
		try {
			ArtifactType artifactType = ArtifactType.valueOf(artifact);
			BaseArtifactEnum apiType = artifactType.getArtifactType().getApiType();
			artifact.setArtifactType(apiType);

			// First map in the standard s-ramp meta-data
			artifact.setCreatedBy(getProperty(jcrNode, "jcr:createdBy"));
			XMLGregorianCalendar createdTS;
			createdTS = DatatypeFactory.newInstance().newXMLGregorianCalendar(getProperty(jcrNode, "jcr:created"));
			artifact.setCreatedTimestamp(createdTS);
			artifact.setDescription(getProperty(jcrNode, "sramp:description"));
			artifact.setLastModifiedBy(getProperty(jcrNode, "jcr:lastModifiedBy"));
			XMLGregorianCalendar modifiedTS = DatatypeFactory.newInstance().newXMLGregorianCalendar(getProperty(jcrNode, "jcr:lastModified"));
			artifact.setLastModifiedTimestamp(modifiedTS);
			artifact.setName(getProperty(jcrNode, "sramp:name"));
			artifact.setUuid(getProperty(jcrNode, "sramp:uuid"));
			artifact.setVersion(getProperty(jcrNode, "version"));

			// Now map in all the s-ramp user-defined properties.
			String srampPropsPrefix = JCRConstants.SRAMP_PROPERTIES + ":";
			int srampPropsPrefixLen = srampPropsPrefix.length();
			PropertyIterator properties = jcrNode.getProperties();
			while (properties.hasNext()) {
				Property property = properties.nextProperty();
				String propQName = property.getName();
				if (propQName.startsWith(srampPropsPrefix)) {
					String propName = propQName.substring(srampPropsPrefixLen);
			    	String propValue = property.getValue().getString();
			    	org.s_ramp.xmlns._2010.s_ramp.Property srampProp = new org.s_ramp.xmlns._2010.s_ramp.Property();
			    	srampProp.setPropertyName(propName);
			    	srampProp.setPropertyValue(propValue);
					artifact.getProperty().add(srampProp);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitDerived(org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType)
	 */
	@Override
	protected void visitDerived(DerivedArtifactType artifact) {
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitWsdlDerived(org.s_ramp.xmlns._2010.s_ramp.WsdlDerivedArtifactType)
	 */
	@Override
	protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitXsdDerived(org.s_ramp.xmlns._2010.s_ramp.XsdType)
	 */
	@Override
	protected void visitXsdDerived(XsdType artifact) {
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitDocument(org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType)
	 */
	@Override
	protected void visitDocument(DocumentArtifactType artifact) {
        artifact.setContentSize(Long.valueOf(getProperty(jcrNode, "sramp:contentSize")));
        artifact.setContentType(getProperty(jcrNode, "sramp:contentType"));
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitXmlDocument(org.s_ramp.xmlns._2010.s_ramp.XmlDocument)
	 */
	@Override
	protected void visitXmlDocument(XmlDocument artifact) {
        artifact.setContentEncoding(getProperty(jcrNode, "sramp:contentEncoding"));
	}

    /**
     * Gets a single property from the given JCR node.  This returns null
     * if the property does not exist.
     * @param node the JCR node
     * @param propertyName the name of the property
     * @return the String value of the property
     */
    protected static final String getProperty(Node node, String propertyName) {
    	return getProperty(node, propertyName, null);
    }

    /**
     * Gets a single property from the given JCR node.  This returns a default value if
     * the property does not exist.
     * @param node the JCR node
     * @param propertyName the name of the property
     * @param defaultValue a default value if the property does not exist on the node
     * @return the String value of the property
     */
    protected static final String getProperty(Node node, String propertyName, String defaultValue) {
    	try {
			return node.getProperty(propertyName).getString();
		} catch (ValueFormatException e) {
		} catch (PathNotFoundException e) {
		} catch (javax.jcr.RepositoryException e) {
		}
		return defaultValue;
    }

}
