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
 * Models an equality expression.
 *
 * <pre>
 *   EqualityExpr ::= ForwardPropertyStep
 *                  | ForwardPropertyStep '=' PrimaryExpr
 *                  | ForwardPropertyStep '!=' PrimaryExpr
 *                  | ForwardPropertyStep '<' PrimaryExpr
 *                  | ForwardPropertyStep '>' PrimaryExpr
 *                  | ForwardPropertyStep '<=' PrimaryExpr
 *                  | ForwardPropertyStep '>=' PrimaryExpr
 *                  | '(' Expr ')'
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public class EqualityExpr extends AbstractBinaryExpr<AbstractXPathNode, PrimaryExpr> {

	// Note: the following three are mutually exclusive.
	private SubartifactSet subartifactSet;
	private Operator operator;
	private Expr expr;

	/**
	 * Default constructor.
	 */
	public EqualityExpr() {
	}

	/**
	 * @return the operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(Operator operator) {
		this.operator = operator;
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
	 * @see org.overlord.sramp.common.query.xpath.ast.AbstractXPathNode#accept(org.overlord.sramp.common.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @return the subArtifactSet
	 */
	public SubartifactSet getSubartifactSet() {
		return subartifactSet;
	}

	/**
	 * @param subArtifactSet the subArtifactSet to set
	 */
	public void setSubArtifactSet(SubartifactSet subArtifactSet) {
		this.subartifactSet = subArtifactSet;
	}

	/**
	 * Models the comparison expression's operator.
	 */
	public static enum Operator {
		EQ("="), //$NON-NLS-1$
		NE("!="), //$NON-NLS-1$
		LT("<"), //$NON-NLS-1$
		GT(">"), //$NON-NLS-1$
		LTE("<="), //$NON-NLS-1$
		GTE(">="); //$NON-NLS-1$

		private String symbol;

		/**
		 * Constructor.
		 */
		private Operator(String symbol) {
			this.symbol = symbol;
		}

		/**
		 * Gets the symbol;
		 */
		public String symbol() {
			return this.symbol;
		}

		/**
		 * Looks up the proper {@link Operator} from a symbol.
		 * @param symbol the symbol (e.g. =, !=, >, etc)
		 * @return an {@link Operator} or null if not found
		 */
		public static Operator valueOfSymbol(String symbol) {
			for (Operator operator : values())
				if (operator.symbol().equals(symbol))
					return operator;
			return null;
		}
	}

}
