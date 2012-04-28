package org.guvnor.sramp.repository.jcr;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.guvnor.sramp.repository.UnsupportedFiletypeException;
import org.modeshape.jcr.JcrSession;

public class SequencingListener implements EventListener {

    private ConcurrentHashMap<String, CountDownLatch> waitingLatches = new ConcurrentHashMap<String, CountDownLatch>();
  
    @Override
    public void onEvent( EventIterator events ) {
        while (events.hasNext()) {
            try {
                Event event = (Event)events.nextEvent();
                String nodePath = event.getPath();
                System.out.println("Received created sequenced node event at: " + nodePath);
                waitingLatches.get(nodePath).countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public void addWaitingLatch(String artifactFileName) throws UnsupportedFiletypeException {
        String derivedNodePath = MapToJCRPath.getDerivedArtifactPath(artifactFileName);
        waitingLatches.putIfAbsent(derivedNodePath, new CountDownLatch(1));
    }
    
    public void waitForLatch(String artifactFileName) throws InterruptedException, UnsupportedFiletypeException {
        String derivedNodePath = MapToJCRPath.getDerivedArtifactPath(artifactFileName);
        waitingLatches.get(derivedNodePath).await(60, TimeUnit.SECONDS);
    }
    
    public Node getDerivedNode(String artifactFileName, JcrSession session) throws PathNotFoundException, RepositoryException, UnsupportedFiletypeException {
        String derivedNodePath = MapToJCRPath.getDerivedArtifactPath(artifactFileName);
        Node derivedNode = session.getNode(derivedNodePath);
        
        return derivedNode;
    }
    
    
    
    
}
