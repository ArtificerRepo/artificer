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
package org.overlord.sramp.ui.shared.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Models the full details of an s-ramp artifact.  This is a heavy-weight object and
 * should never be returned as part of a list.  Lists of artifacts should always be
 * made up of {@link ArtifactSummary} objects.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactDetails extends ArtifactSummary implements Serializable {

	private static final long serialVersionUID = 810743629840463833L;
	
	private String updatedBy;
	private List<String> classifiedBy = new ArrayList<String>();

	/**
	 * Constructor.
	 */
	public ArtifactDetails() {
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
	 * @param classification
	 */
	public void addClassifiedBy(String classification) {
		classifiedBy.add(classification);
	}

}
