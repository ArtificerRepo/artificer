package org.guvnor.sramp.repository.jcr;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.guvnor.sramp.repository.DerivedArtifacts;
import org.guvnor.sramp.repository.DerivedArtifactsCreationException;
import org.guvnor.sramp.repository.PersistenceManager;
import org.guvnor.sramp.repository.UnsupportedFiletypeException;
import org.guvnor.sramp.repository.jcr.mapper.XmlModel;
import org.guvnor.sramp.repository.jcr.mapper.XsdModel;
import org.modeshape.jcr.api.JcrTools;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRPersistence implements PersistenceManager, DerivedArtifacts {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public String persistArtifact(String artifactFileName, InputStream artifactStream) throws UnsupportedFiletypeException {
        Session session = null;
        String uuid = null;
        try {
            session = JCRRepository.getSession();
            JcrTools tools = new JcrTools();
            String artifactPath = MapToJCRPath.getArtifactPath(artifactFileName);
            log.debug("Uploading file {} to JCR.",artifactFileName);
            Node artifactNode = tools.uploadFile(session, artifactPath, artifactStream);
            uuid = artifactNode.getIdentifier();
            log.debug("Successfully saved {} to node={}",artifactFileName, uuid);
            JCRRepository.getListener().addWaitingLatch(artifactFileName);
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
        return uuid;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T createDerivedArtifact(Class<T> entityClass, String artifactFileName) throws DerivedArtifactsCreationException, UnsupportedFiletypeException {
        Session session = null;
        T baseArtifactType = null;
        try {
            session = JCRRepository.getSession();
            SequencingListener listener = JCRRepository.getListener();
            listener.waitForLatch(artifactFileName);
            String derivedNodePath = MapToJCRPath.getDerivedArtifactPath(artifactFileName);
            Node derivedNode = session.getNode(derivedNodePath);
            //if (log.isDebugEnabled()) {
                JcrTools tools = new JcrTools();
                tools.printSubgraph(derivedNode);
            //}
            if (entityClass.equals(XsdDocument.class)) {
                baseArtifactType = (T) XsdModel.getXsdDocument(derivedNode);
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

    @Override
    public String persistDerivedArtifact(BaseArtifactType baseArtifactType) {
        //When using modeshape it is already persisted
        return baseArtifactType.getUuid();
    }
    
    @Override
    public void printArtifactGraph(String uuid) {
        Session session = null;
        
        try {
            session = JCRRepository.getSession();
            Node derivedNode = session.getNodeByIdentifier(uuid);
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
