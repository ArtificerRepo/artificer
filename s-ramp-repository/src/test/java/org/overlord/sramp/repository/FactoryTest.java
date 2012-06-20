package org.overlord.sramp.repository;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.repository.DerivedArtifacts;
import org.overlord.sramp.repository.DerivedArtifactsFactory;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.jcr.JCRPersistence;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class FactoryTest {
    
    @Test
    public void testFindServiceConfig() {
        URL url = this.getClass().getClassLoader().getResource("META-INF/services/org.overlord.sramp.repository.DerivedArtifacts");
        System.out.println("URL=" + url);
        Assert.assertNotNull(url);
    }
    
    @Test
    public void testPersistenceFactory() throws Exception {
        PersistenceManager persistenceManager = PersistenceFactory.newInstance();
        Assert.assertEquals(JCRPersistence.class, persistenceManager.getClass());
    }
    
    @Test
    public void testDerivedArtifactsFactory() throws Exception {
        DerivedArtifacts derivedArtifacts = DerivedArtifactsFactory.newInstance();
        Assert.assertEquals(JCRPersistence.class, derivedArtifacts.getClass());
    }
    
   
}
