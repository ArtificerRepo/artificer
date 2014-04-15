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

package org.overlord.sramp.shell.commands.maven;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.cl.converter.Converter;
import org.jboss.aesh.cl.validator.OptionValidator;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.converter.ConverterInvocation;
import org.jboss.aesh.console.command.validator.ValidatorInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveRegistry;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.integration.java.model.JavaModel;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.validator.FileValidator;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;


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
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Maven.MAVEN_COMMAND_DEPLOY, description = "Uploads a file to the S-RAMP repository as a new artifact.")
public class DeployCommand extends BuiltInShellCommand {

    @Option(required = true, hasValue = true, name = "file", shortName = 'f', completer = FileOptionCompleter.class, validator = FileValidator.class)
    private File _file;

    @Option(required = true, name = "gav", hasValue = true, shortName = 'g', validator = GavValidator.class)
    private String _gav;

    @Option(hasValue = true, name = "artifactType", shortName = 't', completer = ArtifactTypeCompleter.class, converter = ArtifactTypeConverter.class)
    private ArtifactType _artifactType;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;
    /**
     * Constructor.
     */
    public DeployCommand() {
    }

    /**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        super.execute();

        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        }
        if (_artifactType == null) {
            _artifactType = determineArtifactType(_file);
        }

        InputStream content = null;
        ZipToSrampArchive expander = null;
        SrampArchive archive = null;
        try {

            content = FileUtils.openInputStream(_file);
            BaseArtifactType artifact = client.uploadArtifact(_artifactType, content, _file.getName());
            IOUtils.closeQuietly(content);

            // Process GAV and other meta-data, then update the artifact
            MavenMetaData mmd = new MavenMetaData(_gav, _file);
            String artifactName = mmd.artifactId + '-' + mmd.version;
            String pomName = mmd.artifactId + '-' + mmd.version + ".pom";
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_GROUP_ID, mmd.groupId);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_ARTIFACT_ID, mmd.artifactId);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_VERSION, mmd.version);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_MD5, mmd.md5);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_SHA1, mmd.sha1);
            if (mmd.classifier != null) {
                SrampModelUtils.setCustomProperty(artifact, "maven.classifier", mmd.classifier);
                artifactName += '-' + mmd.classifier;
            }
            if (mmd.type != null) {
                SrampModelUtils.setCustomProperty(artifact, "maven.type", mmd.type);
                artifactName += '.' + mmd.type;
            }
            artifact.setName(artifactName);
            client.updateArtifactMetaData(artifact);

            // Now also add "expanded" content to the s-ramp repository
            expander = ZipToSrampArchiveRegistry.createExpander(_artifactType, _file);
            if (expander != null) {
                expander.setContextParam(DefaultMetaDataFactory.PARENT_UUID, artifact.getUuid());
                archive = expander.createSrampArchive();
                client.uploadBatch(archive);
            }

            // Generate and add a POM for the artifact
            String pom = generatePom(mmd);
            InputStream pomContent = new ByteArrayInputStream(pom.getBytes("UTF-8"));
            BaseArtifactType pomArtifact = ArtifactType.ExtendedDocument(JavaModel.TYPE_MAVEN_POM_XML).newArtifactInstance();
            pomArtifact.setName(pomName);
            SrampModelUtils.setCustomProperty(pomArtifact, JavaModel.PROP_MAVEN_TYPE, "pom");
            SrampModelUtils.setCustomProperty(pomArtifact, JavaModel.PROP_MAVEN_HASH_MD5, DigestUtils.md5Hex(pom));
            SrampModelUtils.setCustomProperty(pomArtifact, JavaModel.PROP_MAVEN_HASH_SHA1, DigestUtils.shaHex(pom));
            print(Messages.i18n.format("DeployCommand.Uploading")); //$NON-NLS-1$
            client.uploadArtifact(pomArtifact, pomContent);

            // Put the artifact in the session as the active artifact
            QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
            getContext().setVariable(artifactVarName, artifact);
            print(Messages.i18n.format("DeployCommand.Success")); //$NON-NLS-1$
            PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
        } catch (Exception e) {
            print(Messages.i18n.format("DeployCommand.Failure")); //$NON-NLS-1$
            print("\t" + e.getMessage()); //$NON-NLS-1$
            IOUtils.closeQuietly(content);
            return false;
        }
        return true;
    }

    /**
     * Generates a simple maven pom given the artifact information.
     *
     * @param mmd
     *            the mmd
     * @return a generated Maven pom
     */
    private String generatePom(MavenMetaData mmd) {
        StringBuilder builder = new StringBuilder();
        builder.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n");
        builder.append("  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n");
        builder.append("  <modelVersion>4.0.0</modelVersion>\r\n");
        builder.append("  <groupId>" + mmd.groupId + "</groupId>\r\n");
        builder.append("  <artifactId>" + mmd.artifactId + "</artifactId>\r\n");
        builder.append("  <version>" + mmd.version + "</version>\r\n");
        if (mmd.type != null) {
            builder.append("  <packaging>" + mmd.type + "</packaging>\r\n");
        }
        if (mmd.classifier != null) {
            builder.append("  <classifier>" + mmd.classifier + "</classifier>\r\n");
        }
        builder.append("</project>");
        return builder.toString();
    }

