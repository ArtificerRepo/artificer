package org.guvnor.sramp.repository.jcr;

import junit.framework.Assert;

import org.guvnor.sramp.repository.UnsupportedFiletypeException;
import org.junit.Test;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class MapToJCRPathTest {
    

    
    @Test
    public void testExtentions() {
        Assert.assertEquals("xsd", MapToJCRPath.getFileExtension("PO.xsd"));
        Assert.assertEquals("xml", MapToJCRPath.getFileExtension("PO.xml"));
    }
    
    @Test
    public void testArtifactMap() throws UnsupportedFiletypeException {
        Assert.assertEquals("/artifact/xsd/PO.xsd", MapToJCRPath.getArtifactPath("PO.xsd"));
        Assert.assertEquals("/artifact/xml/PO.xml", MapToJCRPath.getArtifactPath("PO.xml"));
     
    }
    
    @Test
    public void testDerivedArtifactMap() throws UnsupportedFiletypeException {
        Assert.assertEquals("/s-ramp/xsd/XsdDocument/PO.xsd", MapToJCRPath.getDerivedArtifactPath("PO.xsd"));
        Assert.assertEquals("/s-ramp/xml/XmlDocument/PO.xml", MapToJCRPath.getDerivedArtifactPath("PO.xml"));
    }
}
