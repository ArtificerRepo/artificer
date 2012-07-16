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
package org.overlord.sramp.query.xpath.ast;

import org.overlord.sramp.query.xpath.visitors.XPathVisitor;

/**
 * Models a relationship path.
 *
 * @author eric.wittmann@redhat.com
 */
public class RelationshipPath extends AbstractXPathNode {

	private boolean anyOutgoingRelationship;
	private String relationshipType;

	/**
	 * Default constructor.
	 */
	public RelationshipPath() {
	}

	/**
	 * Constructor.
	 * @param relationshipOrFunction
	 */
	public RelationshipPath(String relationshipType) {
		this();
		if ("outgoing".equals(relationshipType)) {
			setAnyOutgoingRelationship(true);
		} else {
			setRelationshipType(relationshipType);
		}
	}

	/**
	 * @return the anyOutgoingRelationship
	 */
	public boolean isAnyOutgoingRelationship() {
		return anyOutgoingRelationship;
	}

	/**
	 * @param anyOutgoingRelationship the anyOutgoingRelationship to set
	 */
	public void setAnyOutgoingRelationship(boolean anyOutgoingRelationship) {
		this.anyOutgoingRelationship = anyOutgoingRelationship;
	}

	/**
	 * @return the relationshipType
	 */
	public String getRelationshipType() {
		return relationshipType;
	}

	/**
	 * @param relationshipType the relationshipType to set
	 */
	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}
	
	/**
	 * @see org.overlord.sramp.query.xpath.ast.AbstractXPathNode#accept(org.overlord.sramp.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}
	
}
