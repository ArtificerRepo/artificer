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
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.SrampConstants;
import org.overlord.sramp.repository.jcr.JCRConstants;
import org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Relationship;
import org.s_ramp.xmlns._2010.s_ramp.Target;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;
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
	private JCRReferenceResolver referenceResolver;

	/**
	 * Constructor.
	 * @param jcrNode
	 * @param referenceResolver
	 */
	public JCRNodeToArtifactVisitor(Node jcrNode, JCRReferenceResolver referenceResolver) {
		this.jcrNode = jcrNode;
		this.referenceResolver = referenceResolver;
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitBase(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
		try {
			DatatypeFactory dtFactory = DatatypeFactory.newInstance();
			ArtifactType artifactType = ArtifactType.valueOf(artifact);
			BaseArtifactEnum apiType = artifactType.getArtifactType().getApiType();
			artifact.setArtifactType(apiType);

			// First map in the standard s-ramp meta-data
			artifact.setCreatedBy(getProperty(jcrNode, "jcr:createdBy"));
			XMLGregorianCalendar createdTS = dtFactory.newXMLGregorianCalendar(getProperty(jcrNode, "jcr:created"));
			artifact.setCreatedTimestamp(createdTS);
			artifact.setDescription(getProperty(jcrNode, "sramp:description"));
			artifact.setLastModifiedBy(getProperty(jcrNode, "jcr:lastModifiedBy"));
			XMLGregorianCalendar modifiedTS = dtFactory.newXMLGregorianCalendar(getProperty(jcrNode, "jcr:lastModified"));
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

			// Now map in the generic relationships
			NodeIterator rnodes = jcrNode.getNodes();
			while (rnodes.hasNext()) {
				Node rNode = rnodes.nextNode();
				if (rNode.isNodeType("sramp:relationship")) {
					String rtype = getProperty(rNode, "sramp:relationshipType");
					boolean generic = false;
					if (rNode.hasProperty("sramp:generic")) {
						generic = rNode.getProperty("sramp:generic").getBoolean();
					}
					if (!generic)
						continue;
					Relationship relationship = new Relationship();
					relationship.setRelationshipType(rtype);
					if (rNode.hasProperty("sramp:relationshipTarget")) {
						Property property = rNode.getProperty("sramp:relationshipTarget");
						Value[] values = property.getValues();
						for (Value value : values) {
							if (value.getType() == PropertyType.REFERENCE) {
								String targetUUID = referenceResolver.resolveReference(value);
								Target target = new Target();
								target.setValue(targetUUID);
								relationship.getRelationshipTarget().add(target);
							}
						}
					}

					artifact.getRelationship().add(relationship);
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
        artifact.setContentSize(getPropertyLength(jcrNode,"jcr:content/jcr:data"));
        artifact.setContentType(getProperty(jcrNode, "jcr:content/jcr:mimeType"));
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitXmlDocument(org.s_ramp.xmlns._2010.s_ramp.XmlDocument)
	 */
	@Override
	protected void visitXmlDocument(XmlDocument artifact) {
        artifact.setContentEncoding(getProperty(jcrNode, "sramp:contentEncoding"));
	}

	/**
     * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitUserDefined(org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType)
     */
    @Override
    protected void visitUserDefined(UserDefinedArtifactType artifact) {
        artifact.setUserType(getProperty(jcrNode, "sramp:userType"));
        artifact.getOtherAttributes().put(new QName(SrampConstants.SRAMP_CONTENT_SIZE), String.valueOf(getPropertyLength(jcrNode,"jcr:content/jcr:data")));
        artifact.getOtherAttributes().put(new QName(SrampConstants.SRAMP_CONTENT_TYPE), getProperty(jcrNode,"jcr:content/jcr:mimeType"));
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

    /**
     * Gets a single property from the given JCR node.  This returns null
     * if the property does not exist.
     * @param node the JCR node
     * @param propertyName the name of the property
     * @return the String value of the property
     */
    protected static final Long getPropertyLength(Node node, String propertyName) {
        return getPropertyLength(node, propertyName, null);
    }

    /**
     * Gets a single property from the given JCR node.  This returns a default value if
     * the property does not exist.
     * @param node the JCR node
     * @param propertyName the name of the property
     * @param defaultValue a default value if the property does not exist on the node
     * @return the String value of the property
     */
    protected static final Long getPropertyLength(Node node, String propertyName, Long defaultValue) {
        try {
            return node.getProperty(propertyName).getLength();
        } catch (ValueFormatException e) {
        } catch (PathNotFoundException e) {
        } catch (javax.jcr.RepositoryException e) {
        }
        return defaultValue;
    }

    /**
     * A simple interface used by this class to resolve JCR references into s-ramp artifact UUIDs.
     *
     * @author eric.wittmann@redhat.com
     */
    public static interface JCRReferenceResolver {

    	/**
    	 * Resolves a JCR reference into an s-ramp artifact UUID.
    	 * @param reference a JCR reference
    	 * @return the UUID of an s-ramp artifact (or null if it fails to resolve)
    	 */
    	public String resolveReference(Value reference);

    }

}
