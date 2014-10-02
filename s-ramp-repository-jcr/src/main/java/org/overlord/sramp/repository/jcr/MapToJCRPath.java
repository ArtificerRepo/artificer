/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository.jcr;

import javax.jcr.RepositoryException;
import static org.overlord.sramp.repository.jcr.JCRConstants.ROOT_PATH;

public class MapToJCRPath {

    private static int folderDepth     = 3;

    /**
     * Given an artifact path, return the path to where that artifact would live if it were to
     * be put in the trash.
     * @param artifactNode
     * @throws RepositoryException
     */
    public static String getTrashPath(String nodePath) throws RepositoryException {
        return nodePath.replace(ROOT_PATH, ROOT_PATH + "-trash");
    }

    /**
     * "/s-ramp/artifacts/[btree]"
     *
     * @param uuid - Universally Unique ID
     * @return path: "/s-ramp/artifacts/[btree]"
     */
    public static String getArtifactPath(String uuid) {
        return ROOT_PATH + "/artifacts/" + bTreePath(uuid);
    }

    /**
     * "/s-ramp/artifacts/[UUID]"
     *
     * @param uuid - Universally Unique ID
     * @return path: "/s-ramp/ontologies/[UUID]"
     */
    public static String getOntologyPath(String uuid) {
        return ROOT_PATH + "/ontologies/" + uuid;
    }

    /**
     * "/s-ramp/queries/queryName"
     *
     * @param queryName
     * @return path: "/s-ramp/queries/queryName"
     */
    public static String getStoredQueryPath(String queryName) {
        return ROOT_PATH + "/queries/" + queryName;
    }

    /**
     * Creates a b-tree path out of the given UUID.  This should add depth to the tree and
     * spread out the nodes within JCR.
     * @param uuid
     */
    private static String bTreePath (String uuid) {
        String bTreePath = "";
        int segmentStartIdx = 0;
        for (int i=0; i < folderDepth; i++) {
            int segmentEndIdx = segmentStartIdx + 2;
            if (segmentEndIdx > uuid.length()) {
                break;
            }
            bTreePath += uuid.substring(segmentStartIdx, segmentEndIdx) + "/";
            segmentStartIdx += 2;
        }
        bTreePath += uuid.substring(segmentStartIdx);
        return bTreePath;
    }

}
