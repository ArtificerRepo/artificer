package org.guvnor.sramp.repository.jcr;

import java.io.InputStream;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import junit.framework.Assert;

import org.guvnor.sramp.repository.DerivedArtifactsCreationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.modeshape.jcr.api.JcrTools;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class JCRPersistenceTest {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    static Session session = null;

    @BeforeClass
    public static void before() throws LoginException, NoSuchWorkspaceException, RepositoryException {
        session = JCRRepository.getSession();
    }
    
    @AfterClass
    public static void after() {
        session.logout();
    }
    
    @Test
    public void testSavePO_XSD() throws Exception {
        
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        
        JCRPersistence persistence = new JCRPersistence();
        String uuid = persistence.persistArtifact(artifactFileName, POXsd);
        POXsd.close();
        log.info("persisted PO.xsd to JCR, returned UUID=" + uuid);
        Assert.assertNotNull(uuid);
        XsdDocument xsdDocument = persistence.createDerivedArtifact(XsdDocument.class, artifactFileName);
        String derivedUuid = persistence.persistDerivedArtifact(xsdDocument);
        
        //print out the derived node
        Node derivedNode = session.getNodeByIdentifier(derivedUuid);
        JcrTools tools = new JcrTools();
        tools.printSubgraph(derivedNode);
        
        Assert.assertEquals(new Long(2376l), xsdDocument.getContentSize());
        Assert.assertEquals("/s-ramp/xsd/XsdDocument/PO.xsd",derivedNode.getPath());
        
        System.out.println("XsdDocument = " + xsdDocument);
    }
    
    /**
     * For now expecting a DerivedArtifactsCreationException since the ModeShape
     * Sequencer for XML is not yet up deriving all the necessary artifacts.
     * @throws Exception
     */
    @Test(expected=DerivedArtifactsCreationException.class)
    public void testSavePO_XML() throws Exception {
        
        String artifactFileName = "PO.xml";
        InputStream POXml = this.getClass().getResourceAsStream("/sample-files/xml/" + artifactFileName);
        
        JCRPersistence persistence = new JCRPersistence();
        String uuid = persistence.persistArtifact(artifactFileName, POXml);
        POXml.close();
        log.info("persisted PO.xml to JCR, returned UUID=" + uuid);
        Assert.assertNotNull(uuid);
        XmlDocument xmlDocument = persistence.createDerivedArtifact(XmlDocument.class, artifactFileName);
        String derivedUuid = persistence.persistDerivedArtifact(xmlDocument);
        
        //print out the derived node
        Node derivedNode = session.getNodeByIdentifier(derivedUuid);
        JcrTools tools = new JcrTools();
        tools.printSubgraph(derivedNode);
        
        Assert.assertEquals(new Long(2376l), xmlDocument.getContentSize());
        Assert.assertEquals("/s-ramp/xml/XmlDocument/PO.xml",derivedNode.getPath());
        
        System.out.println("XmlDocument = " + xmlDocument);
    }
}
