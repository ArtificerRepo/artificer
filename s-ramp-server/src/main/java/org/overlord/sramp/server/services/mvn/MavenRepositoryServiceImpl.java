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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampModelUtils;
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
public class MavenRepositoryServiceImpl implements MavenRepositoryService{

    private static Logger logger = LoggerFactory.getLogger(MavenRepositoryServiceImpl.class);


    /**
     * Creates the query.
     *
     * @param criteria
     *            the criteria
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
            SrampQuery srampQuery = queryManager.createQuery(queryBuilder.toString(),
                    "lastModifiedTimestamp", false); //$NON-NLS-1$

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
     * @see org.overlord.sramp.server.services.MavenRepositoryService#getArtifactContent(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public MavenArtifactWrapper getArtifactContent(String fileName, String groupId, String artifactId,
            String version) throws SrampAtomException, IOException {

        List<String> criteria = new ArrayList<String>();
        List<Object> parameters = new ArrayList<Object>();
        criteria.add("@maven.artifactId = ?"); //$NON-NLS-1$
        criteria.add("@maven.groupId = ?"); //$NON-NLS-1$
        criteria.add("@maven.version = ?"); //$NON-NLS-1$
        criteria.add("@maven.type = ?"); //$NON-NLS-1$

        parameters.add(artifactId);
        parameters.add(groupId);
        parameters.add(version);
        parameters.add(fileName.substring(fileName.lastIndexOf(".") + 1)); //$NON-NLS-1$

        String without_extension = fileName.substring(0, fileName.lastIndexOf(".")); //$NON-NLS-1$
        String classifier = ""; //$NON-NLS-1$
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1); //$NON-NLS-1$
        if (StringUtils.countMatches(without_extension, "-") >= 2) { //$NON-NLS-1$
            int versionIdx = without_extension.indexOf(version);
            int versionLen = version.length();
            int classifierIdx = versionIdx + versionLen + 1;
            if (classifierIdx < without_extension.length()) {
                classifier = without_extension.substring(classifierIdx);
            }
        }

        if (StringUtils.isNotBlank(classifier)) {
            criteria.add("@maven.classifier = ?"); //$NON-NLS-1$
            parameters.add(classifier);
        } else {
            criteria.add("xp2:not(@maven.classifier)"); //$NON-NLS-1$
        }

        ArtifactSet artifactSet = null;
        BaseArtifactType baseArtifact = null;
        try {
            artifactSet = query(criteria, parameters);
            if (artifactSet.size() >= 1) {
                // Found some content!
                baseArtifact = artifactSet.iterator().next();
            }
        } catch (SrampAtomException e) {
            throw e;
        } finally {
            if (artifactSet != null) {
                artifactSet.close();
            }
        }
        if (baseArtifact != null) {
            PersistenceManager persistenceManager = PersistenceFactory.newInstance();
            final InputStream artifactContent;
            ArtifactType artifactType = ArtifactType.valueOf(baseArtifact.getArtifactType());
            Date lastModifiedDate = null;
            if (baseArtifact.getLastModifiedTimestamp() != null) {
                lastModifiedDate = baseArtifact.getLastModifiedTimestamp().toGregorianCalendar().getTime();
            }

            MavenFileExtensionEnum ext = MavenFileExtensionEnum.value(fileName);
            if (ext != null && StringUtils.isNotBlank(ext.getCustomProperty())) {
                // we need to set the input stream with the value of the custom property
                String content = SrampModelUtils.getCustomProperty(baseArtifact, ext.getCustomProperty());
                artifactContent = new ByteArrayInputStream(content.getBytes());
            } else {
                // we need to set the input stream with the artifact content
                try {
                    artifactContent = persistenceManager.getArtifactContent(baseArtifact.getUuid(), artifactType);
                } catch (SrampException e) {
                    logger.error(Messages.i18n.format("maven.resource.get.content.error", baseArtifact.getUuid()), e); //$NON-NLS-1$
                    throw new SrampAtomException(Messages.i18n.format("maven.resource.get.content.error", //$NON-NLS-1$
                            baseArtifact.getUuid()), e);
                }
            }
            String artifactFileName = artifactId + "-" + version; //$NON-NLS-1$
            if (StringUtils.isNotEmpty(classifier)) {
                artifactFileName += "-" + classifier; //$NON-NLS-1$
            }
            artifactFileName += "." + extension; //$NON-NLS-1$
            int contentLength = -1;

            contentLength = Integer.parseInt(baseArtifact.getOtherAttributes().get(
                    SrampConstants.SRAMP_CONTENT_SIZE_QNAME));
            MavenArtifactWrapper wrapper = new MavenArtifactWrapper(artifactContent, contentLength,
                    lastModifiedDate, artifactFileName, artifactType.getMimeType());
            return wrapper;
        } else {
            logger.error(Messages.i18n.format("maven.resource.item.null")); //$NON-NLS-1$
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
    public Set<String> getItems(String path) throws SrampAtomException {
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
    private Set<String> getItems(String groupId, String artifactId, String version)
            throws SrampAtomException {
        List<String> criteria = new ArrayList<String>();
        List<Object> parameters = new ArrayList<Object>();
        Set<String> items = new HashSet<String>();
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
            artifactSet = query(criteria, parameters);
            Iterator<BaseArtifactType> it = artifactSet.iterator();
            while (it.hasNext()) {
                BaseArtifactType artifact = it.next();
                String artifactGroupId = SrampModelUtils.getCustomProperty(artifact, "maven.groupId"); //$NON-NLS-1$
                String artifactVersion = SrampModelUtils.getCustomProperty(artifact, "maven.version"); //$NON-NLS-1$
                String artifactArtifactId = SrampModelUtils.getCustomProperty(artifact, "maven.artifactId"); //$NON-NLS-1$
                String artifactClassifier = SrampModelUtils.getCustomProperty(artifact, "maven.classifier"); //$NON-NLS-1$
                String artifactType = SrampModelUtils.getCustomProperty(artifact, "maven.type"); //$NON-NLS-1$
                if (StringUtils.isNotBlank(artifactId)) {
                    if (StringUtils.isNotBlank(version)) {
                        String toAdd = ""; //$NON-NLS-1$
                        toAdd = artifactArtifactId;
                        toAdd += "-" + artifactVersion; //$NON-NLS-1$
                        if (StringUtils.isNotBlank(artifactClassifier)) {
                            toAdd += "-" + artifactClassifier; //$NON-NLS-1$
                        }
                        toAdd += "." + artifactType; //$NON-NLS-1$
                        items.add(toAdd);
                    } else {
                        items.add(artifactVersion + "/"); //$NON-NLS-1$
                    }
                } else {
                    String restGroupId = artifactGroupId.substring(artifactGroupId.indexOf(groupId)
                            + groupId.length());
                    if (restGroupId.startsWith(".")) { //$NON-NLS-1$
                        String removeBegin = restGroupId.substring(1);
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
            throw e;
        } finally {
            if (artifactSet != null) {
                artifactSet.close();
            }
        }

        return items;
    }


}
