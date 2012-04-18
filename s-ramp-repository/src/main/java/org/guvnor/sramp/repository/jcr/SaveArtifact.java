package org.guvnor.sramp.repository.jcr;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;



public class SaveArtifact {

    public void save(XsdDocument xsdDocument) {
        
        Session session;
        try {
            session = JCRRepository.getSession();
            Node root = session.getRootNode();
            NodeIterator nodeIterator = root.getNodes();
            while (nodeIterator.hasNext()) {
                Node node = (Node) nodeIterator.next();
                System.out.println("Node " + node.getName());
            }
            
            session.logout();
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
