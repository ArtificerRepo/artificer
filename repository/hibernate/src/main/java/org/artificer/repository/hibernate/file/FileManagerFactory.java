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
package org.artificer.repository.hibernate.file;

import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerConstants;

/**
 * @author Brett Meyer.
 */
public class FileManagerFactory {

    private static FileManager instance = null;

    public static FileManager getInstance() {
        if (instance == null) {
            String storage = ArtificerConfig.getConfigProperty(ArtificerConstants.ARTIFICER_FILE_STORAGE, "blob");
            switch (storage) {
                case "filesystem":
                    instance = new FilesystemFileManager(
                            ArtificerConfig.getConfigProperty(ArtificerConstants.ARTIFICER_FILE_STORAGE_FILESYSTEM_PATH, ""));
                    break;
                default:
                    instance = new BlobFileManager();
            }
        }
        return instance;
    }

    // for testing...
    public static void reset() {
        instance = null;
    }
}
