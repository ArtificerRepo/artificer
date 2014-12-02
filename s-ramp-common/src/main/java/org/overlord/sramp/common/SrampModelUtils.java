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
package org.overlord.sramp.common;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.overlord.commons.codec.AesEncrypter;

import javax.xml.namespace.QName;

/**
 * A collection of utilities for dealing with the s-ramp models.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampModelUtils {

    public static final String ENCRYPT_PREFIX = "${crypt:";
    public static final String ENCRYPT_SUFIX = "}";

	/**
	 * Convenience method to help set a custom s-ramp property on the given artifact.
	 * @param artifact
	 * @param propName
	 * @param propValue
	 */
	public static void setCustomProperty(BaseArtifactType artifact, String propName, String propValue) {
		Property prop = null;
		List<Property> properties = artifact.getProperty();
		for (Property property : properties) {
			if (property.getPropertyName().equals(propName)) {
				prop = property;
				break;
			}
		}
		if (prop == null) {
			prop = new Property();
			prop.setPropertyName(propName);
			properties.add(prop);
		}
		prop.setPropertyValue(propValue);
	}
	/**
	 * Convenience method to help remove a custom s-ramp property on the given artifact.
	 * @param artifact
	 * @param propName
	 */
	public static void unsetCustomProperty(BaseArtifactType artifact, String propName) {
		Iterator<Property> iter = artifact.getProperty().iterator();
		while (iter.hasNext()) {
			Property property = iter.next();
			if (property.getPropertyName().equals(propName)) {
				iter.remove();
				break;
			}
		}
	}

    /**
     * Convenience method to help set a custom s-ramp property on the given
     * artifact.
     *
     * @param artifact
     * @param propName
     * @param propValue
     */
    public static void setCustomEncryptedProperty(BaseArtifactType artifact, String propName, String propValue) {
        StringBuilder builder = new StringBuilder();
        builder.append(ENCRYPT_PREFIX).append(AesEncrypter.encrypt(propValue)).append(ENCRYPT_SUFIX);
        setCustomProperty(artifact, propName, builder.toString());

    }

	/**
	 * Gets the value of one of the s-ramp custom properties.
	 * @param artifact the s-ramp artifact
	 * @param propName the name of the custom property
	 * @return the custom property value or null if not found
	 */
	public static String getCustomProperty(BaseArtifactType artifact, String propName) {
		String rval = null;
		List<Property> properties = artifact.getProperty();
		for (Property prop : properties) {
			if (propName.equals(prop.getPropertyName())) {
				rval = prop.getPropertyValue();
				break;
			}
		}
        if (StringUtils.isNotBlank(rval) && rval.startsWith(ENCRYPT_PREFIX) && rval.endsWith(ENCRYPT_SUFIX)) {
            String encrypted_value = rval.substring(ENCRYPT_PREFIX.length(), rval.length() - ENCRYPT_SUFIX.length());
            return AesEncrypter.decrypt(encrypted_value);
        }
		return rval;
	}

	/**
	 * Adds a new generic {@link Relationship} to the artifact.
	 * @param artifact
	 * @param relationshipType
     * @param targetUUID
     * @param otherAttributes
	 * @return the created {@link Relationship}
	 */
	public static Relationship addGenericRelationship(BaseArtifactType artifact, String relationshipType,
            String targetUUID, Map<QName, String> otherAttributes) {
		Relationship relationship = null;
		for (Relationship r : artifact.getRelationship()) {
			if (r.getRelationshipType().equals(relationshipType)) {
				relationship = r;
				break;
			}
		}
		if (relationship == null) {
			relationship = new Relationship();
	        relationship.setRelationshipType(relationshipType);
			artifact.getRelationship().add(relationship);
		}

		// TODO check for duplicates first?
		if (targetUUID != null) {
			Target target = new Target();
			target.setValue(targetUUID);
			relationship.getRelationshipTarget().add(target);
		}

        relationship.getOtherAttributes().putAll(otherAttributes);

		return relationship;
	}

    /**
     * Adds a new generic {@link Relationship} to the artifact.
     * @param artifact
     * @param relationshipType
     * @param targetUUID
     * @return the created {@link Relationship}
     */
    public static Relationship addGenericRelationship(BaseArtifactType artifact, String relationshipType,
            String targetUUID) {
        return addGenericRelationship(artifact, relationshipType, targetUUID, Collections.EMPTY_MAP);
    }

	/**
	 * Gets the generic relationship from the artifact (by type).
	 * @param artifact the s-ramp artifact
	 * @param relationshipType the relationship type
	 * @return the Relationship or null if not found
	 */
	public static Relationship getGenericRelationship(BaseArtifactType artifact, String relationshipType) {
		for (Relationship relationship : artifact.getRelationship()) {
			if (relationship.getRelationshipType().equals(relationshipType)) {
				return relationship;
			}
		}
		return null;
	}

	/**
	 * Returns true if the artifact is a Document style artifact (has content).
	 * @param artifact
	 */
	public static boolean isDocumentArtifact(BaseArtifactType artifact) {
	    return artifact instanceof DocumentArtifactType;
	}

	/**
	 * Returns true if the artifact has text content.
	 * @param artifact
	 */
	public static boolean isTextDocumentArtifact(DocumentArtifactType artifact) {
	    String ct = artifact.getContentType();
	    if (ct == null)
	        return false;
	    ct = ct.toLowerCase();
	    return ct.contains("text/") || ct.contains("application/xml"); //$NON-NLS-1$ //$NON-NLS-2$
	}

    /**
     * Gets all properties with names that begin with the given prefix.  This is useful
     * if the artifact has a number of custom properties that all start with a common
     * domain specific prefix.
     *
     * @param artifact the s-ramp artifact
     * @param prefix the prefix of the properties searched
     * @return the map of custom properties that start by the prefix
     */
    public static Map<String, String> getCustomPropertiesByPrefix(BaseArtifactType artifact, String prefix) {
        Map<String, String> result = new HashMap<String, String>();
        List<Property> properties = artifact.getProperty();
        for (Property prop : properties) {
            if (prop.getPropertyName().startsWith(prefix)) {
                result.put(prop.getPropertyName(), prop.getPropertyValue());
            }
        }
        return result;
    }
}
