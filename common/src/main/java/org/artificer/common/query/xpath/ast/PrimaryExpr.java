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

import javax.xml.namespace.QName;

import org.artificer.common.query.xpath.visitors.XPathVisitor;

/**
 * Models a primary expression.
 * 
 * <pre>
 *   PrimaryExpr ::= Literal
 *                 | Number
 *                 | '$' PropertyQName
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public class PrimaryExpr extends AbstractXPathNode {

	private String literal;
	private Number number;
	private QName propertyQName;
	
	/**
	 * Default constructor.
	 */
	public PrimaryExpr() {
	}

	/**
	 * @return the literal
	 */
	public String getLiteral() {
		return literal;
	}

	/**
	 * @param literal the literal to set
	 */
	public void setLiteral(String literal) {
		this.literal = literal;
	}

	/**
	 * @return the number
	 */
	public Number getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(Number number) {
		this.number = number;
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
	 * @see AbstractXPathNode#accept(org.artificer.common.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}
	
}
