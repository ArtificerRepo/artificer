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
 * Models an argument (to a function call).
 * 
 * <pre>
 *   Argument ::= PrimaryExpr
 *              | Expr
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public class Argument extends AbstractXPathNode {

	private PrimaryExpr primaryExpr;
	private Expr expr;
	
	/**
	 * Default constructor.
	 */
	public Argument() {
	}

	/**
	 * @return the expr
	 */
	public Expr getExpr() {
		return expr;
	}

	/**
	 * @param expr the expr to set
	 */
	public void setExpr(Expr expr) {
		this.expr = expr;
	}

	/**
	 * @return the primaryExpr
	 */
	public PrimaryExpr getPrimaryExpr() {
		return primaryExpr;
	}

	/**
	 * @param primaryExpr the primaryExpr to set
	 */
	public void setPrimaryExpr(PrimaryExpr primaryExpr) {
		this.primaryExpr = primaryExpr;
	}
	
	/**
	 * @see org.overlord.sramp.common.query.xpath.ast.AbstractXPathNode#accept(org.overlord.sramp.common.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}

}
