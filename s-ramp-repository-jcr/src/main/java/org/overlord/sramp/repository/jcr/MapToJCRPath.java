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

import org.overlord.sramp.common.ArtifactType;

public class MapToJCRPath {

    private static int folderDepth     = 3;
    private static String PATH         = "/s-ramp/%1$s/%2$s";

    /**
     * "/artifact/<model>/<type>"
     *
     * @param type - artifact type
     * @return path: "/artifact/<model>/<type>"
     */
    public static String getArtifactTypePath(ArtifactType artifactType) {
        String type = artifactType.getArtifactType().getType();
        //if (artifactType.getUserType()!=null) type = artifactType.getUserType();
        return String.format(PATH, artifactType.getArtifactType().getModel(), type);
    }

    /**
     * "/artifact/<model>/<type>/[btree]"
     *
     * @param uuid - Universally Unique ID
     * @param type - artifact type
     * @return path: "/artifact/<model>/<type>/[btree]"
     */
    public static String getArtifactPath(String uuid, ArtifactType type) {
        return getArtifactTypePath(type) + "/" + bTreePath(uuid);
    }

    private static String bTreePath (String uuid) {
        String bTreePath = "";
        for (int i=0; i < folderDepth; i++) {
            bTreePath += uuid.substring(2*i, 2*i+2) + "/";
        }
        bTreePath += uuid.substring(folderDepth * 2);
        return bTreePath;
    }

}
