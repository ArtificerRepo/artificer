/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.ui.server.services;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit test for the {@link ArtifactTypeGuessingService}.
 * @author eric.wittmann@redhat.com
 */
public class ArtifactTypeGuessingServiceTest {

    /**
     * Test method for {@link org.overlord.sramp.ui.server.services.ArtifactTypeGuessingService#guess(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGuess() {
        ArtifactTypeGuessingService service = new ArtifactTypeGuessingService();
        Assert.assertEquals("SwitchYardXmlDocument", service.guess("switchyard.xml"));
        Assert.assertEquals("MavenPom", service.guess("pom.xml"));
        Assert.assertEquals("XmlDocument", service.guess("my-file.xml"));
        Assert.assertEquals("WsdlDocument", service.guess("some-service.wsdl"));
        Assert.assertEquals("XsdDocument", service.guess("datatype.xsd"));
        Assert.assertEquals("SrampArchive", service.guess("my-archive.sramp"));
        Assert.assertEquals("ZipArchive", service.guess("my-archive.zip"));
        Assert.assertEquals("Document", service.guess("spring-break.png"));
    }

}
