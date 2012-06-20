package org.overlord.sramp.repository.jcr;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.modeshape.jcr.JcrSession;
import org.overlord.sramp.repository.UnsupportedFiletypeException;

public class SequencingListener implements EventListener {

    private ConcurrentHashMap<String, CountDownLatch> waitingLatches = new ConcurrentHashMap<String, CountDownLatch>();
  
    @Override
    public void onEvent( EventIterator events ) {
        while (events.hasNext()) {
            try {
                Event event = (Event)events.nextEvent();
                String derivedArtifactPath = event.getPath();
                System.out.println("Received created sequenced node event for: " + derivedArtifactPath);
                waitingLatches.get(derivedArtifactPath).countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public void addWaitingLatch(String path) throws UnsupportedFiletypeException {
        waitingLatches.putIfAbsent(path, new CountDownLatch(1));
    }
    
    public void waitForLatch(String path) throws InterruptedException, UnsupportedFiletypeException {
        waitingLatches.get(path).await(60, TimeUnit.SECONDS);
    }
    
    public Node getDerivedNode(String path, JcrSession session) throws PathNotFoundException, RepositoryException, UnsupportedFiletypeException {
        String derivedNodePath = MapToJCRPath.getDerivedArtifactPath(path);
        Node derivedNode = session.getNode(derivedNodePath);
        
        return derivedNode;
    }
    
    
    
    
}
