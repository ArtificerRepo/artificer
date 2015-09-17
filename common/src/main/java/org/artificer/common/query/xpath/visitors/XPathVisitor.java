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
package org.artificer.common.query.xpath.visitors;

import org.artificer.common.query.xpath.ast.AndExpr;
import org.artificer.common.query.xpath.ast.Argument;
import org.artificer.common.query.xpath.ast.FunctionCall;
import org.artificer.common.query.xpath.ast.PrimaryExpr;
import org.artificer.common.query.xpath.ast.SubartifactSet;
import org.artificer.common.query.xpath.ast.ArtifactSet;
import org.artificer.common.query.xpath.ast.EqualityExpr;
import org.artificer.common.query.xpath.ast.Expr;
import org.artificer.common.query.xpath.ast.ForwardPropertyStep;
import org.artificer.common.query.xpath.ast.LocationPath;
import org.artificer.common.query.xpath.ast.OrExpr;
import org.artificer.common.query.xpath.ast.Predicate;
import org.artificer.common.query.xpath.ast.Query;
import org.artificer.common.query.xpath.ast.RelationshipPath;

/**
 * Visitor interface for the S-RAMP query AST.
 *
 * @author eric.wittmann@redhat.com
 */
public interface XPathVisitor {
	
	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(AndExpr node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(Argument node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(ArtifactSet node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(EqualityExpr node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(Expr node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(ForwardPropertyStep node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(FunctionCall node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(LocationPath node);
	
	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(OrExpr node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(Predicate node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(PrimaryExpr node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(Query node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(RelationshipPath node);

	/**
	 * Visits a specific x-path node.
	 * @param node
	 */
	public void visit(SubartifactSet node);
	
}
