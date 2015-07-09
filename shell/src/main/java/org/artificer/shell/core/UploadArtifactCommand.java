/*
 * Copyright 2012 JBoss Inc
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtifactTypeEnum;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.shell.common.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.ArtifactTypeCompleter;
import org.artificer.shell.util.FileNameCompleterDelegate;
import org.artificer.shell.util.PrintArtifactMetaDataVisitor;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Uploads an artifact to the s-ramp repository.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "uploadArtifact",
        description = "The \"upload\" command uploads the content of a local file to the Artificer repository, creating a new artifact.  The artifact type can optionally be provided.  If excluded, the artifact type will be automatically detected based on various contextual clues.\n")
public class UploadArtifactCommand extends AbstractCommand {

    @Arguments(description = "<file path>", completer = Completer.class)
    private List<String> arguments;

    @Option(name = "type", hasValue = true, required = false, completer = ArtifactTypeCompleter.class,
            description = "Artifact type")
    private String type;

    @Option(name = "local", hasValue = false, required = false,
            description = "Use if the file is reachable by the Artificer server, through its absolute path (local file, network storage, etc.).  Reduces HTTP overhead.")
    private boolean local;

    @Option(name = "name", hasValue = true, required = false,
            description = "Artifact name")
    private String name;

    @Option(name = "description", hasValue = true, required = false,
            description = "Artifact description")
    private String description;

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        String filePath = requiredArgument(commandInvocation, arguments, 0);

        ArtificerAtomApiClient client = client(commandInvocation);
        InputStream content = null;
        try {
            ArtifactType artifactType = null;
            if (StringUtils.isNotBlank(type)) {
                artifactType = ArtifactType.valueOf(type);
                if (artifactType.isExtendedType()) {
                    artifactType = ArtifactType.ExtendedDocument(artifactType.getExtendedType());
                }
            }

            File file = new File(filePath);
            BaseArtifactType artifact;
            if (local) {
                String path;
                if (file.exists()) {
                    path = file.getAbsolutePath();
                } else {
                    URL url = this.getClass().getClassLoader().getResource(filePath);
                    if (url != null) {
                        path = url.getPath();
                    } else {
                        commandInvocation.getShell().out().println(Messages.i18n.format("Upload.FileNotFound", filePath));
                        return CommandResult.FAILURE;
                    }
                }
                artifact = client.uploadArtifact(artifactType, path);
            } else {
                if (file.exists()) {
                    content = FileUtils.openInputStream(file);
                } else {
                    URL url = this.getClass().getClassLoader().getResource(filePath);
                    if (url != null) {
                        content = url.openStream();
                    } else {
                        commandInvocation.getShell().out().println(Messages.i18n.format("Upload.FileNotFound", filePath));
                        return CommandResult.FAILURE;
                    }
                }
                artifact = client.uploadArtifact(artifactType, content, file.getName());
            }

            if (StringUtils.isNotBlank(name) || StringUtils.isNotBlank(description)) {
                if (StringUtils.isNotBlank(name)) {
                    artifact.setName(name);
                }
                if (StringUtils.isNotBlank(description)) {
                    artifact.setDescription(description);
                }
                client.updateArtifactMetaData(artifact);
            }

            // Put the artifact in the session as the active artifact
            context(commandInvocation).setCurrentArtifact(artifact);
            commandInvocation.getShell().out().println(Messages.i18n.format("Upload.Success"));
            PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor(commandInvocation);
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);

            return CommandResult.SUCCESS;
        } catch (Exception e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("Upload.Failure"));
            commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
        } finally {
            IOUtils.closeQuietly(content);
        }
    }

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            UploadArtifactCommand command = (UploadArtifactCommand) completerInvocation.getCommand();
            if (CollectionUtils.isEmpty(command.arguments)) {
                FileNameCompleterDelegate.complete(completerInvocation);
            } else if (command.arguments.size() == 1) {
                String currentValue = completerInvocation.getGivenCompleteValue();
                for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                    String candidate = t.getType();
                    if (StringUtils.isBlank(currentValue) || candidate.startsWith(currentValue)) {
                        completerInvocation.addCompleterValue(candidate);
                    }
                }
            }
        }
    }

    @Override
    protected String getName() {
        return "uploadArtifact";
    }

}
