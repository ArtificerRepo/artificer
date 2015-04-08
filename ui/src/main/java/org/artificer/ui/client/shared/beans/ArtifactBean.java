/*
 * Copyright 2013 JBoss Inc
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
package org.artificer.ui.client.shared.beans;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import java.util.ArrayList;
import java.util.List;

/**
 * Models the full details of an artifact in the S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
@Bindable
public class ArtifactBean extends ArtifactSummaryBean {

    private static final long serialVersionUID = ArtifactBean.class.hashCode();

    private String updatedBy;
    private String version;
    private List<String> classifiedBy = new ArrayList<String>();
    private int numRelationships;
    private long contentSize = -1;
    private String contentType;
    private boolean textDocument;
    private String repositoryLink;
    private String repositoryMediaLink;
    private List<ArtifactCommentBean> comments = new ArrayList<>();

    /**
     * Constructor.
     */
    public ArtifactBean() {
    }

    /**
     * @return the updatedBy
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy the updatedBy to set
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * @return the classifiedBy
     */
    public List<String> getClassifiedBy() {
        return classifiedBy;
    }

    /**
     * Sets the classified by.
     * @param classifiedBy
     */
    public void setClassifiedBy(List<String> classifiedBy) {
        this.classifiedBy = classifiedBy;
    }

    /**
     * @param classification
     */
    public void addClassifiedBy(String classification) {
        classifiedBy.add(classification);
    }

    /**
     * @return the numRelationships
     */
    public int getNumRelationships() {
        return numRelationships;
    }

    /**
     * @param numRelationships the numRelationships to set
     */
    public void setNumRelationships(int numRelationships) {
        this.numRelationships = numRelationships;
    }

    /**
     * @return true if this artifact has at least one outgoing relationship
     */
    public boolean hasOutgoingRelationships() {
        return this.numRelationships > 0;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return true if this artifact has text content
     */
    public boolean hasSource() {
        return false;
    }

    /**
     * @return the contentSize
     */
    public long getContentSize() {
        return contentSize;
    }

    /**
     * @param contentSize the contentSize to set
     */
    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the textDocument
     */
    public boolean isTextDocument() {
        return textDocument;
    }

    /**
     * @param textDocument the textDocument to set
     */
    public void setTextDocument(boolean textDocument) {
        this.textDocument = textDocument;
    }

    /**
     * @return true if the artifact is a document artifact
     */
    public boolean isDocument() {
        return this.contentSize != -1;
    }

    /**
     * @return the repositoryLink
     */
    public String getRepositoryLink() {
        return repositoryLink;
    }

    /**
     * @param repositoryLink the repositoryLink to set
     */
    public void setRepositoryLink(String repositoryLink) {
        this.repositoryLink = repositoryLink;
    }

    /**
     * @return the repositoryMediaLink
     */
    public String getRepositoryMediaLink() {
        return repositoryMediaLink;
    }

    /**
     * @param repositoryMediaLink the repositoryMediaLink to set
     */
    public void setRepositoryMediaLink(String repositoryMediaLink) {
        this.repositoryMediaLink = repositoryMediaLink;
    }

    public List<ArtifactCommentBean> getComments() {
        return comments;
    }

    public void setComments(List<ArtifactCommentBean> comments) {
        this.comments = comments;
    }
}
