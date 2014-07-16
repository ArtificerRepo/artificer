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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.QueryManagerFactory;
import org.overlord.sramp.repository.query.SrampQuery;
import org.overlord.sramp.server.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class that construct a MavenMetadata object based on an url String
 * which correct format would be groupId/groupId.../version/file_name In case
 * that the url can not be converted to a correct maven metadata object, it
 * would return a MavenMetadata with all its attributes with an empty value.
 *
 * @author David Virgil Naranjo
 */
public class MavenMetaDataBuilder {

    private static Logger logger = LoggerFactory.getLogger(MavenMetaDataBuilder.class);

    /**
     * Builds the Maven Metadata based on a URL, which format is supposed to be /groupId.../artifactId/versionId/filename
     *
     * @param url
     *            the url
     * @return the maven meta data
     */
    public static MavenMetaData build(String url) {
        String groupId = ""; //$NON-NLS-1$
        String artifactId = ""; //$NON-NLS-1$
        String version = ""; //$NON-NLS-1$
        String type = ""; //$NON-NLS-1$
        String classifier = ""; //$NON-NLS-1$
        String parentType = ""; //$NON-NLS-1$
        String fileName = ""; //$NON-NLS-1$
        String snapshotId = "";

        if (url.startsWith("/")) { //$NON-NLS-1$
            url = url.substring(1);
        }

        int lastPathSegmentIdx = url.lastIndexOf('/');
        String lastPathSegment = null;
        if (lastPathSegmentIdx != -1) {
            lastPathSegment = url.substring(lastPathSegmentIdx + 1);
        }
        //This means the last part contains a file with a file extension
        if (lastPathSegment != null && lastPathSegment.indexOf('.') != -1) {
            String[] tokens = url.split("/"); //$NON-NLS-1$
            if (tokens != null && tokens.length > 0) {
              //There could be files either in the artifact folder(maven-metadata.xml) or in the version folder.
                //Then for that it is mandatory to have more than 2 tokens. At least one for groupId, one for artifactId and one for fileName
                if (tokens.length >= 3) {

                    if (lastPathSegment.contains("maven-metadata.xml")) { //$NON-NLS-1$
                        String possibleGroupId = ""; //$NON-NLS-1$

                        boolean isGroup = false;
                        // There are checked the different possibilities about
                        // groupsIds.
                        // It is checked in s-ramp

                        // It is checked from the end to the beginning to avoid
                        // the case that an artifact is contained in a groupId
                        // that contains subgroups
                        // First store the biggest possibility, not until the
                        // last token. The last would be the filename and the
                        // previous could be the artifactId
                        for (int i = 0; i < tokens.length - 2; i++) {
                            possibleGroupId += tokens[i];
                            if (i < tokens.length - 1) {
                                possibleGroupId += "."; //$NON-NLS-1$
                            }

                        }
                        // For example for org.overlord.test the number of
                        // checks is going to be 3
                        int iterations = StringUtils.countMatches(possibleGroupId, ".") + 1; //$NON-NLS-1$
                        for (int i = 0; i < iterations; i++) {
                            if (isGroupId(possibleGroupId)) {
                                isGroup = true;
                                break;
                            }
                            if (possibleGroupId.lastIndexOf(".") != -1) { //$NON-NLS-1$
                                possibleGroupId = possibleGroupId.substring(0, possibleGroupId.lastIndexOf(".")); //$NON-NLS-1$
                            }
                        }
                        // If there is a group in s-ramp that matches the
                        // request
                        if (isGroup) {
                            // This means there is an existing groupId in s-ramp
                            groupId = possibleGroupId;
                            String lastGroupIdToken = ""; //$NON-NLS-1$
                            if (groupId.contains(".")) { //$NON-NLS-1$
                                lastGroupIdToken = groupId.substring(groupId.lastIndexOf(".") + 1); //$NON-NLS-1$
                            } else {
                                lastGroupIdToken = groupId;
                            }
                            int groupIdIndex = 0;
                            for (groupIdIndex = 0; groupIdIndex < tokens.length; groupIdIndex++) {
                                if (tokens[groupIdIndex].equals(lastGroupIdToken)) {
                                    break;
                                }
                            }

                            if (tokens.length == (groupIdIndex + 3)) {
                                // This means it comes a file under the artifact
                                // id.
                                // Example:
                                // /org/test/artifact/maven-metadata.xml
                                // where org/test is the groupId
                                artifactId = tokens[groupIdIndex + 1];
                                if (!existVersion(groupId, tokens[groupIdIndex + 2])) {
                                    fileName = tokens[groupIdIndex + 2];
                                }
                            } else if (tokens.length == (groupIdIndex + 4)) {
                                // This means it comes a file under the version
                                // id.
                                // Example:
                                // /org/test/artifact/0.0.1/maven-metadata.xml
                                // where
                                // org/test is the groupId abd
                                // artifactId=artifact
                                // and version=0.0.1
                                artifactId = tokens[groupIdIndex + 1];
                                version = tokens[groupIdIndex + 2];
                                fileName = tokens[groupIdIndex + 3];
                            }

                        } else {
                            // If the groupId can not be found in s-ramp then,
                            // it is built a default metadata.
                            // Remember that this method is called, both listing
                            // content and creating new content
                            version = tokens[tokens.length - 2];
                            for (int i = 0; i < tokens.length - 2; i++) {
                                if (i < tokens.length - 3) {
                                    if (i != 0) {
                                        groupId += "."; //$NON-NLS-1$
                                    }
                                    groupId += tokens[i];
                                } else {
                                    artifactId = tokens[i];
                                }
                            }
                            fileName = tokens[tokens.length - 1];

                        }
                    } else {
                        //If the groupId can not be found in s-ramp then, it is built a default metadata.
                        //Remember that this method is called, both listing content and creating new content
                        version = tokens[tokens.length - 2];
                        for (int i = 0; i < tokens.length - 2; i++) {
                            if (i < tokens.length - 3) {
                                if (i != 0) {
                                    groupId += "."; //$NON-NLS-1$
                                }
                                groupId += tokens[i];
                            } else {
                                artifactId = tokens[i];
                            }
                        }
                        fileName = tokens[tokens.length - 1];

                    }

                    //It is parsed the fileName to get information about the classifier
                    if (StringUtils.isNotBlank(fileName)) {
                        type = fileName.substring(fileName.lastIndexOf(".") + 1); //$NON-NLS-1$

                        classifier = ""; //$NON-NLS-1$
                        //We know the information about classifier comes in files that are under the version folder

                        if (StringUtils.isNotBlank(version) ) {
                            String versionParsed=""; //$NON-NLS-1$
                            //When the version contains a -SNAPSHOT, then the filename is artifactId-versionWithoutSNAPSHOT
                            //The snapshot keyword is substitute by a timestamp-counter
                            if(version.contains("-SNAPSHOT")){ //$NON-NLS-1$
                                versionParsed=version.substring(0,version.lastIndexOf("-SNAPSHOT")); //$NON-NLS-1$
                            }
                            else{
                                versionParsed=version;
                            }
                            if(fileName.startsWith(artifactId + "-" + versionParsed)){ //$NON-NLS-1$
                                String without_extension = fileName.substring(0, fileName.lastIndexOf(".")); //$NON-NLS-1$
                                if (StringUtils.countMatches(without_extension, "-") >= 2) { //$NON-NLS-1$
                                    int versionIdx = without_extension.indexOf(versionParsed);
                                    int versionLen = versionParsed.length();
                                    int classifierIdx = versionIdx + versionLen;
                                    if (classifierIdx < without_extension.length()) {
                                        //If it is an snapshop
                                        if (version.contains("SNAPSHOT") && !version.equals(versionParsed)) { //$NON-NLS-1$
                                            String rest = without_extension.substring(classifierIdx + 1);
                                            //All the tokens from the clasifierIdx to the end are splitted by '-'
                                            String[] tokens_file_name = rest.split("-"); //$NON-NLS-1$
                                            if (tokens_file_name.length > 0) {
                                                //It is expected a timestamp
                                                if (isMavenTimeStamp(tokens_file_name[0])) {
                                                    if (tokens_file_name.length > 1) {
                                                        try {
                                                            //The it is expected maybe a counter
                                                            String[] counter = StringUtils.split(
                                                                    tokens_file_name[1], "."); //$NON-NLS-1$
                                                            Integer.parseInt(counter[0]);
                                                            snapshotId = tokens_file_name[0] + "-" + counter[0];
                                                            if (tokens_file_name.length > 2) {
                                                                classifier = tokens_file_name[2];
                                                            } else if (counter.length == 2) {
                                                                parentType = counter[1];
                                                            }
                                                        } catch (NumberFormatException nfe) {
                                                            //In case the parseInt throw an exception it means there is not any counter, and then the classifier goes to the end of the filename
                                                            classifier = rest.substring(tokens_file_name[0]
                                                                    .length() + 1);
                                                        }
                                                    }
                                                } else {
                                                    classifier = rest;
                                                }
                                            }
                                        } else {
                                            //This means the version is not snapshot or there is no timestamp in the file
                                            classifier = without_extension.substring(classifierIdx);
                                        }

                                        if (StringUtils.isNotBlank(classifier)) {
                                            if (classifier.contains(".")) {//$NON-NLS-1$
                                                if (classifier.startsWith(".")) { //$NON-NLS-1$
                                                    parentType = classifier.substring(1);
                                                    classifier = ""; //$NON-NLS-1$
                                                } else {
                                                    parentType = classifier
                                                            .substring(classifier.lastIndexOf(".") + 1); //$NON-NLS-1$
                                                    classifier = classifier.substring(0,
                                                            classifier.lastIndexOf(".")); //$NON-NLS-1$
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }

                    }

                }
            }
        }
        //If the type, belongs to the MavenFileExtensionEnum, it means than it is sha1 or md5.
        if (MavenFileExtensionEnum.value(type) != null) {
            //Then the parent type has to be filled. We know that the parent type can be taken from the filename
            String without_ext = fileName.substring(0, fileName.lastIndexOf("." + type)); //$NON-NLS-1$
            if (without_ext.contains(".")) { //$NON-NLS-1$
                parentType = without_ext.substring(without_ext.lastIndexOf(".") + 1); //$NON-NLS-1$
            }
        }

        return new MavenMetaData(groupId, artifactId, version, type, classifier, snapshotId, parentType, fileName);

    }

    /**
     * Checks that there are items in s-ramp with an specific group maven id
     *
     * @param groupId
     *            the group id
     * @return true, if is group id
     */
    private static boolean isGroupId(String groupId) {
        String query = "/s-ramp[@maven.groupId = ?]"; //$NON-NLS-1$
        try {
            QueryManager queryManager = QueryManagerFactory.newInstance();
            SrampQuery srampQuery = queryManager.createQuery(query);

            srampQuery.setString(groupId);

            org.overlord.sramp.repository.query.ArtifactSet artifactSet = srampQuery.executeQuery();
            if (artifactSet.size() > 0) {
                return true;
            }
        } catch (Throwable e) {
            logger.error(Messages.i18n.format("maven.resource.query.error", query), e); //$NON-NLS-1$
        }

        return false;
    }

    /**
     * Checks that there are items in s-ramp with an specific group and version
     * maven id
     *
     * @param groupId
     *            the group id
     * @param version
     *            the version
     * @return true, if successful
     */
    private static boolean existVersion(String groupId, String version) {
        String query = "/s-ramp[@maven.groupId = ? and @maven.version = ? ]"; //$NON-NLS-1$
        try {
            QueryManager queryManager = QueryManagerFactory.newInstance();
            SrampQuery srampQuery = queryManager.createQuery(query);

            srampQuery.setString(groupId);
            srampQuery.setString(version);
            org.overlord.sramp.repository.query.ArtifactSet artifactSet = srampQuery.executeQuery();
            if (artifactSet.size() > 0) {
                return true;
            }
        } catch (Throwable e) {
            logger.error(Messages.i18n.format("maven.resource.query.error", query), e); //$NON-NLS-1$
        }

        return false;
    }

    /**
     * Checks if the parameter is a maven time stamp.
     *
     * @param possibleTimestamp
     *            the possible timestamp
     * @return true, if is maven time stamp
     */
    private static boolean isMavenTimeStamp(String possibleTimestamp) {
        // Timestamp included by maven in the snapthots artifacts.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss"); //$NON-NLS-1$
        try {
            sdf.parse(possibleTimestamp);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

}
