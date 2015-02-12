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

import java.io.Serializable;
import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Bean that models an Ontology Summary.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class OntologySummaryBean implements Serializable {

    private static final long serialVersionUID = -8810949526904944644L;
    
    private String uuid;
    private String id;
    private String base;
    private String label;
    private String comment;
    private String createdBy;
    private Date createdOn;
    private Date lastModifiedOn;

    /**
     * Constructor.
     */
    public OntologySummaryBean() {
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the base
     */
    public String getBase() {
        return base;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * @return the lastModifiedOn
     */
    public Date getLastModifiedOn() {
        return lastModifiedOn;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param base the base to set
     */
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * @param lastModifiedOn the lastModifiedOn to set
     */
    public void setLastModifiedOn(Date lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    
}
