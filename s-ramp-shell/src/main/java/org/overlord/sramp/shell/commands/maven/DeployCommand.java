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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveRegistry;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.SrampClientQuery;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampConfig;
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

    public static final String SEPARATOR_FULL_NAME = ":"; //$NON-NLS-1$

    private final boolean allowSnapshot;

    /**
     * Constructor.
     */
    public DeployCommand() {
        allowSnapshot = SrampConfig.isSnapshotAllowed();
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
            // Process GAV and other meta-data, then update the artifact
            MavenMetaData mmd = new MavenMetaData(gavArg, file);
            if (mmd.type == null) {
                print(Messages.i18n.format("DeployCommand.TypeNotSet", file.getName())); //$NON-NLS-1$
                IOUtils.closeQuietly(content);
                return false;
            }
            if (!allowSnapshot && mmd.snapshot) {
                print(Messages.i18n.format("DeployCommand.SnapshotNotAllowed", mmd.getFullName())); //$NON-NLS-1$
                IOUtils.closeQuietly(content);
                return false;
            }
            BaseArtifactType artifact = findExistingArtifactByGAV(client, mmd);
            if (artifact != null) {
                    print(Messages.i18n.format("DeployCommand.Failure.ReleaseArtifact.Exist", mmd.getFullName())); //$NON-NLS-1$
                    IOUtils.closeQuietly(content);
                    return false;
            } else {
                content = FileUtils.openInputStream(file);
                artifact = client.uploadArtifact(artifactType, content, file.getName());
                IOUtils.closeQuietly(content);
            }



            // Process GAV and other meta-data, then update the artifact
            String artifactName = mmd.artifactId + '-' + mmd.version;
            String pomName = mmd.artifactId + '-' + mmd.version + ".pom"; //$NON-NLS-1$
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_GROUP_ID, mmd.groupId);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_ARTIFACT_ID, mmd.artifactId);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_VERSION, mmd.version);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_MD5, mmd.md5);
            SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_SHA1, mmd.sha1);
            if (StringUtils.isNotBlank(mmd.snapshotId)) {
                SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_SNAPSHOT_ID, mmd.snapshotId);
            } else if (mmd.snapshot) {
                SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_SNAPSHOT_ID, generateSnapshotTimestamp());
            }
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

            BaseArtifactType returned = client.uploadArtifact(pomArtifact, pomContent);

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
     * Tab completion.
     *
     * @param lastArgument
     *            the last argument
     * @param candidates
     *            the candidates
     * @return the int
     * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String,
     *      java.util.List)
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
        public boolean snapshot;
        public String snapshotId;

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
            String [] split = gavArg.split(":"); //$NON-NLS-1$
            if (split.length < 3) {
                throw new Exception(Messages.i18n.format("DeployCommand.InvalidArgMsg.GavFormat")); //$NON-NLS-1$
            }
            groupId = split[0];
            artifactId = split[1];
            version = split[2];
            String filename;
            if (file.getName().endsWith(".tmp")) {
                filename = file.getName().substring(0, file.getName().indexOf(".jar") + 4);
            } else {
                filename = file.getName();
            }
            if (split.length >= 4) {
                type = split[3];
            } else {
                type = getType(filename);
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
            snapshot = version != null && version.endsWith("-SNAPSHOT"); //$NON-NLS-1$
            snapshotId = null;
            if (snapshot && !filename.contains(version)) {
                snapshotId = extractSnapshotId(filename, version, type, classifier);
            }

        }

        /**
         * Extract snapshot id.
         *
         * @param filename
         *            the filename
         * @param version
         *            the version
         * @param type
         *            the type
         * @param classifier
         *            the classifier
         * @return the string
         */
        private String extractSnapshotId(String filename, String version, String type, String classifier) {
            if (version == null) {
                return null;
            }

            String front = version.substring(0, version.indexOf("-SNAPSHOT")); //$NON-NLS-1$
            String back = "." + type; //$NON-NLS-1$
            if (classifier != null) {
                back = "-" + classifier + back; //$NON-NLS-1$
            }
            int idx1 = filename.indexOf(front) + front.length() + 1;
            int idx2 = filename.indexOf(back);

            if (idx1 > 0 && idx1 < filename.length() && idx2 > 0 && idx2 < filename.length()) {
                return filename.substring(idx1, idx2);
            } else {
                return null;
            }
        }

        /**
         * Gets the full name.
         *
         * @return the full name
         */
        public String getFullName() {
            StringBuilder builder = new StringBuilder(""); //$NON-NLS-1$
            builder.append(groupId).append(SEPARATOR_FULL_NAME);
            if (StringUtils.isNotBlank(version)) {
                builder.append(version);
            }
            builder.append(SEPARATOR_FULL_NAME);
            builder.append(artifactId);
            builder.append(SEPARATOR_FULL_NAME);
            builder.append(type);
            return builder.toString();
        }

        /**
         * Obtain the type of file from its filename.
         *
         * @param filename
         *            the filename
         * @return the type
         */
        private String getType(String filename) {
            if (filename.contains(".")) {//$NON-NLS-1$
                String type = filename.substring(filename.lastIndexOf('.') + 1);
                if (filename.endsWith(".sha1")) { //$NON-NLS-1$
                    type = filename.substring(0, filename.length() - 5);
                    type = type.substring(type.lastIndexOf('.') + 1) + ".sha1"; //$NON-NLS-1$
                }
                if (filename.endsWith(".md5")) { //$NON-NLS-1$
                    type = filename.substring(0, filename.length() - 4);
                    type = type.substring(type.lastIndexOf('.') + 1) + ".md5"; //$NON-NLS-1$
                }
                return type;
            } else {
                return null;
            }

        }
    }

    /**
     * Finds an existing artifact in the s-ramp repository that matches the GAV
     * information.
     *
     * @param client
     *            the client
     * @param gavInfo
     *            the gav info
     * @return an s-ramp artifact (if found) or null (if not found)
     * @throws SrampAtomException
     *             the sramp atom exception
     * @throws SrampClientException
     *             the sramp client exception
     * @throws JAXBException
     *             the JAXB exception
     */
    private BaseArtifactType findExistingArtifactByGAV(SrampAtomApiClient client, MavenMetaData gavInfo) throws SrampAtomException,
            SrampClientException, JAXBException {
        SrampClientQuery clientQuery = null;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("/s-ramp"); //$NON-NLS-1$
        List<String> criteria = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();

        criteria.add("@maven.groupId = ?"); //$NON-NLS-1$
        params.add(gavInfo.groupId);
        criteria.add("@maven.artifactId = ?"); //$NON-NLS-1$
        params.add(gavInfo.artifactId);
        criteria.add("@maven.version = ?"); //$NON-NLS-1$
        params.add(gavInfo.version);

        if (StringUtils.isNotBlank(gavInfo.type)) {
            criteria.add("@maven.type = ?"); //$NON-NLS-1$
            params.add(gavInfo.type);
        }
        if (StringUtils.isNotBlank(gavInfo.classifier)) {
            criteria.add("@maven.classifier = ?"); //$NON-NLS-1$
            params.add(gavInfo.classifier);
        } else {
            criteria.add("xp2:not(@maven.classifier)"); //$NON-NLS-1$
        }
        if (StringUtils.isNotBlank(gavInfo.snapshotId)) {
            return null;
        } else {
            criteria.add("xp2:not(@maven.snapshot.id)"); //$NON-NLS-1$
        }

        if (criteria.size() > 0) {
            queryBuilder.append("["); //$NON-NLS-1$
            queryBuilder.append(StringUtils.join(criteria, " and ")); //$NON-NLS-1$
            queryBuilder.append("]"); //$NON-NLS-1$
        }
        clientQuery = client.buildQuery(queryBuilder.toString());
        for (Object param : params) {
            if (param instanceof String) {
                clientQuery.parameter((String) param);
            }
            if (param instanceof Calendar) {
                clientQuery.parameter((Calendar) param);
            }
        }

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
