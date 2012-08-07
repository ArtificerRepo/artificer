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
package org.overlord.sramp.ui.client.places;

import java.util.Map;

import com.google.gwt.place.shared.PlaceTokenizer;

/**
 * Place:  /dashboard/browse/artifact
 * 
 * This place represents the detail page for a single artifact.  This place
 * requires the UUID of an artifact to work properly.
 * 
 * @author eric.wittmann@redhat.com
 */
public class ArtifactPlace extends AbstractPlace {
	
	private String uuid;

	/**
	 * Constructor.
	 * @param uuid
	 */
	public ArtifactPlace(String uuid) {
		this.setUuid(uuid);
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ArtifactPlace))
			return false;
		ArtifactPlace other = (ArtifactPlace) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}



	/*
	 * Tokenizer.
	 */
	public static class Tokenizer implements PlaceTokenizer<ArtifactPlace> {
		/**
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
		 */
		@Override
		public String getToken(ArtifactPlace place) {
			return PlaceUtils.createPlaceToken("uuid", place.getUuid());
		}

		/**
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
		 */
		@Override
		public ArtifactPlace getPlace(String token) {
			Map<String, String> params = PlaceUtils.parsePlaceToken(token);
			String uuid = params.get("uuid");
			return new ArtifactPlace(uuid);
		}
	}
}