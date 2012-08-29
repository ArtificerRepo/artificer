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
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.overlord.sramp.repository.jcr.JCRConstants;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Handles mapping meta data common to all artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class BaseArtifactModel {
	
	/**
	 * Maps the base artifact model meta data (from the JCR node to the s-ramp artifact).
	 * @param jcrNode
	 * @param artifact
	 * @throws DatatypeConfigurationException 
	 * @throws RepositoryException 
	 */
	protected static void mapBaseArtifactMetaData(Node jcrNode, BaseArtifactType artifact) throws DatatypeConfigurationException, RepositoryException {
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
        
    	String srampPropsPrefix = JCRConstants.SRAMP_PROPERTIES + ":";
        PropertyIterator properties = jcrNode.getProperties();
        while (properties.hasNext()) {
        	Property property = properties.nextProperty();
        	String propQName = property.getName();
			if (propQName.startsWith(srampPropsPrefix)) {
				String propName = propQName.substring(7);
	        	String propValue = property.getValue().getString();
	        	org.s_ramp.xmlns._2010.s_ramp.Property srampProp = new org.s_ramp.xmlns._2010.s_ramp.Property();
	        	srampProp.setPropertyName(propName);
	        	srampProp.setPropertyValue(propValue);
				artifact.getProperty().add(srampProp);
        	}
        }
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
		} catch (RepositoryException e) {
		}
		return defaultValue;
    }

}
