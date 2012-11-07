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

import java.util.List;

import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.Relationship;
import org.s_ramp.xmlns._2010.s_ramp.Target;

/**
 * A collection of utilities for dealing with the s-ramp models.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampModelUtils {

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
		if (propValue == null) {
			properties.remove(prop);
		} else {
			prop.setPropertyValue(propValue);
		}
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
		return rval;
	}

	/**
	 * Adds a new generic {@link Relationship} to the artifact.
	 * @param artifact
	 * @param relationshipType
	 * @param targetUUID
	 * @return the created {@link Relationship}
	 */
	public static Relationship addGenericRelationship(BaseArtifactType artifact, String relationshipType, String targetUUID) {
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

		return relationship;
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

}
