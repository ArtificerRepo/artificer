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
package org.artificer.common.maven;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Common methods use for the Maven Facade and the shell's DeployCommand.
 *
 * @author Brett Meyer.
 */
public class MavenUtil {

    public static String gavQuery(MavenGavInfo gavInfo) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("/s-ramp");
        List<String> criteria = new ArrayList<String>();

        criteria.add("@maven.groupId = '" + gavInfo.getGroupId() + "'");
        criteria.add("@maven.artifactId = '" + gavInfo.getArtifactId() + "'");
        criteria.add("@maven.version = '" + gavInfo.getVersion() + "'");
        if (StringUtils.isNotBlank(gavInfo.getType())) {
            criteria.add("@maven.type = '" + gavInfo.getType() + "'");
        }
        if (StringUtils.isNotBlank(gavInfo.getClassifier())) {
            criteria.add("@maven.classifier = '" + gavInfo.getClassifier() + "'");
        }
        if (StringUtils.isNotBlank(gavInfo.getSnapshotId())) {
            criteria.add("@maven.snapshot.id = '" + gavInfo.getSnapshotId() + "'");
        }

        if (criteria.size() > 0) {
            queryBuilder.append("[");
            queryBuilder.append(StringUtils.join(criteria, " and "));
            queryBuilder.append("]");
        }

        return queryBuilder.toString();
    }
}