    /**
     * Try to figure out what kind of artifact we're dealing with.
     *
     * @param file
     *            the file
     * @return the artifact type
     */
    private ArtifactType determineArtifactType(File file) {
        ArtifactType type = null;
        String extension = FilenameUtils.getExtension(file.getName());
        if ("jar".equals(extension)) { //$NON-NLS-1$
            type = ArtifactType.ExtendedDocument(JavaModel.TYPE_ARCHIVE);
        } else if ("war".equals(extension)) { //$NON-NLS-1$
            type = ArtifactType.ExtendedDocument(JavaModel.TYPE_WEB_APPLICATION);
        } else if ("ear".equals(extension)) { //$NON-NLS-1$
            type = ArtifactType.ExtendedDocument(JavaModel.TYPE_ENTERPRISE_APPLICATION);
        } else {
            type = ArtifactType.Document();
        }
        return type;
    }



    /**
     * Encapsulates maven meta-data information for a file.
     *
     * @author eric.wittmann@redhat.com
     */
    private static final class MavenMetaData {
        public String groupId;
        public String artifactId;
        public String version;
        public String type;
        public String classifier;
        public String md5;
        public String sha1;

        /**
         * Constructor.
         *
         * @param gavArg
         *            the gav arg
         * @param file
         *            the file
         * @throws Exception
         *             the exception
         */
        public MavenMetaData(String gavArg, File file) throws Exception {
            String [] split = gavArg.split(":");
            groupId = split[0];
            artifactId = split[1];
            version = split[2];
            if (split.length >= 4) {
                type = split[3];
            }
            if (split.length >= 5) {
                classifier = split[5];
            }
            // MD5 hash
            InputStream is = new FileInputStream(file);
            md5 = DigestUtils.md5Hex(is);
            IOUtils.closeQuietly(is);
            // SHA-1 hash
            is = new FileInputStream(file);
            sha1 = DigestUtils.shaHex(is);
            IOUtils.closeQuietly(is);
        }

    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Maven.MAVEN_COMMAND_DEPLOY;
    }

    /**
     * Convert the input string to an Artifact type object
     *
     * @author David Virgil Naranjo
     */
    private class ArtifactTypeConverter implements Converter<ArtifactType, ConverterInvocation> {

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.converter.Converter#convert(org.jboss.aesh.console.command.converter.ConverterInvocation)
         */
        @Override
        public ArtifactType convert(ConverterInvocation converterInvocation) throws OptionValidatorException {
            ArtifactType artifactType = null;
            if (converterInvocation.getInput() != null) {
                artifactType = ArtifactType.valueOf(converterInvocation.getInput());
                if (artifactType.isExtendedType()) {
                    artifactType = ArtifactType.ExtendedDocument(artifactType.getExtendedType());
                }

            }
            return artifactType;
        }
    }

    /**
     * Completes the string introduced by the user with the different artifact
     * types that match the input
     *
     * @author David Virgil Naranjo
     */
    private class ArtifactTypeCompleter implements OptionCompleter<CompleterInvocation> {

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.completer.OptionCompleter#complete(org.jboss.aesh.console.command.completer.CompleterInvocation)
         */
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            String artifact = completerInvocation.getGivenCompleteValue();
            for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                String candidate = t.getType();
                if (artifact == null || candidate.startsWith(artifact)) {
                    completerInvocation.addCompleterValue(candidate);
                }
            }
        }

    }

    /**
     * Check gav.
     *
     * @param gav
     *            the gav
     * @throws InvalidCommandArgumentException
     *             the invalid command argument exception
     */
    public void checkGav(String gav) throws InvalidCommandArgumentException {
        String[] split = gav.split(":");
        if (split.length < 3) {
            throw new InvalidCommandArgumentException(
                    Messages.i18n.format("DeployCommand.InvalidArgMsg.GavFormat"));
        }
    }

    /**
     * Validates that the input string has a gav format:
     * group:artifactId:version
     *
     * @author David Virgil Naranjo
     */
    public class GavValidator implements OptionValidator<ValidatorInvocation<String>> {

        /**
         * Instantiates a new gav validator.
         */
        public GavValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.OptionValidator#validate(org.jboss.aesh.console.command.validator.ValidatorInvocation)
         */
        @Override
        public void validate(ValidatorInvocation<String> validatorInvocation) throws OptionValidatorException {
            try {
                checkGav(validatorInvocation.getValue());
            } catch (InvalidCommandArgumentException e) {
                throw new OptionValidatorException(e.getMessage());
            }
        }

    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return _file;
    }

    /**
     * Sets the file.
     *
     * @param file
     *            the new file
     */
    public void setFile(File file) {
        this._file = file;
    }

    /**
     * Gets the gav.
     *
     * @return the gav
     */
    public String getGav() {
        return _gav;
    }

    /**
     * Sets the gav.
     *
     * @param gav
     *            the new gav
     */
    public void setGav(String gav) {
        this._gav = gav;
    }

    /**
     * Gets the artifact type.
     *
     * @return the artifact type
     */
    public ArtifactType getArtifactType() {
        return _artifactType;
    }

    /**
     * Sets the artifact type.
     *
     * @param artifactType
     *            the new artifact type
     */
    public void setArtifactType(ArtifactType artifactType) {
        this._artifactType = artifactType;
    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#isHelp()
     */
    @Override
    public boolean isHelp() {
        return _help;
    }

    /**
     * Sets the help.
     *
     * @param help
     *            the new help
     */
    public void setHelp(boolean help) {
        this._help = help;
    }

}
