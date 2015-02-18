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
package org.artificer.client.query;

import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Models a summary of a single S-RAMP artifact from a Feed (result of an
 * S-RAMP query).
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactSummary {

	private final Entry entry;
	private BaseArtifactType artifact;

	/**
	 * Constructor.
	 * @param entry
	 */
	public ArtifactSummary(Entry entry) {
		this.entry = entry;
	}

	/**
	 * @return the artifact type
	 */
	public ArtifactType getType() {
		return ArtificerAtomUtils.getArtifactType(entry);
	}

	/**
	 * @return the artifact's uuid
	 */
	public String getUuid() {
		return entry.getId().toString().replace("urn:uuid:", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return the artifact's last modified timestamp
	 */
	public Date getLastModifiedTimestamp() {
		return entry.getUpdated();
	}

	/**
	 * @return the artifact's name
	 */
	public String getName() {
		return entry.getTitle();
	}

	/**
	 * @return the artifact's created timestamp
	 */
	public Date getCreatedTimestamp() {
		return entry.getPublished();
	}

	/**
	 * @return the artifact's created by
	 */
	public String getCreatedBy() {
		return entry.getAuthors().get(0).getName();
	}

	/**
	 * @return the artifact's description
	 */
	public String getDescription() {
		return entry.getSummary();
	}

	/**
	 * @return true if the artifact is an extended type
	 */
	public boolean isExtendedType() {
	    return getType().isExtendedType();
	}

	/**
	 * @return true if the artifact is a derived type
	 */
	public boolean isDerived() {
        return getType().isDerived();
	}

	/**
	 * Returns the value of a property included in the query result set.  Note that
	 * the property must have been requested via the "propertyName" parameter of the
	 * issueing query.
	 * @param propertyName the name of the property
	 * @return the property value or null if not present
	 */
    public String getCustomPropertyValue(String propertyName) {
        if (artifact == null) {
            artifact = ArtificerAtomUtils.unwrapSrampArtifact(entry);
        }
        if (artifact != null) {
            return ArtificerModelUtils.getCustomProperty(artifact, propertyName);
        } else {
            return null;
        }
    }

	/**
	 * Returns all custom properties included in the query result set that begin
	 * with the given prefix.  Note that only properties which have been requested
	 * via the "propertyName" parameter of the issueing query will be returned.
	 * @param prefix
	 */
    public Map<String, String> getCustomPropertiesByPrefix(String prefix) {
        Map<String, String> result = new HashMap<String, String>();
        if (artifact == null) {
            artifact = ArtificerAtomUtils.unwrapSrampArtifact(entry);
        }
        if (artifact != null) {
            result = ArtificerModelUtils.getCustomPropertiesByPrefix(artifact, prefix);
        }
        return result;
    }

    public Object getExtensionAttribute(Object name) {
        return entry.getExtensionAttributes().get(name);
    }
}
