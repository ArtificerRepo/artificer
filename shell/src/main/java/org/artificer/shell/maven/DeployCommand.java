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

package org.artificer.shell.maven;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ArtificerClientException;
import org.artificer.client.ArtificerClientQuery;
import org.artificer.client.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.common.maven.MavenGavInfo;
import org.artificer.common.maven.MavenUtil;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.integration.java.model.JavaModel;
import org.artificer.shell.AbstractCommand;
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

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Uploads a file to the S-RAMP repository as a new artifact.  Additionally
 * adds Maven meta-data to the resulting artifact, including:
 *
 * <ul>
 *   <li>Group ID</li>
 *   <li>Artifact ID</li>
 *   <li>Version</li>
 *   <li>Classifier (optional)</li>
 *   <li>Type (optional)</li>
 *   <li>MD5 Hash</li>
 *   <li>SHA1 Hash</li>
 * </ul>
 *
 * Usage:
 * <pre>
 *   maven:deploy &lt;pathToFile&gt; &lt;groupId&gt;:&lt;artifactId&gt;:&lt;version&gt;:[&lt;type&gt;]:[&lt;classifier&gt;] [&lt;artifactType&gt;]
 * </pre>
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "deploy",
        description = "The \"deploy\" command uploads the content of a local file to the Artificer repository, creating a new artifact with appropriate maven meta-data (e.g. groupId, artifactId, version). The artifact type can also optionally be provided.  If the artifact type is excluded, it will be automatically detected using contextual clues.\nExample usages\ndeploy /home/uname/files/myarchive.jar org.example:my-archive:1.0.1.Final\ndeploy /home/uname/files/other-archive.war org.example:other-archive:2.0.7.Final:war JavaWebApplication\n")
public class DeployCommand extends AbstractCommand {

    private static final boolean ALLOW_SNAPSHOT = ArtificerConfig.isSnapshotAllowed();

    @Arguments(description = "<file path>", completer = Completer.class)
    private List<String> arguments;

    @Option(name = "gav", hasValue = true, required = true,
            description = "GAV")
    private String gav;

    @Option(name = "type", hasValue = true, required = false, completer = ArtifactTypeCompleter.class,
            description = "Artifact type")
    private String type;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "deploy";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        String filePathArg = requiredArgument(commandInvocation, arguments, 0);

        ArtificerAtomApiClient client = client(commandInvocation);

        // Validate the file
        File file = new File(filePathArg);
        if (!file.isFile()) {
            commandInvocation.getShell().out().println(Messages.i18n.format("DeployCommand.FileNotFound", filePathArg)); //$NON-NLS-1$
            return CommandResult.FAILURE;
        }

