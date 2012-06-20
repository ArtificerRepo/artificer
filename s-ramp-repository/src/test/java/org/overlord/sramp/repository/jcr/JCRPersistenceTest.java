package org.overlord.sramp.repository.jcr;

import java.io.InputStream;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.modeshape.jcr.api.JcrTools;
import org.overlord.sramp.repository.DerivedArtifactsCreationException;
import org.overlord.sramp.repository.jcr.JCRPersistence;
import org.overlord.sramp.repository.jcr.JCRRepository;
import org.overlord.sramp.repository.jcr.MapToJCRPath;
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
        String type = "xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        
        JCRPersistence persistence = new JCRPersistence();
        String id = persistence.persistArtifact(artifactFileName, type, POXsd);
        POXsd.close();
        log.info("persisted PO.xsd to JCR, returned ID=" + id);
        Assert.assertNotNull(id);
        Node artifactNode = session.getNodeByIdentifier(id);
        String derivedArtifactPath = MapToJCRPath.getDerivedArtifactPath(artifactNode.getPath());
        XsdDocument xsdDocument = persistence.createDerivedArtifact(XsdDocument.class, id);
        String derivedId = persistence.persistDerivedArtifact(xsdDocument);
        
        //print out the derived node
        Node derivedNode = session.getNodeByIdentifier(derivedId);
        JcrTools tools = new JcrTools();
        tools.printSubgraph(derivedNode);
        
        Assert.assertEquals(new Long(2376l), xsdDocument.getContentSize());
        Assert.assertEquals(derivedArtifactPath,derivedNode.getPath());
        
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
        String type = "xml";
        InputStream POXml = this.getClass().getResourceAsStream("/sample-files/xml/" + artifactFileName);
        
        JCRPersistence persistence = new JCRPersistence();
        String id = persistence.persistArtifact(artifactFileName, type, POXml);
        POXml.close();
        log.info("persisted PO.xml to JCR, returned ID=" + id);
        Assert.assertNotNull(id);
        Node artifactNode = session.getNodeByIdentifier(id);
        String derivedArtifactPath = MapToJCRPath.getDerivedArtifactPath(artifactNode.getPath());
        XmlDocument xmlDocument = persistence.createDerivedArtifact(XmlDocument.class, id);
        String derivedId = persistence.persistDerivedArtifact(xmlDocument);
        
        //print out the derived node
        Node derivedNode = session.getNodeByIdentifier(derivedId);
        JcrTools tools = new JcrTools();
        tools.printSubgraph(derivedNode);
        
        Assert.assertEquals(new Long(2376l), xmlDocument.getContentSize());
        Assert.assertEquals(derivedArtifactPath,derivedNode.getPath());
        
        System.out.println("XmlDocument = " + xmlDocument);
    }
}
