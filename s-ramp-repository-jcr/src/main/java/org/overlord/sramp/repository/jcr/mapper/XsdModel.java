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
package org.overlord.sramp.repository.jcr.mapper;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.overlord.sramp.repository.DerivedArtifactsCreationException;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

public class XsdModel {

    /**
     * Input is the root node of the derived xsd data
     * @throws DerivedArtifactsCreationException 
     * @throws RepositoryException 
     * @throws PathNotFoundException 
     * @throws ValueFormatException 
     */
    public static XsdDocument getXsdDocument(Node derivedNode) throws DerivedArtifactsCreationException {
        XsdDocument xsdDocument = new XsdDocument();
        
        try {
            xsdDocument.setContentEncoding(getProperty(derivedNode, "sramp:contentEncoding"));
            xsdDocument.setContentSize(Long.valueOf(getProperty(derivedNode, "sramp:contentSize")));
            xsdDocument.setContentType(getProperty(derivedNode, "sramp:contentType"));
            xsdDocument.setCreatedBy(getProperty(derivedNode, "jcr:createdBy"));
            XMLGregorianCalendar createdTS;
            createdTS = DatatypeFactory.newInstance().newXMLGregorianCalendar(getProperty(derivedNode, "jcr:created"));
            xsdDocument.setCreatedTimestamp(createdTS);
            xsdDocument.setDescription(getProperty(derivedNode, "sramp:description"));
            xsdDocument.setLastModifiedBy(getProperty(derivedNode, "jcr:lastModifiedBy"));
            XMLGregorianCalendar modifiedTS = DatatypeFactory.newInstance().newXMLGregorianCalendar(getProperty(derivedNode, "jcr:lastModified"));
            xsdDocument.setLastModifiedTimestamp(modifiedTS);
            xsdDocument.setName(getProperty(derivedNode, "sramp:name"));
            xsdDocument.setUuid(getProperty(derivedNode, "sramp:uuid"));
            xsdDocument.setVersion(getProperty(derivedNode, "version"));
            
            //TODO
            //xsdDocument.getImportedXsds()
            //xsdDocument.getIncludedXsds()
            //xsdDocument.getRedefinedXsds()
            //xsdDocument.getOtherAttributes()
            //xsdDocument.getProperty()
            //xsdDocument.getOtherAttributes()
            //xsdDocument.getRelationship()
            
        } catch (Exception e) {
            throw new DerivedArtifactsCreationException(e.getMessage(),e);
        }
        
        return xsdDocument;
    }

    /**
     * Gets a single property from the given JCR node.  This returns null
     * if the property does not exist.
     * @param node the JCR node
     * @param propertyName the name of the property
     * @return the String value of the property
     */
    static final String getProperty(Node node, String propertyName) {
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
    static final String getProperty(Node node, String propertyName, String defaultValue) {
    	try {
			return node.getProperty(propertyName).getString();
		} catch (ValueFormatException e) {
		} catch (PathNotFoundException e) {
		} catch (RepositoryException e) {
		}
		return defaultValue;
    }
   
}
