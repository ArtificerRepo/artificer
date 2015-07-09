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

import org.artificer.common.error.ArtificerNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

/**
 * @author Brett Meyer.
 */
public class TestStoredQueryCommands extends AbstractCommandTest {

    @Test
    public void testCreateStoredQuery() throws Exception {
        prepare(ArtificerShell.StoredQueryCommands.class);

        // create stored query
        pushToOutput("storedQuery create --name %s --propertyNames %s %s", "FooQuery", "prop1,prop2,prop3", "/s-ramp/xsd/XsdDocument");

        // verify
        ArgumentCaptor<StoredQuery> storedQuery = ArgumentCaptor.forClass(StoredQuery.class);
        Mockito.verify(clientMock).createStoredQuery(storedQuery.capture());
        Assert.assertEquals("FooQuery", storedQuery.getValue().getQueryName());
        Assert.assertTrue(storedQuery.getValue().getPropertyName().contains("prop1"));
        Assert.assertTrue(storedQuery.getValue().getPropertyName().contains("prop2"));
        Assert.assertTrue(storedQuery.getValue().getPropertyName().contains("prop3"));
        Assert.assertEquals("/s-ramp/xsd/XsdDocument", storedQuery.getValue().getQueryExpression());
        Assert.assertTrue(stream.toString().contains("Successfully created the stored query"));
    }

    @Test
    public void testGetStoredQuery() throws Exception {
        prepare(ArtificerShell.StoredQueryCommands.class);

        StoredQuery storedQuery = storedQuery();
        Mockito.when(clientMock.getStoredQuery(Mockito.eq("FooQuery"))).thenReturn(storedQuery);
        Mockito.when(clientMock.getStoredQuery(Mockito.eq("DoesNotExist"))).thenThrow(
                ArtificerNotFoundException.storedQueryNotFound("DoesNotExist"));

        ArgumentCaptor<String> queryNames = ArgumentCaptor.forClass(String.class);

        // success test
        pushToOutput("storedQuery get FooQuery");
        Assert.assertTrue(stream.toString().contains("FooQuery"));

        // failure test
        pushToOutput("storedQuery get DoesNotExist");
        Assert.assertTrue(stream.toString().contains("DoesNotExist"));
        Assert.assertTrue(stream.toString().contains("Failed to retrieve the stored query"));

        Mockito.verify(clientMock, Mockito.times(2)).getStoredQuery(queryNames.capture());

        Assert.assertEquals("FooQuery", queryNames.getAllValues().get(0));
        Assert.assertEquals("DoesNotExist", queryNames.getAllValues().get(1));
    }

    @Test
    public void testExecuteStoredQuery() throws Exception {
        prepare(ArtificerShell.StoredQueryCommands.class);

        // NOTE: QueryResultSet set up by AbstractCommandTest (includes 1 XmlDocument)

        pushToOutput("storedQuery execute FooQuery");
        Mockito.verify(clientMock, Mockito.times(1)).queryWithStoredQuery(Mockito.eq("FooQuery"),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap());
        Assert.assertTrue(stream.toString().contains("Querying the Artificer repository"));
        Assert.assertTrue(stream.toString().contains("Paging: 1-1 of 1 entries"));
        Assert.assertTrue(stream.toString().contains("XmlDocument"));

        // verify it was set in the context
        Assert.assertNotNull(getAeshContext().getCurrentArtifactFeed());
        Assert.assertEquals(1, getAeshContext().getCurrentArtifactFeed().size());
    }

    @Test
    public void testUpdateStoredQuery() throws Exception {
        prepare(ArtificerShell.StoredQueryCommands.class);

        Mockito.doThrow(ArtificerNotFoundException.storedQueryNotFound("DoesNotExist"))
                .when(clientMock).updateStoredQuery(Mockito.eq("DoesNotExist"), Mockito.any(StoredQuery.class));

        ArgumentCaptor<String> queryNames = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<StoredQuery> storedQueries = ArgumentCaptor.forClass(StoredQuery.class);

        // success test
        pushToOutput("storedQuery update --name %s --propertyNames %s %s", "FooQuery", "prop1,prop2,prop3", "/s-ramp/xsd/XsdDocument");
        Assert.assertTrue(stream.toString().contains("FooQuery"));
        Assert.assertTrue(stream.toString().contains("Successfully updated the stored query"));

        // failure test
        pushToOutput("storedQuery update --name %s --propertyNames %s %s", "DoesNotExist", "prop1,prop2,prop3", "/s-ramp/xsd/XsdDocument");
        Assert.assertTrue(stream.toString().contains("DoesNotExist"));
        Assert.assertTrue(stream.toString().contains("Failed to update the stored query"));

        Mockito.verify(clientMock, Mockito.times(2)).updateStoredQuery(queryNames.capture(), storedQueries.capture());

        Assert.assertEquals("FooQuery", queryNames.getAllValues().get(0));
        Assert.assertEquals("DoesNotExist", queryNames.getAllValues().get(1));

        Assert.assertEquals("FooQuery", storedQueries.getAllValues().get(0).getQueryName());
        Assert.assertTrue(storedQueries.getAllValues().get(0).getPropertyName().contains("prop1"));
        Assert.assertTrue(storedQueries.getAllValues().get(0).getPropertyName().contains("prop2"));
        Assert.assertTrue(storedQueries.getAllValues().get(0).getPropertyName().contains("prop3"));
        Assert.assertEquals("/s-ramp/xsd/XsdDocument", storedQueries.getAllValues().get(0).getQueryExpression());
    }

    @Test
    public void testDeleteStoredQuery() throws Exception {
        prepare(ArtificerShell.StoredQueryCommands.class);

        Mockito.doThrow(ArtificerNotFoundException.storedQueryNotFound("DoesNotExist"))
                .when(clientMock).deleteStoredQuery(Mockito.eq("DoesNotExist"));

        ArgumentCaptor<String> queryNames = ArgumentCaptor.forClass(String.class);

        // success test
        pushToOutput("storedQuery delete FooQuery");
        Assert.assertTrue(stream.toString().contains("FooQuery"));
        Assert.assertTrue(stream.toString().contains("Successfully deleted the stored query"));

        // failure test
        pushToOutput("storedQuery delete DoesNotExist");
        Assert.assertTrue(stream.toString().contains("DoesNotExist"));
        Assert.assertTrue(stream.toString().contains("Failed to delete the stored query"));

        Mockito.verify(clientMock, Mockito.times(2)).deleteStoredQuery(queryNames.capture());

        Assert.assertEquals("FooQuery", queryNames.getAllValues().get(0));
        Assert.assertEquals("DoesNotExist", queryNames.getAllValues().get(1));
    }

    private StoredQuery storedQuery() {
        StoredQuery storedQuery = new StoredQuery();
        storedQuery.setQueryName("FooQuery");
        storedQuery.getPropertyName().add("prop1");
        storedQuery.getPropertyName().add("prop2");
        storedQuery.getPropertyName().add("prop3");
        storedQuery.setQueryExpression("/s-ramp/xsd/XsdDocument");
        return storedQuery;
    }
}