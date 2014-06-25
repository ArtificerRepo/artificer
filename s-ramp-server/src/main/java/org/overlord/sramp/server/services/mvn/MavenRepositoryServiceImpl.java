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
package org.overlord.sramp.server.services.mvn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.common.ArtifactType;
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
import org.overlord.sramp.server.services.MavenRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the maven repository service. It connects to s-ramp and
 * return the data, for being displayed as maven requires.
 *
 * @author David Virgil Naranjo
 */
public class MavenRepositoryServiceImpl implements MavenRepositoryService {

    private static Logger logger = LoggerFactory.getLogger(MavenRepositoryServiceImpl.class);

    /**
     * Creates the query.
     *
     * @param criteria
     *            the criteria
     * @param parameters
     *            the parameters
     * @return the artifact set
     * @throws SrampAtomException
     *             the sramp atom exception
     */
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
            SrampQuery srampQuery = queryManager.createQuery(queryBuilder.toString(), "lastModifiedTimestamp", false); //$NON-NLS-1$

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

    /**
     * Gets the artifact content.
     *
     * @param metadata
     *            the metadata
     * @return the artifact content
     * @throws SrampAtomException
     *             the sramp atom exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see org.overlord.sramp.server.services.MavenRepositoryService#getArtifactContent(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public MavenArtifactWrapper getArtifactContent(MavenMetaData metadata) throws MavenRepositoryException {
        // List of criterias and the parameters associated
        List<String> criteria = new ArrayList<String>();
        List<Object> parameters = new ArrayList<Object>();
        criteria.add("@maven.artifactId = ?"); //$NON-NLS-1$
        criteria.add("@maven.groupId = ?"); //$NON-NLS-1$
        criteria.add("@maven.type = ?"); //$NON-NLS-1$

        parameters.add(metadata.getArtifactId());
        parameters.add(metadata.getGroupId());
        // If there is a parent type (in case of sha1 or md5, it is passed as
        // parameter
        if (StringUtils.isNotBlank(metadata.getParentType())) {
            parameters.add(metadata.getParentType());
        } else {
            parameters.add(metadata.getType());
        }
        // Not always it is passed the maven version. This is the case when it
        // is requested a file (normally maven-metadata.xml) that is stored in
        // the artifact subfolder
        if (StringUtils.isNotBlank(metadata.getVersion())) {
            criteria.add("@maven.version = ?"); //$NON-NLS-1$
            parameters.add(metadata.getVersion());
        }
        // If it is included a classfier it is added as parameter.
        if (StringUtils.isNotBlank(metadata.getClassifier())) {
            criteria.add("@maven.classifier = ?"); //$NON-NLS-1$
            parameters.add(metadata.getClassifier());
        } else {
            criteria.add("xp2:not(@maven.classifier)"); //$NON-NLS-1$
        }

        ArtifactSet artifactSet = null;
        BaseArtifactType baseArtifact = null;
        try {
            // query based on the previous criterias
            artifactSet = query(criteria, parameters);
            if (artifactSet.size() >= 1) {
                // Found some content!
                baseArtifact = artifactSet.iterator().next();
            }
        } catch (SrampAtomException e) {
            throw new MavenRepositoryException(Messages.i18n.format(""), e);
        } finally {
            if (artifactSet != null) {
                artifactSet.close();
            }
        }
        // If the artifact returned is not null, then the content will be
        // retrieved
        if (baseArtifact != null) {
            PersistenceManager persistenceManager = PersistenceFactory.newInstance();
            final InputStream artifactContent;
            ArtifactType artifactType = ArtifactType.valueOf(baseArtifact.getArtifactType());
            Date lastModifiedDate = null;
            if (baseArtifact.getLastModifiedTimestamp() != null) {
                lastModifiedDate = baseArtifact.getLastModifiedTimestamp().toGregorianCalendar().getTime();
            }
            int contentLength = -1;
            MavenFileExtensionEnum ext = MavenFileExtensionEnum.value(metadata.getType());
            if (ext != null && StringUtils.isNotBlank(ext.getCustomProperty())) {
                // we need to set the input stream with the value of the custom
                // property
                String content = SrampModelUtils.getCustomProperty(baseArtifact, ext.getCustomProperty());
                if (StringUtils.isNotBlank(content)) {
                    artifactContent = new ByteArrayInputStream(content.getBytes());
                    contentLength = content.length();
                } else {
                    logger.info(Messages.i18n.format("maven.resource.get.subcontent.not.found", baseArtifact.getUuid(), ext.getCustomProperty())); //$NON-NLS-1$
                    return null;
                }

            } else {
                // we need to set the input stream with the artifact content
                try {
                    artifactContent = persistenceManager.getArtifactContent(baseArtifact.getUuid(), artifactType);
                } catch (SrampException e) {
                    logger.error(Messages.i18n.format("maven.resource.get.content.error", baseArtifact.getUuid()), e); //$NON-NLS-1$

                    throw new MavenRepositoryException(Messages.i18n.format("maven.resource.get.content.error", //$NON-NLS-1$
                            baseArtifact.getUuid()), e);

                }
                String contentSize = baseArtifact.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME);
                if (StringUtils.isNotBlank(contentSize)) {
                    contentLength = Integer.parseInt(contentSize);
                }

            }

            MavenArtifactWrapper wrapper = new MavenArtifactWrapper(artifactContent, contentLength, lastModifiedDate, metadata.getFileName(),
                    artifactType.getMimeType());
            return wrapper;
        } else {
            logger.error(Messages.i18n.format("maven.resource.item.null", metadata.toString())); //$NON-NLS-1$
            // Return null so that the servlet can return a 404
            return null;
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.overlord.sramp.server.services.MavenRepositoryService#getItems(java
     * .lang.String)
     */
    @Override
    public Set<String> getItems(String path) throws MavenRepositoryException {
        // It try to get the items based on the different combinations of
        // groupId versionId and artifactId
        Set<String> items = null;
        String[] tokens = path.split("/"); //$NON-NLS-1$
        String groupId = ""; //$NON-NLS-1$
        String version = ""; //$NON-NLS-1$
        String artifactId = ""; //$NON-NLS-1$
        // Search by groupId
        for (int i = 0; i < tokens.length; i++) {
            if (i != 0) {
                groupId += "."; //$NON-NLS-1$
            }
            groupId += tokens[i];
        }
        items = getItems(groupId, null, null);
        if ((items == null || items.size() == 0) && tokens.length >= 2) {
            groupId = ""; //$NON-NLS-1$
            version = ""; //$NON-NLS-1$
            artifactId = ""; //$NON-NLS-1$
            // Search by groupId and artifactId
            for (int i = 0; i < tokens.length - 1; i++) {
                if (i != 0) {
                    groupId += "."; //$NON-NLS-1$
                }
                groupId += tokens[i];
            }
            artifactId = tokens[tokens.length - 1];
            items = getItems(groupId, artifactId, null);
            if ((items == null || items.size() == 0) && tokens.length >= 3) {
                groupId = ""; //$NON-NLS-1$
                version = ""; //$NON-NLS-1$
                artifactId = ""; //$NON-NLS-1$
                for (int i = 0; i < tokens.length - 2; i++) {
                    if (i != 0) {
                        groupId += "."; //$NON-NLS-1$
                    }
                    groupId += tokens[i];
                }
                artifactId = tokens[tokens.length - 2];

                version = tokens[tokens.length - 1];
                items = getItems(groupId, artifactId, version);
            }
        }
        return items;
    }

