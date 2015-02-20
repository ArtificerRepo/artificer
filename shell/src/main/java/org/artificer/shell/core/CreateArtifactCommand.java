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
package org.artificer.shell.core;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.ArtifactType;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.ArtifactTypeCompleter;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * This CLI command is used to create a new artifact in the repository. This
 * command creates non-Document style artifacts (artifacts with no content). It
 * complements the {@link UploadArtifactCommand}, which creates a new artifact
 * *with* content.
 * 
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "createArtifact",
        description = "The \"create\" command creates a new artifact in the Artificer repository, without file content.  The artifact type and name must be provided, along with an optional description.\n")
public class CreateArtifactCommand extends AbstractCommand {

    @Option(name = "type", hasValue = true, required = true, completer = ArtifactTypeCompleter.class,
            description = "Artifact type")
    private String type;

    @Option(name = "name", hasValue = true, required = true,
            description = "Artifact name")
    private String name;

    @Option(name = "description", hasValue = true, required = false,
            description = "Artifact description")
    private String description;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }

        ArtificerAtomApiClient client = client(commandInvocation);

        ArtifactType artifactType = ArtifactType.valueOf(type);
        if (artifactType.isExtendedType()) {
            artifactType = ArtifactType.ExtendedArtifactType(artifactType.getExtendedType(), false);
        }

        if (artifactType.isDerived()) {
            commandInvocation.getShell().out().println(Messages.i18n.format("ArtifactModel.isDerived"));
            return CommandResult.FAILURE;
        } else if (artifactType.isDocument()) {
            commandInvocation.getShell().out().println(Messages.i18n.format("ArtifactModel.isDocument"));
            return CommandResult.FAILURE;
        } else {
            BaseArtifactType artifact = artifactType.newArtifactInstance();
            artifact.setName(name);
            artifact.setDescription(description);
            artifact = client.createArtifact(artifact);
            // Put the artifact in the session as the active artifact
            context(commandInvocation).setCurrentArtifact(artifact);
        }

        commandInvocation.getShell().out().println(Messages.i18n.format("CreateArtifactCommand.Successful", name));
        return CommandResult.SUCCESS;
    }

    @Override
    protected String getName() {
        return "createArtifact";
    }

}
