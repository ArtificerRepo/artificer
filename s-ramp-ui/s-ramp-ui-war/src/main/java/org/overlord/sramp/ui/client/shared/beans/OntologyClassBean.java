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
package org.overlord.sramp.ui.client.shared.beans;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models a single class (classifier) in an S-RAMP ontology.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class OntologyClassBean {
    private String id;
    private String label;
    private String comment;
    private String uri;
    private OntologyClassBean parent;
    private List<OntologyClassBean> children = new ArrayList<OntologyClassBean>();

    /**
     * Constructor.
     */
    public OntologyClassBean() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
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
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the parent
     */
    public OntologyClassBean getParent() {
        return parent;
    }

    /**
     * @return the children
     */
    public List<OntologyClassBean> getChildren() {
        return children;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
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
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(OntologyClassBean parent) {
        this.parent = parent;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<OntologyClassBean> children) {
        this.children = children;
    }

}
