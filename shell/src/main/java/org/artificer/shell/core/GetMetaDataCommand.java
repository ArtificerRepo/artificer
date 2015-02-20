package org.artificer.shell.core;

import org.apache.commons.lang.StringUtils;
import org.artificer.atom.archive.ArtificerArchiveJaxbUtils;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.FileNameCompleterDelegate;
import org.artificer.shell.util.PrintArtifactMetaDataVisitor;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.File;

/**
 * Gets the full meta-data for a single artifact in the Artificer repo.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "getMetaData",
        description = "The \"getMetaData\" command downloads only the meta-data for a single artifact from the Artificer repository.  The artifact can be identified either by its unique Artificer uuid or else by an index into the most recent Feed.  The meta-data will either be displayed or saved to a local file, depending on whether a path to an output file (or directory) is provided.\n")
public class GetMetaDataCommand extends AbstractCommand {

    @Option(name = "uuid", hasValue = true, required = false,
            description = "Artifact UUID")
    private String artifactUuid;

    @Option(name = "feed", hasValue = true, required = false,
            description = "Feed index")
    private String feedIndex;

    @Option(name = "outputFile", hasValue = true, required = false, completer = Completer.class,
            description = "Output file path")
    private String outputFilePath;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }

        BaseArtifactType artifact = null;
        if (StringUtils.isNotBlank(artifactUuid)) {
            artifact = artifact(commandInvocation, artifactUuid);
        } else if (StringUtils.isNotBlank(feedIndex)) {
            artifact = artifactFromFeed(commandInvocation, feedIndex);
        } else {
            commandInvocation.getShell().out().println(Messages.i18n.format("Artifact.Arguments"));
            return CommandResult.FAILURE;
        }

        // Store the artifact in the context, making it the active artifact.
        context(commandInvocation).setCurrentArtifact(artifact);

        if (StringUtils.isBlank(outputFilePath)) {
            // Print out the meta-data information
            commandInvocation.getShell().out().println(Messages.i18n.format("GetMetaData.MetaDataLabel", artifact.getUuid()));
            commandInvocation.getShell().out().println("--------------");
            PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor(commandInvocation);
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
        } else {
            File outFile = new File(outputFilePath);
            if (outFile.isFile()) {
                commandInvocation.getShell().out().println(Messages.i18n.format("GetMetaData.OutputFileExists", outFile.getCanonicalPath()));
                return CommandResult.FAILURE;
            } else if (outFile.isDirectory()) {
                String fileName = artifact.getName() + "-metadata.xml";
                outFile = new File(outFile, fileName);
            }
            outFile.getParentFile().mkdirs();
            ArtificerArchiveJaxbUtils.writeMetaData(outFile, artifact, false);
            commandInvocation.getShell().out().println(Messages.i18n.format("GetMetaData.SavedTo", outFile.getCanonicalPath()));
        }

        return CommandResult.SUCCESS;
    }

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            GetMetaDataCommand command = (GetMetaDataCommand) completerInvocation.getCommand();
            if (StringUtils.isBlank(command.outputFilePath)) {
                FileNameCompleterDelegate.complete(completerInvocation);
            }
        }
    }

    @Override
    protected String getName() {
        return "getMetaData";
    }
}
