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
package org.overlord.sramp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;

/**
 * Helps figure out mime types for artifacts.  Used by the client and the
 * server.
 *
 * @author eric.wittmann@redhat.com
 */
public class MimeTypes {

	private static final MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();
	static {
		InputStream is = null;
		try {
			is = MimeTypes.class.getResourceAsStream("mime.types");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();
			StringBuilder buff = new StringBuilder();
			while (line != null) {
				buff.append(line);
				buff.append("\n");
				line = reader.readLine();
			}
			mimeTypes.addMimeTypes(buff.toString());
		} catch (Throwable t) {
			// Eat it.
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Returns the content-type for the given file/resource/artifact name.
	 * @param name
	 * @return an appropriate content-type
	 */
	public static String getContentType(String name) {
		if (name == null)
			return null;
		else
			return mimeTypes.getContentType(name);
	}
	
	  /**
     * Figures out the mime type of the new artifact given the POSTed Content-Type, the name
     * of the uploaded file, and the S-RAMP arifact type.  If the artifact type is Document
     * then the other two pieces of information are used to determine an appropriate mime type.
     * If no appropriate mime type can be determined for core/Document, then binary is returned.
     * @param contentType the content type request header
     * @param fileName the slug request header
     * @param artifactType the artifact type (based on the endpoint POSTed to)
     */
    public static String determineMimeType(String contentType, String fileName, ArtifactType artifactType) {
        if (artifactType.getArtifactType() == ArtifactTypeEnum.Document || artifactType.getArtifactType() == ArtifactTypeEnum.UserDefinedArtifactType) {
            if (contentType != null && contentType.trim().length() > 0)
                return contentType;
            if (fileName != null) {
                String ct = MimeTypes.getContentType(fileName);
                if (ct != null)
                    return ct;
            }
            return "application/octet-stream";
        } else {
            // Everything else is an XML file
            return "application/xml";
        }
    }

}
