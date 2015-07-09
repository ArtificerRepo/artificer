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

import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.shell.core.AddCommentCommand;
import org.artificer.shell.core.CreateArtifactCommand;
import org.artificer.shell.core.GetMetaDataCommand;
import org.artificer.shell.core.PropertyCommand;
import org.artificer.shell.core.QueryCommand;
import org.artificer.shell.core.RefreshMetaDataCommand;
import org.artificer.shell.core.ShowMetaDataCommand;
import org.artificer.shell.core.UpdateMetaDataCommand;
import org.artificer.shell.core.UploadArtifactCommand;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Comment;

import java.io.InputStream;

/**
 * @author Brett Meyer.
 */
public class TestCoreCommands extends AbstractCommandTest {

    @Test
    public void testGetMetaDataByUuid() throws Exception {
        prepare(GetMetaDataCommand.class);

        pushToOutput("getMetaData --uuid %s", artifact.getUuid());
        Mockito.verify(clientMock).getArtifactMetaData(artifact.getUuid());
        Assert.assertTrue(stream.toString().contains("Type: " + artifactType.getType()));
        Assert.assertTrue(stream.toString().contains("Model: " + artifactType.getModel()));
        Assert.assertTrue(stream.toString().contains("UUID: " + artifact.getUuid()));
    }

    @Test
    public void testGetMetaDataByFeed() throws Exception {
        prepare(GetMetaDataCommand.class, QueryCommand.class);

        pushToOutput("query /s-ramp");
        pushToOutput("getMetaData --feed %d", 1);
        Mockito.verify(clientMock).getArtifactMetaData(artifactType, artifact.getUuid());
        Assert.assertTrue(stream.toString().contains("Type: " + artifactType.getType()));
        Assert.assertTrue(stream.toString().contains("Model: " + artifactType.getModel()));
        Assert.assertTrue(stream.toString().contains("UUID: " + artifact.getUuid()));
    }

    @Test
    public void testAddComment() throws Exception {
        prepare(GetMetaDataCommand.class, RefreshMetaDataCommand.class, AddCommentCommand.class);

        // create artifact and load into shell context using getMetaData
        pushToOutput("getMetaData --uuid %s", artifact.getUuid());

        // setup
        Mockito.when(clientMock.addComment(
                Mockito.anyString(), Mockito.any(ArtifactType.class), Mockito.anyString())).thenReturn(artifact);

        // add comment
        pushToOutput("addComment 'Comment Test'");

        // add comment to mock
        Comment comment = new Comment();
        comment.setText("Comment Test");
        artifact.getComment().add(comment);

        // verify
        Mockito.verify(clientMock).addComment(artifact.getUuid(), artifactType, "Comment Test");
        Assert.assertTrue(stream.toString().contains("Comment successfully created"));
        pushToOutput("refreshMetaData");
        Assert.assertTrue(stream.toString().contains(": Comment Test"));
    }

    @Test
    public void testCreateArtifact() throws Exception {
        prepare(CreateArtifactCommand.class);

        // failure tests
        pushToOutput("createArtifact --type ComplexTypeDeclaration --name FooName");
        Assert.assertTrue(stream.toString().contains("The artifact you are trying to create is a derived artifact"));
        pushToOutput("createArtifact --type XmlDocument --name FooName");
        Assert.assertTrue(stream.toString().contains("The artifact you are trying to create is a document artifact"));

        // the initial create needs to return an artifact, its then used to set the context
        BaseArtifactType extendedArtifact = ArtifactType.ExtendedArtifactType("FooType").newArtifactInstance();
        extendedArtifact.setName("FooName");
        Mockito.when(clientMock.createArtifact(Mockito.any(BaseArtifactType.class))).thenReturn(extendedArtifact);

        // success tests
        pushToOutput("createArtifact --type FooType --name FooName --description FooDescription");
        ArgumentCaptor<BaseArtifactType> artifact = ArgumentCaptor.forClass(BaseArtifactType.class);
        Mockito.verify(clientMock, Mockito.times(1)).createArtifact(artifact.capture());
        Assert.assertEquals("FooName", artifact.getValue().getName());
        Assert.assertEquals("FooDescription", artifact.getValue().getDescription());
        Assert.assertEquals("FooType", ArtifactType.valueOf(artifact.getValue()).getExtendedType());

        // ensure it was set as the current artifact in the context
        Assert.assertEquals("FooName", getAeshContext().getCurrentArtifact().getName());
    }

