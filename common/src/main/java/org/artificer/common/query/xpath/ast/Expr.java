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
 * An X-Path expression.
 * 
 * <pre>
 *   Expr ::= AndExpr
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public class Expr extends AbstractXPathNode {

	private AndExpr andExpr;
	
	/**
	 * Default constructor.
	 */
	public Expr() {
	}

	/**
	 * @return the andExpr
	 */
	public AndExpr getAndExpr() {
		return andExpr;
	}

	/**
	 * @param andExpr the andExpr to set
	 */
	public void setAndExpr(AndExpr andExpr) {
		this.andExpr = andExpr;
	}
	
	/**
	 * @see AbstractXPathNode#accept(org.artificer.common.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}
	
}