        InputStream content = null;
        try {
            ArtifactType artifactType = null;
            if (StringUtils.isNotBlank(type)) {
                artifactType = ArtifactType.valueOf(type);
                if (artifactType.isExtendedType()) {
                    artifactType = ArtifactType.ExtendedDocument(artifactType.getExtendedType());
                }
            }
            // Process GAV and other meta-data, then update the artifact
            MavenGavInfo mavenGavInfo = MavenGavInfo.fromCommandLine(gav, file);
            if (mavenGavInfo.getType() == null) {
                commandInvocation.getShell().out().println(Messages.i18n.format("DeployCommand.TypeNotSet", file.getName())); //$NON-NLS-1$
                IOUtils.closeQuietly(content);
                return CommandResult.FAILURE;
            }
            if (!ALLOW_SNAPSHOT && mavenGavInfo.isSnapshot()) {
                commandInvocation.getShell().out().println(Messages.i18n.format("DeployCommand.SnapshotNotAllowed", gav)); //$NON-NLS-1$
                IOUtils.closeQuietly(content);
                return CommandResult.FAILURE;
            }
            BaseArtifactType artifact = findExistingArtifactByGAV(client, mavenGavInfo);
            if (artifact != null) {
                commandInvocation.getShell().out().println(Messages.i18n.format("DeployCommand.Failure.ReleaseArtifact.Exist", gav)); //$NON-NLS-1$
                IOUtils.closeQuietly(content);
                return CommandResult.FAILURE;
            } else {
                content = FileUtils.openInputStream(file);
                artifact = client.uploadArtifact(artifactType, content, file.getName());
                IOUtils.closeQuietly(content);
            }

            // Process GAV and other meta-data, then update the artifact
            String artifactName = mavenGavInfo.getArtifactId() + '-' + mavenGavInfo.getVersion();
            String pomName = mavenGavInfo.getArtifactId() + '-' + mavenGavInfo.getVersion() + ".pom"; //$NON-NLS-1$
            ArtificerModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_GROUP_ID, mavenGavInfo.getGroupId());
            ArtificerModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_ARTIFACT_ID, mavenGavInfo.getArtifactId());
            ArtificerModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_VERSION, mavenGavInfo.getVersion());
            ArtificerModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_MD5, mavenGavInfo.getMd5());
            ArtificerModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_SHA1, mavenGavInfo.getSha1());
            if (StringUtils.isNotBlank(mavenGavInfo.getSnapshotId())) {
                ArtificerModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_SNAPSHOT_ID, mavenGavInfo.getSnapshotId());
            } else if (mavenGavInfo.isSnapshot()) {
                ArtificerModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_SNAPSHOT_ID, generateSnapshotTimestamp());
            }
            if (mavenGavInfo.getClassifier() != null) {
                ArtificerModelUtils.setCustomProperty(artifact, "maven.classifier", mavenGavInfo.getClassifier()); //$NON-NLS-1$
                artifactName += '-' + mavenGavInfo.getClassifier();
            }
            if (mavenGavInfo.getType() != null) {
                ArtificerModelUtils.setCustomProperty(artifact, "maven.type", mavenGavInfo.getType()); //$NON-NLS-1$
                artifactName += '.' + mavenGavInfo.getType();
            }
            artifact.setName(artifactName);
            client.updateArtifactMetaData(artifact);

            // Generate and add a POM for the artifact
            String pom = generatePom(mavenGavInfo);
            InputStream pomContent = new ByteArrayInputStream(pom.getBytes("UTF-8")); //$NON-NLS-1$
            BaseArtifactType pomArtifact = ArtifactType.ExtendedDocument(JavaModel.TYPE_MAVEN_POM_XML).newArtifactInstance();
            pomArtifact.setName(pomName);
            ArtificerModelUtils.setCustomProperty(pomArtifact, JavaModel.PROP_MAVEN_TYPE, "pom"); //$NON-NLS-1$
            ArtificerModelUtils.setCustomProperty(pomArtifact, JavaModel.PROP_MAVEN_HASH_MD5, DigestUtils.md5Hex(pom));
            ArtificerModelUtils.setCustomProperty(pomArtifact, JavaModel.PROP_MAVEN_HASH_SHA1, DigestUtils.shaHex(pom));

            client.uploadArtifact(pomArtifact, pomContent);

            // Put the artifact in the session as the active artifact
            context(commandInvocation).setCurrentArtifact(artifact);
            commandInvocation.getShell().out().println(Messages.i18n.format("DeployCommand.Success")); //$NON-NLS-1$
            PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor(commandInvocation);
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
        } catch (Exception e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("DeployCommand.Failure")); //$NON-NLS-1$
            commandInvocation.getShell().out().println("\t" + e.getMessage()); //$NON-NLS-1$
            IOUtils.closeQuietly(content);
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }

    /**
     * Generates a simple maven pom given the artifact information.
     *
     * @param mavenGavInfo
     * @return a generated Maven pom
     */
    private String generatePom(MavenGavInfo mavenGavInfo) {
        StringBuilder builder = new StringBuilder();
        builder.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"); //$NON-NLS-1$
        builder.append("  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"); //$NON-NLS-1$
        builder.append("  <modelVersion>4.0.0</modelVersion>\r\n"); //$NON-NLS-1$
        builder.append("  <groupId>" + mavenGavInfo.getGroupId() + "</groupId>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        builder.append("  <artifactId>" + mavenGavInfo.getArtifactId() + "</artifactId>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        builder.append("  <version>" + mavenGavInfo.getVersion() + "</version>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        if (mavenGavInfo.getType() != null) {
            builder.append("  <packaging>" + mavenGavInfo.getType() + "</packaging>\r\n");  //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (mavenGavInfo.getClassifier() != null) {
            builder.append("  <classifier>" + mavenGavInfo.getClassifier() + "</classifier>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        builder.append("</project>"); //$NON-NLS-1$
        return builder.toString();
    }

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            DeployCommand command = (DeployCommand) completerInvocation.getCommand();
            if (CollectionUtils.isEmpty(command.arguments)) {
                FileNameCompleterDelegate.complete(completerInvocation);
            }
        }
    }

    /**
     * Finds an existing artifact in the s-ramp repository that matches the GAV
     * information.
     *
     * @param client
     *            the client
     * @param mavenGavInfo
     * @return an s-ramp artifact (if found) or null (if not found)
     * @throws org.artificer.atom.err.ArtificerAtomException
     *             the sramp atom exception
     * @throws org.artificer.client.ArtificerClientException
     *             the sramp client exception
     * @throws javax.xml.bind.JAXBException
     *             the JAXB exception
     */
    private BaseArtifactType findExistingArtifactByGAV(ArtificerAtomApiClient client, MavenGavInfo mavenGavInfo) throws ArtificerAtomException,
            ArtificerClientException, JAXBException {
        String query = MavenUtil.gavQuery(mavenGavInfo);
        ArtificerClientQuery clientQuery = client.buildQuery(query);

        QueryResultSet rset = clientQuery.count(100).query();
        if (rset.size() > 0) {
            for (ArtifactSummary summary : rset) {
                String uuid = summary.getUuid();
                ArtifactType artifactType = summary.getType();
                BaseArtifactType arty = client.getArtifactMetaData(artifactType, uuid);
                return arty;

            }
        }
        return null;
    }


    /**
     * Generate snapshot timestamp.
     *
     * @return the string
     */
    private String generateSnapshotTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.hhmmss"); //$NON-NLS-1$
        String timestamp = sdf.format(new Date());
        StringBuilder builder = new StringBuilder();
        // It is added at the end the maven counter. By default it is set to
        // "1". The maven format for the timestamp is yyyyMMdd.hhmmss-counter
        builder.append(timestamp).append("-1"); //$NON-NLS-1$
        return builder.toString();
    }

}
