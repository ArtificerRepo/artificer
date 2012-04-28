package org.guvnor.sramp.repository.jcr;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

public class SequencingListener implements EventListener {

    public ConcurrentHashMap<String, String> sequencedNodePaths = new ConcurrentHashMap<String, String>();
    public ConcurrentHashMap<String, CountDownLatch> waitingLatches = new ConcurrentHashMap<String, CountDownLatch>();
  
    @Override
    public void onEvent( EventIterator events ) {
        while (events.hasNext()) {
            try {
                Event event = (Event)events.nextEvent();
                String nodePath = event.getPath();
                System.out.println("Received created sequenced node event at: " + nodePath);
                sequencedNodePaths.put(nodePath, nodePath);
                // signal the node is available
                waitingLatches.get(nodePath).countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
