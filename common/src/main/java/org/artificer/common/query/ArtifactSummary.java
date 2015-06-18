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
package org.artificer.common.query;

import org.artificer.common.ArtifactType;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Models a summary of a single artifact from a query result.
 *
 * @author Brett Meyer
 */
public class ArtifactSummary implements Serializable {

    private String uuid;

    private String name;

    private String description;

	private String model;

    private String type;

    private boolean derived;

    private Calendar createdTimestamp;

    private String createdBy;

    private Calendar lastModifiedTimestamp;

    private Map<Serializable, Serializable> extensionAttributes = new HashMap<>();

    public ArtifactSummary() {

    }

    public ArtifactSummary(String uuid, String name, String description, String model, String type, boolean derived) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.model = model;
        this.type = type;
        this.derived = derived;
    }

    public ArtifactSummary(String uuid, String name, String description, String model, String type, boolean derived,
            Calendar createdTimestamp, String createdBy, Calendar lastModifiedTimestamp) {
        this(uuid, name, description, model, type, derived);
        this.createdTimestamp = createdTimestamp;
        this.createdBy = createdBy;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    public Calendar getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Calendar createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Calendar getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(Calendar lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public Map<Serializable, Serializable> getExtensionAttributes() {
        return extensionAttributes;
    }

    public Serializable getExtensionAttribute(Serializable key) {
        return extensionAttributes.get(key);
    }

    public void setExtensionAttributes(Map<Serializable, Serializable> extensionAttributes) {
        this.extensionAttributes = extensionAttributes;
    }

    public ArtifactType getArtifactType() {
        ArtifactType artifactType = ArtifactType.valueOf(model, type, false);
        artifactType.setExtendedDerivedType(derived);
        return artifactType;
    }
}
