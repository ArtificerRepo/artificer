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

import org.apache.commons.io.IOUtils;
import org.artificer.client.ontology.OntologySummary;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Brett Meyer.
 */
public class TestOntologyCommands extends AbstractCommandTest {

    private static final String RANDOM_UUID = UUID.randomUUID().toString();

    private final URL ontology = getClass().getClassLoader().getResource("regional.owl.xml");

    private String capturedString = null;

    @Before
    public void setup() throws Exception {
        OntologySummary summary = Mockito.mock(OntologySummary.class);
        Mockito.when(summary.getUuid()).thenReturn(RANDOM_UUID);
        List<OntologySummary> results = new ArrayList<>();
        results.add(summary);
        Mockito.when(clientMock.getOntologies()).thenReturn(results);

        RDF rdf = Mockito.mock(RDF.class);
        Mockito.when(clientMock.getOntology(Mockito.eq(RANDOM_UUID))).thenReturn(rdf);
    }

    @Test
    public void testUploadOntology() throws Exception {
        prepare(ArtificerShell.OntologyCommands.class);

        // capture the InputStream argument before the client closes it
        Mockito.when(clientMock.uploadOntology(Mockito.any(InputStream.class))).thenAnswer(new Answer<RDF>() {
            @Override
            public RDF answer(InvocationOnMock invocation) throws Throwable {
                try {
                    capturedString = IOUtils.toString((InputStream) invocation.getArguments()[0]);
                } catch (Exception e) {
                    // eat it
                }
                return null;
            }
        });

        // verify success
        pushToOutput("ontology upload regional.owl.xml");
        Mockito.verify(clientMock).uploadOntology(Mockito.any(InputStream.class));
        Assert.assertEquals(IOUtils.toString(ontology), capturedString);
        Assert.assertTrue(stream.toString().contains("Successfully uploaded a new ontology to the Artificer repository"));

        // verify failure
        pushToOutput("ontology upload nope.owl.xml");
        Assert.assertTrue(stream.toString().contains("ERROR: Cannot find nope.owl.xml"));

        capturedString = null;
    }

    @Test
    public void testUpdateOntology() throws Exception {
        prepare(ArtificerShell.OntologyCommands.class);

        // failure tests
        pushToOutput("ontology update regional.owl.xml");
        Assert.assertTrue(stream.toString().contains("Must include either an artifact UUID or a feed index"));
        pushToOutput("ontology update --feed 1 regional.owl.xml");
        Assert.assertTrue(stream.toString().contains("There is no ontology feed available"));

        // populate the context
        pushToOutput("ontology list");

        // capture the InputStream argument before the client closes it
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                try {
                    capturedString = IOUtils.toString((InputStream) invocation.getArguments()[1]);
                } catch (Exception e) {
                    // eat it
                }
                return null;
            }
        }).when(clientMock).updateOntology(Mockito.anyString(), Mockito.any(InputStream.class));

        // verify success (feed)
        pushToOutput("ontology update --feed 1 regional.owl.xml");
        ArgumentCaptor<String> s = ArgumentCaptor.forClass(String.class);
        Mockito.verify(clientMock, Mockito.times(1)).updateOntology(s.capture(), Mockito.any(InputStream.class));
        verifyOntologyUpload(s.getValue());

        capturedString = null;

        // verify success (uuid)
        pushToOutput("ontology update --uuid " + RANDOM_UUID + " regional.owl.xml");
        s = ArgumentCaptor.forClass(String.class);
        Mockito.verify(clientMock, Mockito.times(2)).updateOntology(s.capture(), Mockito.any(InputStream.class));

        capturedString = null;
    }

    private void verifyOntologyUpload(String uuid) throws Exception {
        Assert.assertEquals(RANDOM_UUID, uuid);
        Assert.assertEquals(IOUtils.toString(ontology), capturedString);
        Assert.assertTrue(stream.toString().contains("Successfully updated an ontology in the Artificer repository"));
    }

    @Test
    public void testGetOntology() throws Exception {
        prepare(ArtificerShell.OntologyCommands.class);

        // failure tests
        pushToOutput("ontology get --feed 1");
        Assert.assertTrue(stream.toString().contains("There is no ontology feed available"));

        // populate the context
        pushToOutput("ontology list");

        // success tests
        pushToOutput("ontology get --feed 1");
        // Seems stupid simple, but mostly interested in checking that the marshaller output kicked in.
        Assert.assertTrue(stream.toString().contains("RDF"));
        pushToOutput("ontology get --uuid " + RANDOM_UUID);
        // Seems stupid simple, but mostly interested in checking that the marshaller output kicked in.
        Assert.assertTrue(stream.toString().contains("RDF"));

        ArgumentCaptor<String> s = ArgumentCaptor.forClass(String.class);
        Mockito.verify(clientMock, Mockito.times(2)).getOntology(s.capture());
        Assert.assertEquals(RANDOM_UUID, s.getAllValues().get(0));
        Assert.assertEquals(RANDOM_UUID, s.getAllValues().get(1));
    }

    @Test
    public void testListOntologies() throws Exception {
        prepare(ArtificerShell.OntologyCommands.class);

        pushToOutput("ontology list");
        Assert.assertTrue(stream.toString().contains("Ontologies"));
        Assert.assertTrue(stream.toString().contains(RANDOM_UUID));
    }

    @Test
    public void testDeleteOntology() throws Exception {
        prepare(ArtificerShell.OntologyCommands.class);

        // failure tests
        pushToOutput("ontology delete");
        Assert.assertTrue(stream.toString().contains("Must include either an artifact UUID or a feed index"));
        pushToOutput("ontology delete --feed 1");
        Assert.assertTrue(stream.toString().contains("There is no ontology feed available"));

        // populate the context
        pushToOutput("ontology list");

        // success tests
        pushToOutput("ontology delete --feed 1");
        Assert.assertTrue(stream.toString().contains("Successfully deleted the ontology"));
        pushToOutput("ontology delete --uuid " + RANDOM_UUID);
        Assert.assertTrue(stream.toString().contains("Successfully deleted the ontology"));

        ArgumentCaptor<String> s = ArgumentCaptor.forClass(String.class);
        Mockito.verify(clientMock, Mockito.times(2)).deleteOntology(s.capture());
        Assert.assertEquals(RANDOM_UUID, s.getAllValues().get(0));
        Assert.assertEquals(RANDOM_UUID, s.getAllValues().get(1));
    }
}
