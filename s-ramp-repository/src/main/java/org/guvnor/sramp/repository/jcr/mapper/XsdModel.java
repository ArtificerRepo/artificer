package org.guvnor.sramp.repository.jcr.mapper;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.guvnor.sramp.repository.DerivedArtifactsCreationException;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

public class XsdModel {

    /**
     * Input is the root node of the derived xsd data
     * @throws DerivedArtifactsCreationException 
     * @throws RepositoryException 
     * @throws PathNotFoundException 
     * @throws ValueFormatException 
     */
    public static  XsdDocument getXsdDocument(Node derivedNode) throws DerivedArtifactsCreationException {
        XsdDocument xsdDocument = new XsdDocument();
        
        try {
            xsdDocument.setContentEncoding(derivedNode.getProperty("sramp:contentEncoding").getString());
            xsdDocument.setContentSize(Long.valueOf(derivedNode.getProperty("sramp:contentSize").getString()));
            xsdDocument.setContentType(derivedNode.getProperty("sramp:contentType").getString());
            xsdDocument.setCreatedBy(derivedNode.getProperty("jcr:createdBy").getString());
            XMLGregorianCalendar createdTS;
            createdTS = DatatypeFactory.newInstance().newXMLGregorianCalendar(derivedNode.getProperty("jcr:created").getString());
            xsdDocument.setCreatedTimestamp(createdTS);
            xsdDocument.setDescription(derivedNode.getProperty("sramp:description").getString());
            xsdDocument.setLastModifiedBy(derivedNode.getProperty("jcr:lastModifiedBy").getString());
            XMLGregorianCalendar modifiedTS = DatatypeFactory.newInstance().newXMLGregorianCalendar(derivedNode.getProperty("jcr:lastModified").getString());
            xsdDocument.setLastModifiedTimestamp(modifiedTS);
            xsdDocument.setName(derivedNode.getName());
            xsdDocument.setUuid(derivedNode.getIdentifier());
            xsdDocument.setVersion(derivedNode.getProperty("version").getString());
            
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
