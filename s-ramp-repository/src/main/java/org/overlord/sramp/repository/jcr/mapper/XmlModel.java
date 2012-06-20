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
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;

public class XmlModel {

    /**
     * Input is the root node of the derived xsd data
     * @throws DerivedArtifactsCreationException 
     * @throws RepositoryException 
     * @throws PathNotFoundException 
     * @throws ValueFormatException 
     */
    public static  XmlDocument getXmlDocument(Node derivedNode) throws DerivedArtifactsCreationException {
        XmlDocument xmlDocument = new XmlDocument();
        
        try {
            xmlDocument.setContentEncoding(derivedNode.getProperty("sramp:contentEncoding").getString());
            xmlDocument.setContentSize(Long.valueOf(derivedNode.getProperty("sramp:contentSize").getString()));
            xmlDocument.setContentType(derivedNode.getProperty("sramp:contentType").getString());
            xmlDocument.setCreatedBy(derivedNode.getProperty("jcr:createdBy").getString());
            XMLGregorianCalendar createdTS;
            createdTS = DatatypeFactory.newInstance().newXMLGregorianCalendar(derivedNode.getProperty("jcr:created").getString());
            xmlDocument.setCreatedTimestamp(createdTS);
            xmlDocument.setDescription(derivedNode.getProperty("sramp:description").getString());
            xmlDocument.setLastModifiedBy(derivedNode.getProperty("jcr:lastModifiedBy").getString());
            XMLGregorianCalendar modifiedTS = DatatypeFactory.newInstance().newXMLGregorianCalendar(derivedNode.getProperty("jcr:lastModified").getString());
            xmlDocument.setLastModifiedTimestamp(modifiedTS);
            xmlDocument.setName(derivedNode.getName());
            xmlDocument.setUuid(derivedNode.getProperty("uuid").getString());
            xmlDocument.setVersion(derivedNode.getProperty("version").getString());
            
            
        } catch (Exception e) {
            throw new DerivedArtifactsCreationException(e.getMessage(),e);
        }
        return xmlDocument;
    }

   
}
