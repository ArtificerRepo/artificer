/*
 * Copyright 2012 JBoss Inc
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
package org.artificer.server.mime;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtifactTypeEnum;

/**
 * Helps figure out mime types for artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
public class MimeTypes {

	private static final org.apache.tika.Tika tika = new Tika();

	/**
	 * Gets the content type from the given file.
	 * @param file
	 */
	public static String getContentType(File file) {
	    BufferedInputStream is = null;
	    try {
	        is = new BufferedInputStream(new FileInputStream(file));
	        return getContentType(file.getName(), is);
	    } catch (IOException e) {
	        return null;
	    } finally {
	        IOUtils.closeQuietly(is);
	    }
	}

	/**
	 * Returns the content-type for the given file/resource/artifact name and/or contents.
	 *
	 * @param name the name of the file/resource/artifact or null if not known.
	 * @param stream the stream containing the data of the file/resource/artifact
	 *               or null if none can be provided. Note that the stream <b>MUST</b> support
	 *               mark ({@link java.io.InputStream#markSupported()}. For example the {@link java.io.BufferedInputStream}
	 *               supports this.
	 * @return an appropriate content-type
	 */
    public static String getContentType(String name, InputStream stream) {
        try {
            return tika.detect(stream, name);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Figures out the mime type of the new artifact given the name of the uploaded file, its content and
     * the S-RAMP artifact type.  If the artifact type is Document or ExtendedArtifactType
     * then the other two pieces of information are used to determine an appropriate mime type, all other artifact
     * types in S-RAMP are XML (application/xml).
     * If no appropriate mime type can be determined for core/Document, then binary is returned.
     *
     * @param fileName the slug request header
     * @param stream the input stream with the file's data. Consult {@link #getContentType(String, java.io.InputStream)} for restrictions on the stream.
     * @param artifactType the artifact type (based on the endpoint POSTed to)
     */
    public static String determineMimeType(String fileName, InputStream stream, ArtifactType artifactType) {
        if (artifactType.getArtifactType() == ArtifactTypeEnum.Document
                || artifactType.getArtifactType() == ArtifactTypeEnum.ExtendedDocument) {
            String ct = getContentType(fileName, stream);
            return ct == null ? "application/octet-stream" : ct; //$NON-NLS-1$
        } else {
            // Everything else is an XML file
            return "application/xml"; //$NON-NLS-1$
        }
    }

}
