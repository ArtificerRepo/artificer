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
package org.artificer.common.query.xpath.ast;

import org.artificer.common.query.xpath.visitors.XPathVisitor;


/**
 * The top level S-RAMP Query.  This is what you get when you successfully parse a valid
 * S-RAMP Query.
 *
 * @author eric.wittmann@redhat.com
 */
public class Query extends AbstractXPathNode {

	private ArtifactSet artifactSet;
	private Predicate predicate;
	private SubartifactSet subartifactSet;
	
	/**
	 * Constructor.
	 */
	public Query() {
	}

	/**
	 * @return the artifactSet
	 */
	public ArtifactSet getArtifactSet() {
		return artifactSet;
	}

	/**
	 * @param artifactSet the artifactSet to set
	 */
	public void setArtifactSet(ArtifactSet artifactSet) {
		this.artifactSet = artifactSet;
	}

	/**
	 * @return the predicate
	 */
	public Predicate getPredicate() {
		return predicate;
	}

	/**
	 * @param predicate the predicate to set
	 */
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}

	/**
	 * @return the subartifactSet
	 */
	public SubartifactSet getSubartifactSet() {
		return subartifactSet;
	}

	/**
	 * @param subartifactSet the subartifactSet to set
	 */
	public void setSubartifactSet(SubartifactSet subartifactSet) {
		this.subartifactSet = subartifactSet;
	}
	
	/**
	 * @see AbstractXPathNode#accept(org.artificer.common.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}
	
}
