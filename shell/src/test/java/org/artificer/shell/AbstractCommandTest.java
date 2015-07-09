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

import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.query.ArtifactSummary;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.terminal.TestTerminal;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.junit.After;
import org.junit.Assert;
import org.mockito.Mockito;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.UUID;

/**
 * Taken and modified from aesh-example's AeshTestCommons.
 *
 * TODO: This should be replaced after https://issues.jboss.org/browse/AESH-328!
 *
 * @author Brett Meyer.
 */
public class AbstractCommandTest {

    private PipedOutputStream pos;
    private PipedInputStream pis;
    protected ByteArrayOutputStream stream;
    private Settings settings;
    private AeshConsole aeshConsole;
    private CommandRegistry registry;

    protected ArtificerAtomApiClient clientMock;
    protected ArtificerContext aeshContext;
    protected BaseArtifactType artifact;
    protected ArtifactType artifactType;

    public AbstractCommandTest() {
        pos = new PipedOutputStream();
        try {
            pis = new PipedInputStream(pos);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        stream = new ByteArrayOutputStream();

        clientMock = Mockito.mock(ArtificerAtomApiClient.class);

        settings = new SettingsBuilder()
                .terminal(new TestTerminal())
                .inputStream(pis)
                .outputStream(new PrintStream(stream))
                .logging(true)
                .aeshContext(getAeshContext())
                .create();
    }

    protected void prepare(Class<? extends Command>... commands) throws Exception {
        registry = new AeshCommandRegistryBuilder()
                .commands(commands)
                .create();
        AeshConsoleBuilder consoleBuilder = new AeshConsoleBuilder()
                .settings(settings)
                .commandRegistry(registry);
        aeshConsole = consoleBuilder.create();
        aeshConsole.start();
//        stream.flush();

        setupArtifact();
    }

    // TODO: Added arguments and the reset
    protected void pushToOutput(String command, Object... args) throws IOException {
        // First, reset the stream
        stream.reset();

        String literalCommand = String.format(command, args);
        pos.write((literalCommand).getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        smallPause();

        // A missing i18n value results in "!![key]!!" text, so simply ensure none exist in the stream.
        Assert.assertFalse("Missing an i18n entry!  Output: " + stream.toString(), stream.toString().contains("!!"));
    }

    @After
    public void after() {
        smallPause();
//        System.out.println("Got out: " + getStream().toString());
        aeshConsole.stop();
        Mockito.reset(clientMock);
    }

    // This seems unnecessary, but without it, commands appear to run in succession "too fast" and periodically fail.
    protected void smallPause() {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO: This was not a part of AeshTestCommons, but should be added to it so subclasses can override.  Note the
    protected ArtificerContext getAeshContext() {
        if (aeshContext == null) {
            aeshContext = new ArtificerContext();
            aeshContext.setClient(clientMock);
        }
        return aeshContext;
    }

    private BaseArtifactType setupArtifact() throws Exception {
        String uuid = UUID.randomUUID().toString();

        artifactType = ArtifactType.XmlDocument();
        artifact = artifactType.newArtifactInstance();
        artifact.setUuid(uuid);

        // getArtifactMetaData
        Mockito.when(clientMock.getArtifactMetaData(Mockito.contains(uuid))).thenReturn(artifact);
        Mockito.when(clientMock.getArtifactMetaData(
                Mockito.any(ArtifactType.class), Mockito.contains(uuid))).thenReturn(artifact);

        ArtifactSummary summary = new ArtifactSummary();
        summary.setUuid(uuid);
        summary.setModel("core");
        summary.setType("XmlDocument");
        Feed feed = new Feed();
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_ITEMS_PER_PAGE_QNAME, String.valueOf(1));
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_START_INDEX_QNAME, String.valueOf(1));
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_TOTAL_RESULTS_QNAME, String.valueOf(1));
        Entry entry = ArtificerAtomUtils.wrapArtifactSummary(summary);
        feed.getEntries().add(entry);
        QueryResultSet queryResult = new QueryResultSet(feed);
        // query
        Mockito.when(clientMock.query(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.anyBoolean())).thenReturn(queryResult);
        Mockito.when(clientMock.query(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyCollection())).thenReturn(queryResult);
        // stored query
        Mockito.when(clientMock.queryWithStoredQuery(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(queryResult);

        return artifact;
    }
}
