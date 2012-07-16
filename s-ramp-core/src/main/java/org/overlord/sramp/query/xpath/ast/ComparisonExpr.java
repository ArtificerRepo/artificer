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
 * Models a comparison expression.
 * 
 * <pre>
 *   ComparisonExpr ::= ForwardPropertyStep
 *                    | ForwardPropertyStep '=' PrimaryExpr
 *                    | ForwardPropertyStep '!=' PrimaryExpr
 *                    | ForwardPropertyStep '<' PrimaryExpr
 *                    | ForwardPropertyStep '>' PrimaryExpr
 *                    | ForwardPropertyStep '<=' PrimaryExpr
 *                    | ForwardPropertyStep '>=' PrimaryExpr
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public class ComparisonExpr extends AbstractBinaryExpr<ForwardPropertyStep, PrimaryExpr>{
	
	private Operator operator;
	
	/**
	 * Default constructor.
	 */
	public ComparisonExpr() {
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
	 * @see org.overlord.sramp.query.xpath.ast.AbstractXPathNode#accept(org.overlord.sramp.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * Models the comparison expression's operator.
	 */
	public static enum Operator {
		EQ, NE, LT, GT, LTE, GTE;
	}
	
}
