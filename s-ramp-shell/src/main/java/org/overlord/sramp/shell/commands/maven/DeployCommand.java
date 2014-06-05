/*
 * Copyright 2013 JBoss Inc
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
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveRegistry;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.integration.java.model.JavaModel;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.FileNameCompleter;
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
public class DeployCommand extends BuiltInShellCommand {

    /**
     * Constructor.
     */
    public DeployCommand() {
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        String filePathArg = this.requiredArgument(0, Messages.i18n.format("DeployCommand.InvalidArgMsg.LocalFile")); //$NON-NLS-1$
        String gavArg = this.requiredArgument(1, Messages.i18n.format("DeployCommand.InvalidArgMsg.GAVInfo")); //$NON-NLS-1$
        String artifactTypeArg = this.optionalArgument(2);

        QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
        SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        }

        // Validate the file
        File file = new File(filePathArg);
        if (!file.isFile()) {
            print(Messages.i18n.format("DeployCommand.FileNotFound", filePathArg)); //$NON-NLS-1$
            return false;
        }

        InputStream content = null;
        ZipToSrampArchive expander = null;
        SrampArchive archive = null;
        try {
            ArtifactType artifactType = null;
            if (artifactTypeArg != null) {
                artifactType = ArtifactType.valueOf(artifactTypeArg);
                if (artifactType.isExtendedType()) {
                    artifactType = ArtifactType.ExtendedDocument(artifactType.getExtendedType());
                }
            } else {
                artifactType = determineArtifactType(file);
            }
            content = FileUtils.openInputStream(file);
            BaseArtifactType artifact = client.uploadArtifact(artifactType, content, file.getName());
            IOUtils.closeQuietly(content);
            
            // Process GAV and other meta-data, then update the artifact
            MavenMetaData mmd = new MavenMetaData(gavArg, file);
            String artifactName = mmd.artifactId + '-' + mmd.version;
            String pomName = mmd.artifactId + '-' + mmd.version + ".pom"; //$NON-NLS-1$
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_GROUP_ID, mmd.groupId);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_ARTIFACT_ID, mmd.artifactId);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_VERSION, mmd.version);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_MD5, mmd.md5);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_SHA1, mmd.sha1);
            if (mmd.classifier != null) {
                SrampModelUtils.setCustomProperty(artifact, "maven.classifier", mmd.classifier); //$NON-NLS-1$
                artifactName += '-' + mmd.classifier;
            }
            if (mmd.type != null) {
                SrampModelUtils.setCustomProperty(artifact, "maven.type", mmd.type); //$NON-NLS-1$
                artifactName += '.' + mmd.type;
            }
            artifact.setName(artifactName);
            client.updateArtifactMetaData(artifact);

            // Now also add "expanded" content to the s-ramp repository
            expander = ZipToSrampArchiveRegistry.createExpander(artifactType, file);
            if (expander != null) {
                expander.setContextParam(DefaultMetaDataFactory.PARENT_UUID, artifact.getUuid());
                archive = expander.createSrampArchive();
                client.uploadBatch(archive);
            }
            
            // Generate and add a POM for the artifact
            String pom = generatePom(mmd);
            InputStream pomContent = new ByteArrayInputStream(pom.getBytes("UTF-8")); //$NON-NLS-1$
            BaseArtifactType pomArtifact = ArtifactType.ExtendedDocument(JavaModel.TYPE_MAVEN_POM_XML).newArtifactInstance();
            pomArtifact.setName(pomName);
            SrampModelUtils.setCustomProperty(pomArtifact, JavaModel.PROP_MAVEN_TYPE, "pom"); //$NON-NLS-1$
            SrampModelUtils.setCustomProperty(pomArtifact, JavaModel.PROP_MAVEN_HASH_MD5, DigestUtils.md5Hex(pom));
            SrampModelUtils.setCustomProperty(pomArtifact, JavaModel.PROP_MAVEN_HASH_SHA1, DigestUtils.shaHex(pom));
            
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
     * @param mmd
     * @return a generated Maven pom
     */
    private String generatePom(MavenMetaData mmd) {
        StringBuilder builder = new StringBuilder();
        builder.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"); //$NON-NLS-1$
        builder.append("  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"); //$NON-NLS-1$
        builder.append("  <modelVersion>4.0.0</modelVersion>\r\n"); //$NON-NLS-1$
        builder.append("  <groupId>" + mmd.groupId + "</groupId>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        builder.append("  <artifactId>" + mmd.artifactId + "</artifactId>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        builder.append("  <version>" + mmd.version + "</version>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        if (mmd.type != null) {
            builder.append("  <packaging>" + mmd.type + "</packaging>\r\n");  //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (mmd.classifier != null) {
            builder.append("  <classifier>" + mmd.classifier + "</classifier>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        builder.append("</project>"); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * Try to figure out what kind of artifact we're dealing with.
     * @param file
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
     * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
     */
    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        if (getArguments().isEmpty()) {
            if (lastArgument == null)
                lastArgument = ""; //$NON-NLS-1$
            FileNameCompleter delegate = new FileNameCompleter();
            return delegate.complete(lastArgument, lastArgument.length(), candidates);
        } else if (getArguments().size() == 1) {
            return -1;
        } else if (getArguments().size() == 2) {
            for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                String candidate = t.getType();
                if (lastArgument == null || candidate.startsWith(lastArgument)) {
                    candidates.add(candidate);
                }
            }
            return 0;
        }
        return -1;
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
         * @param gavArg
         * @param file
         */
        public MavenMetaData(String gavArg, File file) throws Exception {
            String [] split = gavArg.split(":"); //$NON-NLS-1$
            if (split.length < 3) {
                throw new Exception(Messages.i18n.format("DeployCommand.InvalidArgMsg.GavFormat")); //$NON-NLS-1$
            }
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

}
