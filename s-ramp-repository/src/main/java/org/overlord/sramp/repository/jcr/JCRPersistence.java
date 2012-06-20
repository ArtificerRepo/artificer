package org.overlord.sramp.repository.jcr;

import static org.overlord.sramp.repository.jcr.JCRConstants.OVERLORD;
import static org.overlord.sramp.repository.jcr.JCRConstants.OVERLORD_ARTIFACT;
import static org.overlord.sramp.repository.jcr.JCRConstants.OVERLORD_FILENAME;
import static org.overlord.sramp.repository.jcr.JCRConstants.SRAMP_UUID;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.modeshape.jcr.api.JcrTools;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.overlord.sramp.repository.DerivedArtifacts;
import org.overlord.sramp.repository.DerivedArtifactsCreationException;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.UnsupportedFiletypeException;
import org.overlord.sramp.repository.jcr.mapper.XmlModel;
import org.overlord.sramp.repository.jcr.mapper.XsdModel;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRPersistence implements PersistenceManager, DerivedArtifacts {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    public JCRPersistence() {
        Session session = null;
        InputStream is = null;
        try {
            session = JCRRepository.getSession();
            NodeTypeManager manager = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();
            session.setNamespacePrefix(OVERLORD, "http://www.jboss.org/overlord/1.0");
            
            if (! manager.hasNodeType(OVERLORD_ARTIFACT)) {
                is = this.getClass().getResourceAsStream("/org/overlord/s-ramp/overlord.cnd");
                manager.registerNodeTypes(is,true);
            }
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (is !=null)
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            session.logout();
        }
    }
    @Override
    public String persistArtifact(String artifactFileName, String type, InputStream artifactStream) throws UnsupportedFiletypeException {
        Session session = null;
        String identifier = null;
        try {
            session = JCRRepository.getSession();
            JcrTools tools = new JcrTools();
            String uuid = UUID.randomUUID().toString();
            String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);
            log.debug("Uploading file {} to JCR.",artifactFileName);
            Node artifactNode = tools.uploadFile(session, artifactPath, artifactStream);
            identifier = artifactNode.getIdentifier();
            artifactNode.addMixin(OVERLORD_ARTIFACT);
            
            artifactNode.setProperty(SRAMP_UUID, uuid);
            artifactNode.setProperty(OVERLORD_FILENAME, artifactFileName);
            log.debug("Successfully saved {} to node={}",artifactFileName, uuid);
            JCRRepository.getListener().addWaitingLatch(MapToJCRPath.getDerivedArtifactPath(artifactPath));
            session.save();
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                artifactStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            session.logout();
        }
        return identifier;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T createDerivedArtifact(Class<T> entityClass, String identifier) throws DerivedArtifactsCreationException, UnsupportedFiletypeException {
        Session session = null;
        T baseArtifactType = null;
        try {
            session = JCRRepository.getSession();
            SequencingListener listener = JCRRepository.getListener();
            Node artifactNode = session.getNodeByIdentifier(identifier);
            String derivedNodePath = MapToJCRPath.getDerivedArtifactPath(artifactNode.getPath());
            listener.waitForLatch(derivedNodePath);
            Node derivedNode = session.getNode(derivedNodePath);
            derivedNode.addMixin(OVERLORD_ARTIFACT);
            String uuid = artifactNode.getProperty(SRAMP_UUID).getValue().getString();
            String filename = artifactNode.getProperty(OVERLORD_FILENAME).getValue().getString();
            derivedNode.setProperty(SRAMP_UUID, uuid);
            derivedNode.setProperty(OVERLORD_FILENAME, filename);
            session.save();
            //if (log.isDebugEnabled()) {
                JcrTools tools = new JcrTools();
                tools.printSubgraph(derivedNode);
            //}
            if (entityClass.equals(XsdDocument.class)) {
                XsdDocument xsdDocument = XsdModel.getXsdDocument(derivedNode);
                xsdDocument.setUuid(uuid);
                xsdDocument.setUuid(uuid);
                baseArtifactType = (T) xsdDocument;
                
            } else if (entityClass.equals(XmlDocument.class)) {
                baseArtifactType = (T) XmlModel.getXmlDocument(derivedNode);
            }
            return baseArtifactType;
        } catch (UnsupportedRepositoryOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            session.logout();
        }
        
        return null;
    }
    
    private String getType(Class<? extends BaseArtifactType> entityClass) {
        String type = null;
        if (entityClass.equals(XsdDocument.class)) {
            type = "xsd";
        } else if (entityClass.equals(XmlDocument.class)) {
            type = "xml";
        }
        return type;
    }

    @Override
    public String persistDerivedArtifact(BaseArtifactType baseArtifactType) {
        //When using modeshape it is already persisted
        String uuid = baseArtifactType.getUuid();
        String type = getType(baseArtifactType.getClass());
        String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);
        String derivedArtifactPath = MapToJCRPath.getDerivedArtifactPath(artifactPath);
        
        String identifier = null;
        Session session = null;
        try {
            session = JCRRepository.getSession();
            Node derivedArtifactNode = session.getNode(derivedArtifactPath);
            identifier = derivedArtifactNode.getIdentifier();
        } catch (PathNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            session.logout();
        }
        return identifier;
        
    }
    
    @Override
    public void printArtifactGraph(String identifier) {
        Session session = null;
        
        try {
            session = JCRRepository.getSession();
            Node derivedNode = session.getNodeByIdentifier(identifier);
            JcrTools tools = new JcrTools();
            tools.printSubgraph(derivedNode);
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            session.logout();
        }
    }

}