    /**
     * Gets the items.
     *
     * @param groupId
     *            the group id
     * @param artifactId
     *            the artifact id
     * @param version
     *            the version
     * @return the items
     * @throws SrampAtomException
     *             the sramp atom exception
     */
    private Set<String> getItems(String groupId, String artifactId, String version) throws MavenRepositoryException {
        // Add the criterias/parameters depends on the method parameters
        List<String> criteria = new ArrayList<String>();
        List<Object> parameters = new ArrayList<Object>();

        Set<String> items = new TreeSet<String>();
        if (StringUtils.isNotBlank(groupId)) {
            criteria.add("fn:matches(@maven.groupId, ?)"); //$NON-NLS-1$
            parameters.add(groupId + ".*"); //$NON-NLS-1$
        } else {
            criteria.add("@maven.groupId"); //$NON-NLS-1$
        }

        if (StringUtils.isNotBlank(version)) {
            criteria.add("@maven.version= ?"); //$NON-NLS-1$
            parameters.add(version);
        }
        if (StringUtils.isNotBlank(artifactId)) {
            criteria.add("@maven.artifactId= ?"); //$NON-NLS-1$
            parameters.add(artifactId);
        }
        criteria.add("@derived='false'"); //$NON-NLS-1$

        ArtifactSet artifactSet = null;
        try {
            // Query the previous added criterias
            artifactSet = query(criteria, parameters);
            Iterator<BaseArtifactType> it = artifactSet.iterator();
            // Iterate upon all the items retrieved
            while (it.hasNext()) {
                BaseArtifactType artifact = it.next();
                String artifactGroupId = SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_MAVEN_GROUP_ID);
                String artifactArtifactId = SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_MAVEN_ARTIFACT_ID);

                // If it is supposed to belong to an artifact
                if (StringUtils.isNotBlank(artifactId)) {
                    String toAdd = ""; //$NON-NLS-1$
                    // If the request is about listing a version folder
                    if (StringUtils.isNotBlank(version)) {
                        // It is added the artifact
                        toAdd = artifact.getName();
                        items.add(toAdd);
                        String md5 = SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_MD5);
                        String sha1 = SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_MAVEN_HASH_SHA1);
                        // If the artifact contains an md5 or sha1 then it is
                        // added as another maven entry
                        if (StringUtils.isNotBlank(md5)) {
                            String itemAdded = toAdd + ".md5"; //$NON-NLS-1$
                            items.add(itemAdded);
                        }
                        if (StringUtils.isNotBlank(sha1)) {
                            String itemAdded = toAdd + ".sha1"; //$NON-NLS-1$
                            items.add(itemAdded);
                        }
                    } else {// It is being listed the artifact folder, listing
                            // all the files contained (could be contained a
                            // maven-metadata.xml) and all the artifact's
                            // versions
                        String artifactVersionId = SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_MAVEN_VERSION);
                        if (StringUtils.isNotBlank(artifactVersionId)) {
                            items.add(artifactVersionId);
                        } else {
                            items.add(artifact.getName());
                        }

                    }
                } else {// In this case, we need to list a part of a groupId
                    // The remaining group id to be listed
                    String restGroupId = artifactGroupId.substring(artifactGroupId.indexOf(groupId) + groupId.length());
                    // This means it is being listed an element inside of the
                    // groupId
                    if (restGroupId.startsWith(".")) { //$NON-NLS-1$
                        String removeBegin = restGroupId.substring(1);
                        // Listing next element
                        if (removeBegin.contains(".")) { //$NON-NLS-1$
                            items.add(removeBegin.substring(0, removeBegin.indexOf(".")) + "/"); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            items.add(removeBegin + "/"); //$NON-NLS-1$
                        }
                    } else {
                        if (restGroupId.contains(".")) { //$NON-NLS-1$
                            items.add(restGroupId.substring(0, restGroupId.indexOf(".")) + "/"); //$NON-NLS-1$ //$NON-NLS-2$
                        } else if (restGroupId.trim().equals("")) { //$NON-NLS-1$
                            items.add(artifactArtifactId + "/"); //$NON-NLS-1$
                        } else {
                            items.add(restGroupId);
                        }
                    }
                }
            }
        } catch (SrampAtomException e) {
            throw new MavenRepositoryException(Messages.i18n.format("maven.resource.get.items.error", groupId, artifactId, version), e);
        } finally {
            if (artifactSet != null) {
                artifactSet.close();
            }
        }

        return items;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.overlord.sramp.server.services.MavenRepositoryService#uploadArtifact
     * (org.overlord.sramp.server.services.mvn.MavenMetaData,
     * java.io.InputStream)
     */
    @Override
    public String uploadArtifact(MavenMetaData metadata, InputStream content) throws MavenRepositoryException {
        String uuid = null;
        if (content == null) {
            throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.no.content"));
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
            throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.query.error", metadata.toString()), e);
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
                    String content_value = "";
                    try {
                        content_value = IOUtils.toString(content);
                    } catch (IOException e1) {
                        throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.error", metadata.toString()), e1);
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
                                throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.error", metadata.toString()), e);
                            }
                        }
                    }
                } else {
                    try {
                        persistenceManager.updateArtifactContent(baseArtifact.getUuid(), artifactType, content);
                    } catch (SrampException e) {
                        throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.error", metadata.toString()), e);
                    }
                }

            }
        } else {
            BaseArtifactType persisted = null;
            // If there is an existing artifact in s-ramp it would be updaded
            // with the new content
            if (baseArtifact != null) {
                ArtifactType artifactType = ArtifactType.valueOf(baseArtifact);
                try {
                    persistenceManager.updateArtifactContent(baseArtifact.getUuid(), artifactType, content);
                } catch (SrampException e) {
                    throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.update.content.error",
                            baseArtifact.getUuid()), e);
                }
                persisted = baseArtifact;
            } else {
                // we need to create a new artifact in s-ramp and persist the
                // content
                ArtifactType artifactType = determineArtifactType(fileName);
                BaseArtifactType baseArtifactType = artifactType.newArtifactInstance();
                try {
                    persisted = persistenceManager.persistArtifact(baseArtifactType, content);
                } catch (SrampException e1) {
                    throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.new.content.error"), e1);
                }
            }
            // Store the metadata to the persisted artifact
            SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_GROUP_ID, metadata.getGroupId());
            SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_ARTIFACT_ID, metadata.getArtifactId());
            SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_VERSION, metadata.getVersion());

            if (StringUtils.isNotBlank(metadata.getClassifier())) {
                SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_CLASSIFIER, metadata.getClassifier()); //$NON-NLS-1$;
            }
            if (StringUtils.isNotBlank(metadata.getType())) {
                SrampModelUtils.setCustomProperty(persisted, JavaModel.PROP_MAVEN_TYPE, metadata.getType()); //$NON-NLS-1$
            }
            try {
                // Persist the content size, because it will be required when
                // reading
                persisted.getOtherAttributes().put(SrampConstants.SRAMP_CONTENT_SIZE_QNAME, content.available() + "");
            } catch (IOException e) {
                logger.error("");
            }

            persisted.setName(metadata.getFileName());
            ArtifactType artifactType = ArtifactType.valueOf(persisted);
            try {
                persistenceManager.updateArtifact(persisted, artifactType);
            } catch (SrampException e) {
                throw new MavenRepositoryException(Messages.i18n.format("maven.resource.upload.sramp.update.content.metadata.error",
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

}