    @Test
    public void testUploadArtifact() throws Exception {
        prepare(UploadArtifactCommand.class);

        // failure tests
        pushToOutput("uploadArtifact --type XmlDocument nope.xml");
        Assert.assertTrue(stream.toString().contains("File not found"));

        // the initial upload needs to return an artifact, since the command later modifies its custom properties and updates
        BaseArtifactType xmlDocument = ArtifactType.XmlDocument().newArtifactInstance();
        Mockito.when(clientMock.uploadArtifact(
                Mockito.any(ArtifactType.class), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(xmlDocument);

        // success tests
        pushToOutput("uploadArtifact --type XmlDocument --name FooName --description FooDescription PO.xml");

        ArgumentCaptor<ArtifactType> artifactType = ArgumentCaptor.forClass(ArtifactType.class);
        ArgumentCaptor<String> fileName = ArgumentCaptor.forClass(String.class);
        Mockito.verify(clientMock, Mockito.times(1)).uploadArtifact(
                artifactType.capture(), Mockito.any(InputStream.class), fileName.capture());
        Assert.assertEquals(ArtifactType.XmlDocument(), artifactType.getValue());
        Assert.assertEquals("PO.xml", fileName.getValue());

        ArgumentCaptor<BaseArtifactType> artifact = ArgumentCaptor.forClass(BaseArtifactType.class);
        Mockito.verify(clientMock, Mockito.times(1)).updateArtifactMetaData(artifact.capture());
        Assert.assertEquals("FooName", artifact.getValue().getName());
        Assert.assertEquals("FooDescription", artifact.getValue().getDescription());

        Assert.assertTrue(stream.toString().contains("Successfully uploaded the artifact"));

        // ensure it was set as the current artifact in the context
        Assert.assertEquals("FooName", getAeshContext().getCurrentArtifact().getName());
    }

    @Test
    public void testUpdateMetaData() throws Exception {
        prepare(UpdateMetaDataCommand.class, GetMetaDataCommand.class);

        // failure tests
        pushToOutput("updateMetaData");
        Assert.assertTrue(stream.toString().contains("No active Artificer artifact exists"));

        // populate the context
        pushToOutput("getMetaData --uuid %s", artifact.getUuid());

        // success tests
        pushToOutput("updateMetaData");
        ArgumentCaptor<BaseArtifactType> updatedArtifact = ArgumentCaptor.forClass(BaseArtifactType.class);
        Mockito.verify(clientMock, Mockito.times(1)).updateArtifactMetaData(updatedArtifact.capture());
        Assert.assertEquals(artifact.getUuid(), updatedArtifact.getValue().getUuid());
        Assert.assertTrue(stream.toString().contains("Successfully updated artifact"));
    }

    @Test
    public void testRefreshMetaData() throws Exception {
        prepare(RefreshMetaDataCommand.class, GetMetaDataCommand.class);

        // failure tests
        pushToOutput("refreshMetaData");
        Assert.assertTrue(stream.toString().contains("No active Artificer artifact exists"));

        // populate the context
        pushToOutput("getMetaData --uuid %s", artifact.getUuid());

        // success tests
        pushToOutput("refreshMetaData");
        ArgumentCaptor<ArtifactType> type = ArgumentCaptor.forClass(ArtifactType.class);
        ArgumentCaptor<String> uuid = ArgumentCaptor.forClass(String.class);
        Mockito.verify(clientMock, Mockito.times(1)).getArtifactMetaData(type.capture(), uuid.capture());
        Assert.assertEquals(artifact.getArtifactType().value(), type.getValue().getArtifactType().getApiType().value());
        Assert.assertEquals(artifact.getUuid(), uuid.getValue());

        Assert.assertTrue(stream.toString().contains("Successfully refreshed meta-data for artifact"));
        Assert.assertTrue(stream.toString().contains("Type: " + artifactType.getType()));
        Assert.assertTrue(stream.toString().contains("Model: " + artifactType.getModel()));
        Assert.assertTrue(stream.toString().contains("UUID: " + artifact.getUuid()));
    }

    @Test
    public void testShowMetaData() throws Exception {
        prepare(ShowMetaDataCommand.class, GetMetaDataCommand.class);

        // failure tests
        pushToOutput("showMetaData");
        Assert.assertTrue(stream.toString().contains("No active Artificer artifact exists"));

        // populate the context
        pushToOutput("getMetaData --uuid %s", artifact.getUuid());

        // success tests
        pushToOutput("showMetaData");
        Assert.assertTrue(stream.toString().contains("Type: " + artifactType.getType()));
        Assert.assertTrue(stream.toString().contains("Model: " + artifactType.getModel()));
        Assert.assertTrue(stream.toString().contains("UUID: " + artifact.getUuid()));
    }

    @Test
    public void testProperty() throws Exception {
        prepare(PropertyCommand.class, GetMetaDataCommand.class);

        // failure tests
        pushToOutput("property set name NewFooName");
        Assert.assertTrue(stream.toString().contains("No active Artificer artifact exists"));

        // populate the context
        pushToOutput("getMetaData --uuid %s", artifact.getUuid());

        // success tests
        pushToOutput("property set name NewFooName");
        Assert.assertEquals("NewFooName", aeshContext.getCurrentArtifact().getName());
        pushToOutput("property set fooProperty NewFooValue");
        Assert.assertEquals("NewFooValue", ArtificerModelUtils.getCustomProperty(aeshContext.getCurrentArtifact(), "fooProperty"));
        pushToOutput("property unset name NewFooName");
        Assert.assertNull(aeshContext.getCurrentArtifact().getName());
        pushToOutput("property unset fooProperty NewFooValue");
        Assert.assertNull(ArtificerModelUtils.getCustomProperty(aeshContext.getCurrentArtifact(), "fooProperty"));
    }
}
