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

import org.apache.commons.lang.StringUtils;

/**
 * Encapsulates maven meta-data information for a file.
 *
 * @author David Virgil Naranjo
 */
public class MavenMetaData {

    public static final String SEPARATOR_FULL_NAME = ":";
    private String groupId = ""; //$NON-NLS-1$
    private String artifactId = ""; //$NON-NLS-1$
    private String version = ""; //$NON-NLS-1$
    private String type = ""; //$NON-NLS-1$
    private String classifier = ""; //$NON-NLS-1$

    private String parentType = ""; //$NON-NLS-1$

    private String fileName = ""; //$NON-NLS-1$

    private String snapshotId = ""; //$NON-NLS-1$




    /**
     * Instantiates a new maven meta data.
     *
     * @param groupId
     *            the group id
     * @param artifactId
     *            the artifact id
     * @param version
     *            the version
     * @param type
     *            the type
     * @param classifier
     *            the classifier
     * @param parentType
     *            the parent type
     * @param fileName
     *            the file name
     */
    public MavenMetaData(String groupId, String artifactId, String version, String type, String classifier, String snapshotId,
            String parentType, String fileName) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
        this.parentType = parentType;
        this.fileName = fileName;
        this.snapshotId = snapshotId;
    }

    /**
     * Sets the file name.
     *
     * @param fileName
     *            the new file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Checks if is artifact.
     *
     * @return true, if is artifact
     */
    public boolean isArtifact() {
        if (StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(fileName)) {
            return true;
        }
        return false;
    }

    /**
     * Gets the group id.
     *
     * @return the group id
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the group id.
     *
     * @param groupId
     *            the new group id
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Gets the artifact id.
     *
     * @return the artifact id
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Sets the artifact id.
     *
     * @param artifactId
     *            the new artifact id
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version
     *            the new version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the classifier.
     *
     * @return the classifier
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Sets the classifier.
     *
     * @param classifier
     *            the new classifier
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * Gets the parent type.
     *
     * @return the parent type
     */
    public String getParentType() {
        return parentType;
    }

    /**
     * Sets the parent type.
     *
     * @param parentType
     *            the new parent type
     */
    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getParentFileName() {
        if (StringUtils.isBlank(parentType)) {
            return fileName;
        } else {
            return fileName.substring(0, fileName.lastIndexOf(".")); //$NON-NLS-1$
        }
    }

    public boolean isSnapshotVersion() {
        if (StringUtils.isNotBlank(version) && version.contains("SNAPSHOT")) {//$NON-NLS-1$
            return true;
        }
        return false;
    }

    public String getFullName() {
        StringBuilder builder = new StringBuilder("");
        builder.append(groupId).append(SEPARATOR_FULL_NAME);
        if (StringUtils.isNotBlank(version)) {
            builder.append(version);
        }
        builder.append(SEPARATOR_FULL_NAME);
        builder.append(fileName);
        return builder.toString();
    }

    @Override
    public String toString() {
        return "MavenMetaData [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ", type=" + type + ", classifier=" + classifier + ", parentType=" + parentType //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ", fileName=" + fileName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
