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

import javax.xml.namespace.QName;

import org.overlord.sramp.query.xpath.visitors.XPathVisitor;

/**
 * Models a forward property step.
 * 
 * <pre>
 *   ForwardPropertyStep ::= subartifact-set
 *                         | subartifact-set '/' '@' PropertyQName
 *                         | '@' PropertyQName
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public class ForwardPropertyStep extends AbstractXPathNode {
	
	private SubartifactSet subartifactSet;
	private QName propertyQName;

	/**
	 * Default constructor.
	 */
	public ForwardPropertyStep() {
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
	 * @return the propertyQName
	 */
	public QName getPropertyQName() {
		return propertyQName;
	}

	/**
	 * @param propertyQName the propertyQName to set
	 */
	public void setPropertyQName(QName propertyQName) {
		this.propertyQName = propertyQName;
	}
	
	/**
	 * @see org.overlord.sramp.query.xpath.ast.AbstractXPathNode#accept(org.overlord.sramp.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}
	
}
