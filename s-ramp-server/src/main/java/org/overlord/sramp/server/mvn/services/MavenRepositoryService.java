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
package org.overlord.sramp.server.mvn.services;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConfig;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.integration.java.model.JavaModel;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.QueryManagerFactory;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;
import org.overlord.sramp.server.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the maven repository service. It connects to s-ramp and
 * return the data, for being displayed as maven requires.
 *
 * @author David Virgil Naranjo
 */
public class MavenRepositoryService extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String URL_CONTEXT_STR = "maven/repository"; //$NON-NLS-1$

    private static boolean SNAPSHOT_ALLOWED;

    private static Logger logger = LoggerFactory.getLogger(MavenRepositoryService.class);

    static {
        SNAPSHOT_ALLOWED = SrampConfig.isSnapshotAllowed();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException,
            IOException {
        uploadArtifact(req, response);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse response) throws ServletException,
            IOException {
        uploadArtifact(req, response);
    }

    private ArtifactSet query(List<String> criteria, List<Object> parameters) throws SrampAtomException {
        ArtifactSet artifactSet = null;
        /* Query */

        StringBuilder queryBuilder = new StringBuilder();
        // Initial query

        queryBuilder.append("/s-ramp"); //$NON-NLS-1$

        // Now create the query predicate from the generated criteria
        if (criteria.size() > 0) {
            queryBuilder.append("["); //$NON-NLS-1$
            queryBuilder.append(StringUtils.join(criteria, " and ")); //$NON-NLS-1$
            queryBuilder.append("]"); //$NON-NLS-1$
        }

        try {
            QueryManager queryManager = QueryManagerFactory.newInstance();
            SrampQuery srampQuery = queryManager.createQuery(queryBuilder.toString(), "createdTimestamp", false); //$NON-NLS-1$

            for (Object parameter : parameters) {
                if (parameter instanceof String) {
                    srampQuery.setString((String) parameter);
                } else if (parameter instanceof Date) {
                    srampQuery.setDate((Date) parameter);
                }
            }
            artifactSet = srampQuery.executeQuery();
        } catch (Throwable e) {
            logger.error(Messages.i18n.format("maven.resource.query.error", queryBuilder.toString()), e); //$NON-NLS-1$
            throw new SrampAtomException(e);
        }
        return artifactSet;
    }

    private void uploadArtifact(HttpServletRequest req, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the URL request and prepare it to obtain the maven metadata
        // information
        String url = req.getRequestURI();
        String maven_url = ""; //$NON-NLS-1$
        if (url.contains(URL_CONTEXT_STR)) {
            maven_url = url.substring(url.indexOf(URL_CONTEXT_STR) + URL_CONTEXT_STR.length());
        } else {
            maven_url = url;
        }

        if (maven_url.startsWith("/")) { //$NON-NLS-1$
            maven_url = maven_url.substring(1);
        }

        // Extract the relevant content from the POST'd form
        Map<String, String> responseMap = new HashMap<String, String>();

        InputStream content = null;
        // Parse the request
        content = req.getInputStream();

        // Builder class that converts the url into a Maven MetaData Object
        MavenMetaData metadata = MavenMetaDataBuilder.build(maven_url);
        try {
            if (metadata.isArtifact()) {
                if (SNAPSHOT_ALLOWED || !metadata.isSnapshotVersion()) {
                    String uuid = uploadArtifact(metadata, content);
                    responseMap.put("uuid", uuid); //$NON-NLS-1$
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Messages.i18n.format("maven.servlet.put.snapshot.not.allowed")); //$NON-NLS-1$
                }

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, Messages.i18n.format("maven.servlet.put.url.without.artifact")); //$NON-NLS-1$
            }

        } catch (Throwable e) {
            logger.error(Messages.i18n.format("maven.servlet.artifact.content.put.exception"), e); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Messages.i18n.format("maven.servlet.put.exception")); //$NON-NLS-1$
        } finally {
            if (content != null) {
                IOUtils.closeQuietly(content);
            }

        }
    }

    private String uploadArtifact(MavenMetaData metadata, InputStream content) throws MavenRepositoryException {
        String uuid = null;
        if (content == null) {
            throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.no.content")); //$NON-NLS-1$
        }
        String fileName = metadata.getFileName();
        PersistenceManager persistenceManager = PersistenceFactory.newInstance();

        // We need to query firstly to check if there is an existing item. If
        // there is an existing item, then it would be updated
        // Adding the criterias and parameters
        List<String> criteria = new ArrayList<String>();
        List<Object> parameters = new ArrayList<Object>();
        criteria.add("@maven.artifactId = ?"); //$NON-NLS-1$
        criteria.add("@maven.groupId = ?"); //$NON-NLS-1$
        criteria.add("@maven.type = ?"); //$NON-NLS-1$

        parameters.add(metadata.getArtifactId());
        parameters.add(metadata.getGroupId());
        if (StringUtils.isNotBlank(metadata.getParentType())) {
            parameters.add(metadata.getParentType());
        } else {
            parameters.add(metadata.getType());
        }

        if (StringUtils.isNotBlank(metadata.getVersion())) {
            criteria.add("@maven.version = ?"); //$NON-NLS-1$
            parameters.add(metadata.getVersion());
        } else {
            criteria.add("xp2:not(@maven.version)"); //$NON-NLS-1$
        }
        if (StringUtils.isNotBlank(metadata.getParentFileName())) {
            criteria.add("@name = ?"); //$NON-NLS-1$
            parameters.add(metadata.getParentFileName());

        }

        if (StringUtils.isNotBlank(metadata.getSnapshotId())) {
            criteria.add("@maven.snapshot.id = ?"); //$NON-NLS-1$
            parameters.add(metadata.getSnapshotId());
        } else {
            criteria.add("xp2:not(@maven.snapshot.id)"); //$NON-NLS-1$
        }

        ArtifactSet artifactSet = null;
        BaseArtifactType baseArtifact = null;
        try {
            // Query the previous criterias
            artifactSet = query(criteria, parameters);
            if (artifactSet.size() >= 1) {
                // Found some content!
                baseArtifact = artifactSet.iterator().next();
            }
        } catch (SrampAtomException e) {
            throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.query.error", metadata.toString()), e); //$NON-NLS-1$
        } finally {
            if (artifactSet != null) {
                artifactSet.close();
            }
        }
        if (MavenFileExtensionEnum.value(metadata.getType()) != null) {
            if (baseArtifact != null) {
                boolean update = false;
                ArtifactType artifactType = ArtifactType.valueOf(baseArtifact.getArtifactType());
                if (metadata.getType().equals(MavenFileExtensionEnum.HASH_MD5.getExtension())
                        || metadata.getType().equals(MavenFileExtensionEnum.HASH_SHA1.getExtension())) {
                    String content_value = ""; //$NON-NLS-1$
                    try {
                        content_value = IOUtils.toString(content);
                    } catch (IOException e1) {
                        throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.error", metadata.toString()), e1); //$NON-NLS-1$
                    }

                    if (StringUtils.isNotBlank(content_value)) {
                        if (metadata.getType().equals(MavenFileExtensionEnum.HASH_MD5.getExtension())) {
                            SrampModelUtils.setCustomProperty(baseArtifact, JavaModel.PROP_MAVEN_HASH_MD5, content_value);
                            update = true;
                        } else if (metadata.getType().equals(MavenFileExtensionEnum.HASH_SHA1.getExtension())) {
                            SrampModelUtils.setCustomProperty(baseArtifact, JavaModel.PROP_MAVEN_HASH_SHA1, content_value);
                            update = true;
                        }
                        if (update) {

                            try {
                                persistenceManager.updateArtifact(baseArtifact, artifactType);
                            } catch (SrampException e) {
                                throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.error", metadata.toString()), e); //$NON-NLS-1$
                            }
                        }
                    }
                } else {
                    try {
                        persistenceManager.updateArtifactContent(baseArtifact.getUuid(), artifactType, new ArtifactContent(fileName, content));
                    } catch (Exception e) {
                        throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.error", metadata.toString()), e); //$NON-NLS-1$
                    }
                }

            }
        } else {
            BaseArtifactType persisted = null;
            // If there is an existing artifact in s-ramp it would be updaded
            // with the new content
            if (baseArtifact != null) {
                if (metadata.isSnapshotVersion() || metadata.getFileName().equals("maven-metadata.xml")) { //$NON-NLS-1$
                    ArtifactType artifactType = ArtifactType.valueOf(baseArtifact);
                    try {
                        persistenceManager.updateArtifactContent(baseArtifact.getUuid(), artifactType, new ArtifactContent(fileName, content));
                    } catch (Exception e) {
                        throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.update.content.error", //$NON-NLS-1$
                                baseArtifact.getUuid()), e);
                    }
                    persisted = baseArtifact;
                } else {
                    throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.release.artifact.exist", //$NON-NLS-1$
                            metadata.getFullName()));
                }

            } else {
                // we need to create a new artifact in s-ramp and persist the
                // content
                ArtifactType artifactType = determineArtifactType(fileName);
                BaseArtifactType baseArtifactType = artifactType.newArtifactInstance();
                try {
                    persisted = persistenceManager.persistArtifact(baseArtifactType, new ArtifactContent(fileName, content));
                } catch (Exception e1) {
                    throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.new.content.error"), e1); //$NON-NLS-1$
                }
            }
            // Store the metadata to the persisted artifact
            SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_GROUP_ID, metadata.getGroupId());
            SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_ARTIFACT_ID, metadata.getArtifactId());
            SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_VERSION, metadata.getVersion());

            if (StringUtils.isNotBlank(metadata.getClassifier())) {
                SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_CLASSIFIER, metadata.getClassifier());
            }
            if (StringUtils.isNotBlank(metadata.getType())) {
                SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_TYPE, metadata.getType());
            }
            if (StringUtils.isNotBlank(metadata.getSnapshotId())) {
                SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_SNAPSHOT_ID, metadata.getSnapshotId());
            }
            try {
                // Persist the content size, because it will be required when
                // reading
                persisted.getOtherAttributes().put(SrampConstants.SRAMP_CONTENT_SIZE_QNAME, content.available() + ""); //$NON-NLS-1$
            } catch (IOException e) {
                logger.error(""); //$NON-NLS-1$
            }

            persisted.setName(metadata.getFileName());
            ArtifactType artifactType = ArtifactType.valueOf(persisted);
            try {
                persistenceManager.updateArtifact(persisted, artifactType);
            } catch (SrampException e) {
                throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.update.content.metadata.error", //$NON-NLS-1$
                        persisted.getUuid()), e);
            }
            uuid = persisted.getUuid();
        }

        return uuid;

    }

    /**
     * Try to figure out what kind of artifact we're dealing with.
     *
     * @param fileName
     *            the file name
     * @return the artifact type
     */
    private ArtifactType determineArtifactType(String fileName) {
        ArtifactType type = null;
        String extension = FilenameUtils.getExtension(fileName);
        if ("jar".equals(extension)) { //$NON-NLS-1$
            type = ArtifactType.ExtendedDocument(JavaModel.TYPE_ARCHIVE);
        } else if ("war".equals(extension)) { //$NON-NLS-1$
            type = ArtifactType.ExtendedDocument(JavaModel.TYPE_WEB_APPLICATION);
        } else if ("ear".equals(extension)) { //$NON-NLS-1$
            type = ArtifactType.ExtendedDocument(JavaModel.TYPE_ENTERPRISE_APPLICATION);
        } else if ("pom".equals(extension)) { //$NON-NLS-1$
            type = ArtifactType.ExtendedDocument(JavaModel.TYPE_MAVEN_POM_XML);
        } else {
            type = ArtifactType.Document();
        }
        return type;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        String method = req.getMethod();
        String url = req.getRequestURI();
        logger.info(Messages.i18n.format("maven.repository.servlet.service", method, url)); //$NON-NLS-1$
        super.service(req, resp);
    }

}
