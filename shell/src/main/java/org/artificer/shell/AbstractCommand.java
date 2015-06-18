package org.artificer.shell;/*
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

import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.console.AeshConsoleBufferBuilder;
import org.jboss.aesh.console.AeshInputProcessorBuilder;
import org.jboss.aesh.console.ConsoleBuffer;
import org.jboss.aesh.console.InputProcessor;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.IOException;
import java.util.List;

/**
 * @author Brett Meyer.
 */
public abstract class AbstractCommand implements Command<CommandInvocation> {

//    @Option(shortName = 'h', name = "help", hasValue = false, required = false,
//            description = "Display help")
//    private boolean help;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {
        try {
//            if (help) {
//                return doHelp(commandInvocation);
//            }

            return doExecute(commandInvocation);
        } catch (Exception e) {
            commandInvocation.getShell().out().println(e.getMessage());
            return CommandResult.FAILURE;
        }
    }

    protected CommandResult doHelp(CommandInvocation commandInvocation) {
        commandInvocation.getShell().out().println(commandInvocation.getHelpInfo(getName()));
        return CommandResult.SUCCESS;
    }

    protected ArtificerContext context(CommandInvocation commandInvocation) {
        return (ArtificerContext) commandInvocation.getAeshContext();
    }

    protected String optionalArgument(List<String> arguments ,int argIndex) throws ArtificerShellException {
        if (arguments.size() <= argIndex) {
            return null;
        }
        return arguments.get(argIndex);
    }

    protected String requiredArgument(CommandInvocation commandInvocation, List<String> arguments ,int argIndex) throws ArtificerShellException {
        if (arguments.size() <= argIndex) {
            throw new ArtificerShellException(commandInvocation.getHelpInfo(getName()));
        }
        return arguments.get(argIndex);
    }

    protected ArtificerAtomApiClient client(CommandInvocation commandInvocation) throws ArtificerShellException {
        ArtificerAtomApiClient client = context(commandInvocation).getClient();
        if (client == null) {
            throw new ArtificerShellException(Messages.i18n.format("MissingArtificerConnection"));
        }
        return client;
    }

    protected BaseArtifactType artifact(CommandInvocation commandInvocation, String artifactUuid) throws Exception {
        return client(commandInvocation).getArtifactMetaData(artifactUuid);
    }

    protected ArtifactSummary artifactSummaryFromFeed(CommandInvocation commandInvocation, String feedIndex) throws Exception {
        QueryResultSet rset = currentArtifactFeed(commandInvocation);
        int feedIdx = Integer.parseInt(feedIndex) - 1;
        if (feedIdx < 0 || feedIdx >= rset.size()) {
            throw new ArtificerShellException(Messages.i18n.format("FeedIndexOutOfRange"));
        }
        return rset.get(feedIdx);
    }

    protected BaseArtifactType artifactFromFeed(CommandInvocation commandInvocation, String feedIndex) throws Exception {
        ArtifactSummary summary = artifactSummaryFromFeed(commandInvocation, feedIndex);
        String artifactUUID = summary.getUuid();
        return client(commandInvocation).getArtifactMetaData(summary.getArtifactType(), artifactUUID);
    }

    protected boolean hasCurrentArtifact(CommandInvocation commandInvocation) throws ArtificerShellException {
        return context(commandInvocation).getCurrentArtifact() != null;
    }

    protected BaseArtifactType currentArtifact(CommandInvocation commandInvocation) throws ArtificerShellException {
        BaseArtifactType artifact = context(commandInvocation).getCurrentArtifact();
        if (artifact == null) {
            throw new ArtificerShellException(Messages.i18n.format("NoActiveArtifact"));
        }
        return artifact;
    }

    protected QueryResultSet currentArtifactFeed(CommandInvocation commandInvocation) throws ArtificerShellException {
        QueryResultSet rset = context(commandInvocation).getCurrentArtifactFeed();
        if (rset == null) {
            throw new ArtificerShellException(Messages.i18n.format("NoFeed"));
        }
        return rset;
    }

    protected String promptForInput(String prompt, Character mask, CommandInvocation invocation)
            throws IOException, InterruptedException {
        ConsoleBuffer consoleBuffer = new AeshConsoleBufferBuilder()
                .shell(invocation.getShell())
                .prompt(new Prompt(prompt, mask))
                .create();
        InputProcessor inputProcessor = new AeshInputProcessorBuilder()
                .consoleBuffer(consoleBuffer)
                .create();
        consoleBuffer.displayPrompt();
        String result;
        do {
            result = inputProcessor.parseOperation(invocation.getInput());
        }
        while(result == null );
        return result;
    }

    protected String argOrPromptForInput(List<String> arguments, int argumentIndex, String prompt, Character mask, CommandInvocation invocation)
            throws IOException, InterruptedException {
        if (arguments.size() >= argumentIndex + 1) {
            if (StringUtils.isNotBlank(arguments.get(argumentIndex))) {
                return arguments.get(argumentIndex);
            }
        }
        return promptForInput(prompt, mask, invocation);
    }

    protected abstract CommandResult doExecute(CommandInvocation commandInvocation) throws Exception;

    protected abstract String getName();
}
