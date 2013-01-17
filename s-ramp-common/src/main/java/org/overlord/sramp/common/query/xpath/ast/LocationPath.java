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
package org.overlord.sramp.common.query.xpath.ast;

import org.overlord.sramp.common.query.xpath.visitors.XPathVisitor;

/**
 * Models the artifact-set portion of an S-RAMP Query.  Note that in the grammar found in the
 * S-RAMP specification, the artifact-set is exactly a location-path.  I chose to collapse the 
 * two into a single model.
 *
 * @author eric.wittmann@redhat.com
 */
public class LocationPath extends AbstractXPathNode {

	private String artifactModel;
	private String artifactType;
	
	/**
	 * Default constructor.
	 */
	public LocationPath() {
	}

	/**
	 * @return the artifactModel
	 */
	public String getArtifactModel() {
		return artifactModel;
	}

	/**
	 * @param artifactModel the artifactModel to set
	 */
	public void setArtifactModel(String artifactModel) {
		this.artifactModel = artifactModel;
	}

	/**
	 * @return the artifactType
	 */
	public String getArtifactType() {
		return artifactType;
	}

	/**
	 * @param artifactType the artifactType to set
	 */
	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}
	
	/**
	 * @see org.overlord.sramp.common.query.xpath.ast.AbstractXPathNode#accept(org.overlord.sramp.common.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}
	
}
