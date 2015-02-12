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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models an S-RAMP Ontology, including the entire tree of classes.
 * @author eric.wittmann@redhat.com
 */
@Portable
public class OntologyBean extends OntologySummaryBean {

    private static final long serialVersionUID = 9164017316249330169L;
    
    private String lastModifiedBy;
    private List<OntologyClassBean> rootClasses = new ArrayList<OntologyClassBean>();

    /**
     * Constructor.
     */
    public OntologyBean() {
    }

    /**
     * @return the lastModifiedBy
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * @param lastModifiedBy the lastModifiedBy to set
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * @return the rootClasses
     */
    public List<OntologyClassBean> getRootClasses() {
        return rootClasses;
    }

    /**
     * Creates a class (and indexes it).
     * @param id
     */
    public OntologyClassBean createClass(String id) {
        OntologyClassBean c = new OntologyClassBean();
        c.setId(id);
        String uri = getBase() + "#" + id; //$NON-NLS-1$
        c.setUri(uri);
        return c;
    }
    
    /**
     * Make a copy of the ontology.
     */
    public OntologyBean copy() {
        OntologyBean c = new OntologyBean();
        c.setBase(this.getBase());
        c.setComment(this.getComment());
        c.setCreatedBy(this.getCreatedBy());
        c.setCreatedOn(this.getCreatedOn());
        c.setId(this.getId());
        c.setLabel(this.getLabel());
        c.setLastModifiedBy(this.getLastModifiedBy());
        c.setLastModifiedOn(this.getLastModifiedOn());
        c.setUuid(this.getUuid());
        for (OntologyClassBean bean : getRootClasses()) {
            c.getRootClasses().add(bean.copy());
        }
        
        return c;
    }

}
