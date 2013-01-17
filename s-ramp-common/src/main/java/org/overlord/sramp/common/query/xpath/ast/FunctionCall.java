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

import java.util.List;

import javax.xml.namespace.QName;

import org.overlord.sramp.common.query.xpath.visitors.XPathVisitor;

/**
 * Models a function call
 * 
 * <pre>
 *   FunctionCall ::= FunctionName '(' ( Argument ( ',' Argument )* )? ')'
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public class FunctionCall extends AbstractXPathNode {

	private QName functionName;
	private List<Argument> arguments;
	
	/**
	 * Default constructor.
	 */
	public FunctionCall() {
	}

	/**
	 * @return the functionName
	 */
	public QName getFunctionName() {
		return functionName;
	}

	/**
	 * @param functionName the functionName to set
	 */
	public void setFunctionName(QName functionName) {
		this.functionName = functionName;
	}

	/**
	 * @return the arguments
	 */
	public List<Argument> getArguments() {
		return arguments;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(List<Argument> arguments) {
		this.arguments = arguments;
	}
	
	/**
	 * @see org.overlord.sramp.common.query.xpath.ast.AbstractXPathNode#accept(org.overlord.sramp.common.query.xpath.visitors.XPathVisitor)
	 */
	@Override
	public void accept(XPathVisitor visitor) {
		visitor.visit(this);
	}
	
}
