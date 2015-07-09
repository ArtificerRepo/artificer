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
package org.artificer.shell;

import org.artificer.client.ArtificerClientQuery;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.integration.java.model.JavaModel;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.InputStream;

/**
 * @author Brett Meyer.
 */
public class TestMavenCommands extends AbstractCommandTest {

    @Test
    public void testDeploy() throws Exception {
        prepare(ArtificerShell.MavenCommands.class);

        // failure tests
        pushToOutput("maven deploy --type XmlDocument --gav org.artificer.shell.test:maven-test:1.0.0.Final nope.xml");
        Assert.assertTrue(stream.toString().contains("File not found"));
        pushToOutput("maven deploy --type XmlDocument --gav org.artificer.shell.test:maven-test:1.0.0.Final PO");
        Assert.assertTrue(stream.toString().contains("does not include an extension, the Maven GAV argument must include the type"));

        // called by DeployCommand#findExistingArtifactByGAV
        ArtificerClientQuery clientQuery = Mockito.mock(ArtificerClientQuery.class);
        Mockito.when(clientQuery.count(Mockito.anyInt())).thenReturn(clientQuery);
        QueryResultSet resultSet = Mockito.mock(QueryResultSet.class);
        Mockito.when(resultSet.size()).thenReturn(0l);
        Mockito.when(clientQuery.query()).thenReturn(resultSet);
        Mockito.when(clientMock.buildQuery(Mockito.anyString())).thenReturn(clientQuery);

        // the initial upload needs to return an artifact, since the command later modifies its custom properties and updates
        BaseArtifactType xmlDocument = ArtifactType.XmlDocument().newArtifactInstance();
        Mockito.when(clientMock.uploadArtifact(
                Mockito.any(ArtifactType.class), Mockito.any(InputStream.class), Mockito.anyString()))
                        .thenReturn(xmlDocument);

        // success tests
        pushToOutput("maven deploy --type XmlDocument --gav org.artificer.shell.test:maven-test:1.0.0.Final PO.xml");

        ArgumentCaptor<ArtifactType> artifactType = ArgumentCaptor.forClass(ArtifactType.class);
        ArgumentCaptor<String> fileName = ArgumentCaptor.forClass(String.class);
        Mockito.verify(clientMock, Mockito.times(1)).uploadArtifact(
                artifactType.capture(), Mockito.any(InputStream.class), fileName.capture());
        Assert.assertEquals(ArtifactType.XmlDocument(), artifactType.getValue());
        Assert.assertEquals("PO.xml", fileName.getValue());

        ArgumentCaptor<BaseArtifactType> artifact = ArgumentCaptor.forClass(BaseArtifactType.class);
        Mockito.verify(clientMock, Mockito.times(1)).updateArtifactMetaData(artifact.capture());
        Assert.assertEquals("org.artificer.shell.test",
                ArtificerModelUtils.getCustomProperty(artifact.getValue(), JavaModel.PROP_MAVEN_GROUP_ID));
        Assert.assertEquals("maven-test",
                ArtificerModelUtils.getCustomProperty(artifact.getValue(), JavaModel.PROP_MAVEN_ARTIFACT_ID));
        Assert.assertEquals("1.0.0.Final",
                ArtificerModelUtils.getCustomProperty(artifact.getValue(), JavaModel.PROP_MAVEN_VERSION));

        artifact = ArgumentCaptor.forClass(BaseArtifactType.class);
        Mockito.verify(clientMock, Mockito.times(1)).uploadArtifact(artifact.capture(), Mockito.any(InputStream.class));
        Assert.assertEquals(ArtifactType.ExtendedDocument(JavaModel.TYPE_MAVEN_POM_XML),
                ArtifactType.valueOf(artifact.getValue()));
        Assert.assertEquals("pom",
                ArtificerModelUtils.getCustomProperty(artifact.getValue(), JavaModel.PROP_MAVEN_TYPE));

        Assert.assertTrue(stream.toString().contains("Successfully deployed the artifact"));

        // ensure it was set as the current artifact in the context
        Assert.assertEquals("maven-test",
                ArtificerModelUtils.getCustomProperty(getAeshContext().getCurrentArtifact(), JavaModel.PROP_MAVEN_ARTIFACT_ID));
    }
}
