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
 * Models an equality expression.
 * 
 * <pre>
 *   EqualityExpr ::= subartifact-set
 *                  | ComparisonExpr
 *                  | '(' Expr ')'
 * </pre>
 * 
 * Note that the grammar does not define a ComparisonExpr, but for convenience we do.
 *
 * @author eric.wittmann@redhat.com
 */
public class EqualityExpr extends AbstractXPathNode {
	
	// Note: the following three are mutually exclusive.
	
	private SubartifactSet subartifactSet;
	private ComparisonExpr comparisonExpr;
	private Expr expr;

	/**
	 * Default constructor.
	 */
	public EqualityExpr() {
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
	 * @return the comparisonExpr
	 */
	public ComparisonExpr getComparisonExpr() {
		return comparisonExpr;
	}

	/**
	 * @param comparisonExpr the comparisonExpr to set
	 */
	public void setComparisonExpr(ComparisonExpr comparisonExpr) {
		this.comparisonExpr = comparisonExpr;
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
	 * @see org.overlord.sramp.query.xpath.ast.AbstractXPathNode#accept(org.overlord.sramp.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}

}
