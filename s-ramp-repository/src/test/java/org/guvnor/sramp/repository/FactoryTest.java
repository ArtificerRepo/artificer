package org.guvnor.sramp.repository;

import java.net.URL;

import org.guvnor.sramp.repository.jcr.JCRPersistence;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class FactoryTest {
    
    @Test
    public void testFindServiceConfig() {
        URL url = this.getClass().getClassLoader().getResource("META-INF/services/org.guvnor.sramp.repository.DerivedArtifacts");
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
