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
package org.artificer.server.mvn.services;

import org.apache.commons.io.IOUtils;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.common.maven.MavenGavInfo;
import org.artificer.common.maven.MavenUtil;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.repository.query.PagedResult;
import org.artificer.server.ArtifactServiceImpl;
import org.artificer.server.QueryServiceImpl;
import org.artificer.server.i18n.Messages;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides a Maven repository "facade" to be used when deploying Maven-based artifacts.
 *
 * @author eric.wittmann@redhat.com
 * @author Brett Meyer
 * @author David Virgil Naranjo
 */
public class MavenFacadeServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenFacadeServlet.class);
    private static final boolean SNAPSHOT_ALLOWED = ArtificerConfig.isSnapshotAllowed();

    private final ArtifactServiceImpl artifactService = new ArtifactServiceImpl();
    private final QueryServiceImpl queryService = new QueryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        try {
            MavenGavInfo gavInfo = MavenGavInfo.fromUrl(req.getRequestURI());

            if (gavInfo.isMavenMetaData() && gavInfo.getVersion() == null) {
                writeResponse(doGenerateArtifactDirMavenMetaData(gavInfo), gavInfo, resp);
            } else if (gavInfo.isMavenMetaData() && gavInfo.getVersion() != null) {
                writeResponse(doGenerateSnapshotMavenMetaData(gavInfo), gavInfo, resp);
            } else if (gavInfo.isHash()) {
                writeResponse(doGetHash(gavInfo, req), gavInfo, resp);
            } else {
                writeResponse(findExistingArtifact(gavInfo), gavInfo, resp);
            }
        } catch (MavenRepositoryException e) {
            LOGGER.warn(Messages.i18n.format("MAVEN_GET_ERROR"), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.error(Messages.i18n.format("MAVEN_GET_ERROR"), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void writeResponse(String rval, MavenGavInfo gavInfo, HttpServletResponse resp) throws Exception {
        if (rval != null) {
            resp.addHeader("Content-Disposition",
                    "attachment; filename=" + gavInfo.getName());
            resp.getWriter().write(rval);
            resp.getWriter().flush();
            resp.getWriter().close();
        }
    }

    private void writeResponse(BaseArtifactType artifact, MavenGavInfo gavInfo, HttpServletResponse resp) throws Exception {
        if (artifact != null) {
            ArtifactType artifactType = ArtifactType.valueOf(artifact);
            resp.setContentType(artifactType.getMimeType());
            resp.addHeader("Content-Disposition",
                    "attachment; filename=" + gavInfo.getName());
            IOUtils.copy(artifactService.getContent(artifactType, artifact), resp.getOutputStream());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        try {
            MavenGavInfo gavInfo = MavenGavInfo.fromUrl(req.getRequestURI());
            upload(gavInfo, req);
        } catch (MavenRepositoryException e) {
            LOGGER.warn(Messages.i18n.format("MAVEN_UPLOAD_ERROR"), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.error(Messages.i18n.format("MAVEN_UPLOAD_ERROR"), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        try {
            MavenGavInfo gavInfo = MavenGavInfo.fromUrl(req.getRequestURI());
            upload(gavInfo, req);
        } catch (MavenRepositoryException e) {
            LOGGER.warn(Messages.i18n.format("MAVEN_UPLOAD_ERROR"), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.error(Messages.i18n.format("MAVEN_UPLOAD_ERROR"), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Generates the maven-metadata.xml file dynamically for a given groupId/artifactId pair.  This will
     * list all of the versions available for that groupId+artifactId, along with the latest release and
     * snapshot versions.
     * @param gavInfo
     */
    private String doGenerateArtifactDirMavenMetaData(MavenGavInfo gavInfo) throws Exception {
        PagedResult<ArtifactSummary> artifacts = queryService.query(
                "/s-ramp[@maven.groupId = '" + gavInfo.getGroupId() + "' and @maven.artifactId = '" + gavInfo.getArtifactId() + "']",
                "createdTimestamp", true);
        if (artifacts.getTotalSize() == 0) {
            return null;
        }

        String groupId = gavInfo.getGroupId();
        String artifactId = gavInfo.getArtifactId();
        String latest = null;
        String release = null;
        String lastUpdated = null;

        LinkedHashSet<String> versions = new LinkedHashSet<String>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        for (ArtifactSummary artifactSummary : artifacts.getResults()) {
            BaseArtifactType artifact = artifactService.getMetaData(
                    artifactSummary.getModel(), artifactSummary.getType(), artifactSummary.getUuid());
            String version = ArtificerModelUtils.getCustomProperty(artifact, "maven.version");
            if (versions.add(version)) {
                latest = version;
                if (!version.endsWith("-SNAPSHOT")) {
                    release = version;
                }
            }
            lastUpdated = format.format(artifactSummary.getCreatedTimestamp().getTime());
        }

        StringBuilder mavenMetadata = new StringBuilder();
        mavenMetadata.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        mavenMetadata.append("<metadata>\n");
        mavenMetadata.append("  <groupId>").append(groupId).append("</groupId>\n");
        mavenMetadata.append("  <artifactId>").append(artifactId).append("</artifactId>\n");
        mavenMetadata.append("  <versioning>\n");
        mavenMetadata.append("    <latest>").append(latest).append("</latest>\n");
        mavenMetadata.append("    <release>").append(release).append("</release>\n");
        mavenMetadata.append("    <versions>\n");
        for (String version : versions) {
            mavenMetadata.append("      <version>").append(version).append("</version>\n");
        }
        mavenMetadata.append("    </versions>\n");
        mavenMetadata.append("    <lastUpdated>").append(lastUpdated).append("</lastUpdated>\n");
        mavenMetadata.append("  </versioning>\n");
        mavenMetadata.append("</metadata>\n");

        if (!gavInfo.isHash()) {
            return mavenMetadata.toString();
        } else {
            return generateHash(mavenMetadata.toString(), gavInfo.getHashAlgorithm());
        }
    }

    /**
     * Generates the maven-metadata.xml file dynamically for a given groupId/artifactId/snapshot-version.
     * This will list all of the snapshot versions available.
     * @param gavInfo
     * @throws Exception
     */
    private String doGenerateSnapshotMavenMetaData(MavenGavInfo gavInfo) throws Exception {
        PagedResult<ArtifactSummary> artifacts = queryService.query(
                "/s-ramp[@maven.groupId = '" + gavInfo.getGroupId() + "'" +
                        " and @maven.artifactId = '" + gavInfo.getArtifactId() + "'" +
                        " and @maven.version = '" + gavInfo.getVersion() + "']",
                "createdTimestamp", true);
        if (artifacts.getTotalSize() == 0) {
            return null;
        }

        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
        SimpleDateFormat updatedFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        StringBuilder snapshotVersions = new StringBuilder();
        snapshotVersions.append("    <snapshotVersions>\n");
        Set<String> processed = new HashSet<String>();
        Calendar latestDate = null;
        for (ArtifactSummary artifactSummary : artifacts.getResults()) {
            BaseArtifactType artifact = artifactService.getMetaData(
                    artifactSummary.getModel(), artifactSummary.getType(), artifactSummary.getUuid());
            String extension = ArtificerModelUtils.getCustomProperty(artifact, "maven.type");
            String classifier = ArtificerModelUtils.getCustomProperty(artifact, "maven.classifier");
            String value = gavInfo.getVersion();
            Calendar updatedDate = artifact.getLastModifiedTimestamp().toGregorianCalendar();
            String updated = updatedFormat.format(updatedDate.getTime());
            String pkey = classifier+"::"+extension;
            if (processed.add(pkey)) {
                snapshotVersions.append("      <snapshotVersion>\n");
                if (classifier != null)
                    snapshotVersions.append("        <classifier>").append(classifier).append("</classifier>\n");
                snapshotVersions.append("        <extension>").append(extension).append("</extension>\n");
                snapshotVersions.append("        <value>").append(value).append("</value>\n");
                snapshotVersions.append("        <updated>").append(updated).append("</updated>\n");
                snapshotVersions.append("      </snapshotVersion>\n");
                if (latestDate == null || latestDate.before(updatedDate)) {
                    latestDate = updatedDate;
                }
            }
        }
        snapshotVersions.append("    </snapshotVersions>\n");

        String groupId = gavInfo.getGroupId();
        String artifactId = gavInfo.getArtifactId();
        String version = gavInfo.getVersion();
        String lastUpdated = updatedFormat.format(latestDate.getTime());

        StringBuilder mavenMetadata = new StringBuilder();
        mavenMetadata.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        mavenMetadata.append("<metadata>\n");
        mavenMetadata.append("  <groupId>").append(groupId).append("</groupId>\n");
        mavenMetadata.append("  <artifactId>").append(artifactId).append("</artifactId>\n");
        mavenMetadata.append("  <version>").append(version).append("</version>\n");
        mavenMetadata.append("  <versioning>\n");
        mavenMetadata.append("    <snapshot>\n");
        mavenMetadata.append("      <timestamp>").append(timestampFormat.format(latestDate.getTime())).append("</timestamp>\n");
        mavenMetadata.append("      <buildNumber>1</buildNumber>\n");
        mavenMetadata.append("    </snapshot>\n");
        mavenMetadata.append("    <lastUpdated>").append(lastUpdated).append("</lastUpdated>\n");
        mavenMetadata.append(snapshotVersions.toString());
        mavenMetadata.append("  </versioning>\n");
        mavenMetadata.append("</metadata>\n");

        if (!gavInfo.isHash()) {
            return mavenMetadata.toString();
        } else {
            return generateHash(mavenMetadata.toString(), gavInfo.getHashAlgorithm());
        }
    }

    /**
     * Gets the hash data from the s-ramp repository for use by Maven.
     * @param gavInfo
     * @param req
     * @throws Exception
     */
    private String doGetHash(MavenGavInfo gavInfo, HttpServletRequest req) throws Exception {
        int hashExtensionLength;
        String hashPropName;
        if (gavInfo.getType().endsWith(".md5")) {
            hashExtensionLength = 4;
            hashPropName = "maven.hash.md5";
        } else {
            hashExtensionLength = 5;
            hashPropName = "maven.hash.sha1";
        }

        MavenGavInfo primaryGavInfo = gavWithoutHash(req, hashExtensionLength);
        BaseArtifactType artifact = findExistingArtifact(primaryGavInfo);
        if (artifact == null) {
            return null;
        }

        return ArtificerModelUtils.getCustomProperty(artifact, hashPropName);
    }

    /**
     * Finds an existing artifact in the s-ramp repository that matches the type and GAV information.
     * @param gavInfo
     * @return an s-ramp artifact (if found) or null (if not found)
     * @throws Exception
     */
    private BaseArtifactType findExistingArtifact(MavenGavInfo gavInfo) throws Exception {
        BaseArtifactType artifact = findExistingArtifactByGAV(gavInfo);
        if (artifact == null)
            artifact = findExistingArtifactByUniversal(gavInfo);
        return artifact;
    }

    /**
     * Finds an existing artifact in the s-ramp repository that matches the GAV information.
     * @param gavInfo
     * @return an s-ramp artifact (if found) or null (if not found)
     * @throws Exception
     */
    private BaseArtifactType findExistingArtifactByGAV(MavenGavInfo gavInfo) throws Exception {
        String query = MavenUtil.gavQuery(gavInfo);

        PagedResult<ArtifactSummary> artifacts = queryService.query(query, "createdTimestamp", false);
        if (artifacts.getTotalSize() > 0) {
            for (ArtifactSummary artifactSummary : artifacts.getResults()) {
                BaseArtifactType artifact = artifactService.getMetaData(
                        artifactSummary.getModel(), artifactSummary.getType(), artifactSummary.getUuid());
                // If no classifier in the GAV info, only return the artifact that also has no classifier
                // TODO replace this with "not(@maven.classifer)" in the query, then force the result set to return 2 items (expecting only 1)
                if (gavInfo.getClassifier() == null) {
                    String artyClassifier = ArtificerModelUtils.getCustomProperty(artifact, "maven.classifier");
                    if (artyClassifier == null) {
                        return artifact;
                    }
                } else {
                    // If classifier was supplied in the GAV info, we'll get the first artifact <shrug>
                    return artifact;
                }
            }
        }
        return null;
    }

    /**
     * Finds an existing artifact in the s-ramp repository using 'universal' form.  This allows
     * any artifact in the s-ramp repository to be referenced as a Maven dependency using the
     * model.type and UUID of the artifact.
     * @param gavInfo
     * @return an existing s-ramp artifact (if found) or null (if not found)
     * @throws Exception
     */
    private BaseArtifactType findExistingArtifactByUniversal(MavenGavInfo gavInfo) throws Exception {
        String artifactType = gavInfo.getGroupId().substring(gavInfo.getGroupId().indexOf('.') + 1);
        String uuid = gavInfo.getArtifactId();
        try {
            return artifactService.getMetaData(ArtifactType.valueOf(artifactType, true), uuid);
        } catch (Exception e) {
            // Eat it.  If this wasn't model.type, it'll be a real groupId, which is *not* a valid extended type.
            // The server will through an exception if it's not completely alphanumeric.
            return null;
        }
    }

    /**
     * Common put implementation.  Handles firing events and ultimately sending the data via the
     * s-ramp client.
     * @param gavInfo
     * @param req
     * @throws Exception
     */
    private void upload(MavenGavInfo gavInfo, HttpServletRequest req) throws Exception {
        if (SNAPSHOT_ALLOWED || !gavInfo.isSnapshot()) {
            if (!gavInfo.getName().contains("maven-metadata.xml")) {
                if (gavInfo.isHash()) {
                    uploadHash(gavInfo, req);
                } else {
                    uploadArtifact(gavInfo, req);
                }
            }
        } else {
            throw new MavenRepositoryException(Messages.i18n.format("MAVEN_UPLOAD_SNAPSHOT"));
        }
    }

    /**
     * Updates an artifact by storing its hash value as an S-RAMP property.
     * @param gavInfo
     * @param req
     * @throws Exception
     */
    private void uploadHash(MavenGavInfo gavInfo, HttpServletRequest req) throws Exception {
        int hashExtensionLength;
        String hashPropName;
        if (gavInfo.getType().endsWith(".md5")) {
            hashExtensionLength = 4;
            hashPropName = "maven.hash.md5";
        } else {
            hashExtensionLength = 5;
            hashPropName = "maven.hash.sha1";
        }
        String hashValue = IOUtils.toString(req.getInputStream());

        // Re-fetch the artifact meta-data in case it changed on the server since we uploaded it.
        MavenGavInfo primaryGavInfo = gavWithoutHash(req, hashExtensionLength);
        BaseArtifactType artifact = findExistingArtifactByGAV(primaryGavInfo);
        ArtificerModelUtils.setCustomProperty(artifact, hashPropName, hashValue);

        // The meta-data has been updated in the local/temp archive - now send it to the remote repo
        artifactService.updateMetaData(artifact);
    }

    /**
     * Puts the artifact into the s-ramp repository.
     * @param gavInfo
     * @param req
     * @throws Exception
     */
    private void uploadArtifact(final MavenGavInfo gavInfo, HttpServletRequest req) throws Exception {
        BaseArtifactType artifact = findExistingArtifactByGAV(gavInfo);
        ArtifactType artifactType = getArtifactType(gavInfo, req);
        // If we found an artifact, we should update its content.  If not, we should upload
        // the artifact to the repository.
        if (artifact != null) {
            if (gavInfo.isSnapshot()) {
                artifactService.delete(artifactType, artifact.getUuid());
                artifact = artifactService.upload(artifactType, gavInfo.getName(), req.getInputStream());
                updateGavProperties(gavInfo, artifact);
            } else {
                throw new MavenRepositoryException(Messages.i18n.format("MAVEN_UPLOAD_ARTIFACT_EXISTS"));
            }
        } else {
            artifact = artifactService.upload(artifactType, gavInfo.getName(), req.getInputStream());
            updateGavProperties(gavInfo, artifact);
        }
    }

    private void updateGavProperties(final MavenGavInfo gavInfo, BaseArtifactType artifact) throws Exception {
        ArtificerModelUtils.setCustomProperty(artifact, "maven.groupId", gavInfo.getGroupId());
        ArtificerModelUtils.setCustomProperty(artifact, "maven.artifactId", gavInfo.getArtifactId());
        ArtificerModelUtils.setCustomProperty(artifact, "maven.version", gavInfo.getVersion());
        artifact.setVersion(gavInfo.getVersion());
        if (gavInfo.getClassifier() != null) {
            ArtificerModelUtils.setCustomProperty(artifact, "maven.classifier", gavInfo.getClassifier());
        }
        if (gavInfo.getSnapshotId() != null && !gavInfo.getSnapshotId().equals("")) {
            ArtificerModelUtils.setCustomProperty(artifact, "maven.snapshot.id", gavInfo.getSnapshotId());
        }
        ArtificerModelUtils.setCustomProperty(artifact, "maven.type", gavInfo.getType());

        artifactService.updateMetaData(artifact);
    }

    /**
     * When looking up a hash, we have to lookup the primary artifact it's attached to.  Strip the hash extension
     * from the type, etc.
     *
     * @param req
     * @param hashExtensionLength
     * @return MavenGavInfo
     */
    private MavenGavInfo gavWithoutHash(HttpServletRequest req, int hashExtensionLength) {
        MavenGavInfo primaryGavInfo = MavenGavInfo.fromUrl(req.getRequestURI());
        primaryGavInfo.setType(primaryGavInfo.getType().substring(0, primaryGavInfo.getType().length() - hashExtensionLength));
        return primaryGavInfo;
    }

    /**
     * Generates a hash for the given content using the given hash algorithm.
     * @param string
     * @param hashAlgorithm
     */
    private String generateHash(String string, String hashAlgorithm) throws Exception {
        InputStream inputStream = IOUtils.toInputStream(string);
        MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
        byte[] dataBytes = new byte[1024];

        int nread = 0;

        while ((nread = inputStream.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }

        byte[] mdbytes = md.digest();

        // convert the byte to hex format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    /**
     * Gets the artifact type from the GAV or HttpServletRequest.
     * @param gavInfo
     */
    private ArtifactType getArtifactType(MavenGavInfo gavInfo, HttpServletRequest req) {
        String customAT = req.getParameter("artifactType");
        if (gavInfo.getType().equals("pom")) {
            return ArtifactType.valueOf("MavenPom", true);
        } else if (isPrimaryArtifact(gavInfo) && customAT != null) {
            return ArtifactType.valueOf(customAT, true);
        } else {
            return null;
        }
    }

    /**
     * Returns true if this represents the primary artifact in the Maven module.
     * @param gavInfo
     */
    private boolean isPrimaryArtifact(MavenGavInfo gavInfo) {
        return gavInfo.getClassifier() == null;
    }
}
