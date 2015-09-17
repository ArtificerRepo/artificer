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

import org.apache.commons.io.IOUtils;
import org.artificer.common.ArtifactContent;
import org.artificer.repository.hibernate.entity.ArtificerDocumentArtifact;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * @author Brett Meyer.
 */
public class FilesystemFileManager implements FileManager {

    private final String path;

    public FilesystemFileManager(String path) {
        this.path = path;
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    @Override
    public InputStream read(ArtificerDocumentArtifact artifact) throws Exception {
        String uuid = artifact.getUuid();
        File out = new File(path + uuid);
        return new FileInputStream(out);
    }

    @Override
    public void write(ArtificerDocumentArtifact artifact, ArtifactContent content, EntityManager entityManager) throws Exception {
        InputStream inputStream = null;
        try {
            File out = new File(path + artifact.getUuid());
            out.createNewFile();
            inputStream = content.getInputStream();
            Files.copy(content.getInputStream(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            artifact.setContentPath(path + artifact.getUuid());
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }
}
