/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.shell;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Junit class that test the correct filtering of properties in a String
 * parameter.
 * 
 * @author David Virgil Naranjo
 */
public class AbstractShellCommandReaderTest {

    /**
     * Filter line.
     */
    @Test
    public void interpolate() {
        String line = "maven:deploy ${dtgov-workflow-jar} ${dt-workflows-groupId}:1.2.1-SNAPSHOT KieJarArchive"; //$NON-NLS-1$
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("dtgov-workflow-jar", "/home/test/dtgov-workflows.jar"); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("dt-workflows-groupId", "org.overlord.dtgov:dtgov-workflows"); //$NON-NLS-1$ //$NON-NLS-2$
        String filtered = AbstractShellCommandReader.interpolate(line, properties);
        Assert.assertEquals("maven:deploy /home/test/dtgov-workflows.jar org.overlord.dtgov:dtgov-workflows:1.2.1-SNAPSHOT KieJarArchive", filtered); //$NON-NLS-1$

        line = "maven:deploy ${dtgov-workflow-jar} ${dt-workflows-groupId}:1.2.1-SNAPSHOT KieJarArchive"; //$NON-NLS-1$
        properties.clear();
        System.setProperty("dtgov-workflow-jar", "/home/test/dtgov-workflows.jar"); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("dt-workflows-groupId", "org.overlord.dtgov:dtgov-workflows"); //$NON-NLS-1$ //$NON-NLS-2$
        filtered = AbstractShellCommandReader.interpolate(line, properties);
        Assert.assertEquals("maven:deploy /home/test/dtgov-workflows.jar org.overlord.dtgov:dtgov-workflows:1.2.1-SNAPSHOT KieJarArchive", filtered); //$NON-NLS-1$

        line = "connect ${sramp.endpoint:http://localhost:8080/s-ramp-server} ${sramp.username:admin} ${sramp.password:admin123}"; //$NON-NLS-1$
        properties.clear();
        filtered = AbstractShellCommandReader.interpolate(line, properties);
        Assert.assertEquals("connect http://localhost:8080/s-ramp-server admin admin123", filtered); //$NON-NLS-1$
        properties.put("sramp.endpoint", "http://localhost:8181/sramp"); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("sramp.username", "bwayne"); //$NON-NLS-1$ //$NON-NLS-2$
        filtered = AbstractShellCommandReader.interpolate(line, properties);
        Assert.assertEquals("connect http://localhost:8181/sramp bwayne admin123", filtered); //$NON-NLS-1$
    }

}
