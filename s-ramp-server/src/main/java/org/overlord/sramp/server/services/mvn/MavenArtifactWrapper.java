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

import java.io.InputStream;
import java.util.Date;

/**
 * Class that encapsulate all the data needed to be added in the response of a
 * get artifact content called.
 *
 * @author David Virgil Naranjo
 */
public class MavenArtifactWrapper {

    private InputStream content;
    private int contentLength;
    private Date lastModifiedDate;
    private String fileName;
    private String contentType;

    /**
     * Instantiates a new maven artifact wrapper.
     *
     * @param content
     *            the content
     * @param contentLength
     *            the content length
     * @param lastModifiedDate
     *            the last modified date
     * @param fileName
     *            the file name
     * @param contentType
     *            the content type
     */
    public MavenArtifactWrapper(InputStream content, int contentLength, Date lastModifiedDate,
            String fileName, String contentType) {
        super();
        this.content = content;
        this.contentLength = contentLength;
        this.lastModifiedDate = lastModifiedDate;
        this.fileName = fileName;
        this.contentType = contentType;
    }


    /**
     * @return the content stream
     */
    public InputStream getContent() {
        return content;
    }

    /**
     * @param content the content stream
     */
    public void setContent(InputStream content) {
        this.content = content;
    }

    /**
     * Gets the content length.
     *
     * @return the content length
     */
    public int getContentLength() {
        return contentLength;
    }

    /**
     * Sets the content length.
     *
     * @param contentLength
     *            the new content length
     */
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Gets the last modified date.
     *
     * @return the last modified date
     */
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * Sets the last modified date.
     *
     * @param lastModifiedDate
     *            the new last modified date
     */
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
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
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     *
     * @param contentType
     *            the new content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
