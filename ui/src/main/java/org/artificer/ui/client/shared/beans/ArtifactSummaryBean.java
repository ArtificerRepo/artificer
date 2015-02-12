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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * A simple data bean for returning summary information for an artifact. This is typically returned in search
 * results, for example.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
@Bindable
public class ArtifactSummaryBean implements Serializable {

    private static final long serialVersionUID = 1038258536497848012L;
    
    private String model;
    private String type;
    private String rawType;
    private String uuid;
    private String name;
    private String description;
    private String createdBy;
    private Date createdOn;
    private Date updatedOn;
    private Map<String, String> properties = new HashMap<String, String>();
    private boolean derived;

    /**
     * Constructor.
     */
    public ArtifactSummaryBean() {
    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
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
     * @return the updatedOn
     */
    public Date getUpdatedOn() {
        return updatedOn;
    }

    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * @return the derived
     */
    public boolean isDerived() {
        return derived;
    }

    /**
     * @param model the model to set
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
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
     * @param updatedOn the updatedOn to set
     */
    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * @param derived the derived to set
     */
    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    /**
     * Gets a property by name.
     * @param propName
     */
    public String getProperty(String propName) {
        return this.properties.get(propName);
    }

    /**
     * Sets a property.
     * @param propName
     * @param propVal
     */
    public void setProperty(String propName, String propVal) {
        this.properties.put(propName, propVal);
    }

    /**
     * @return the names of all the custom properties
     */
    public Set<String> getPropertyNames() {
        return this.properties.keySet();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ArtifactSummaryBean other = (ArtifactSummaryBean) obj;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }

    /**
     * @return the rawType
     */
    public String getRawType() {
        return rawType;
    }

    /**
     * @param rawType the rawType to set
     */
    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

}
