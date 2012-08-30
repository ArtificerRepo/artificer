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

import org.overlord.sramp.repository.DerivedArtifactsCreationException;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

public class XsdModel extends XmlModel {

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
        	mapBaseArtifactMetaData(derivedNode, xsdDocument);
        	mapDocumentArtifactMetaData(derivedNode, xsdDocument);
        	mapXmlDocumentArtifactMetaData(derivedNode, xsdDocument);
            
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
   
}
