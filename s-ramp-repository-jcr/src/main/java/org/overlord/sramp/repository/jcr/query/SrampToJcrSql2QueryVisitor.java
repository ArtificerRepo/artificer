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
package org.overlord.sramp.repository.jcr.query;

import org.overlord.sramp.query.xpath.ast.AndExpr;
import org.overlord.sramp.query.xpath.ast.Argument;
import org.overlord.sramp.query.xpath.ast.ArtifactSet;
import org.overlord.sramp.query.xpath.ast.EqualityExpr;
import org.overlord.sramp.query.xpath.ast.Expr;
import org.overlord.sramp.query.xpath.ast.ForwardPropertyStep;
import org.overlord.sramp.query.xpath.ast.FunctionCall;
import org.overlord.sramp.query.xpath.ast.LocationPath;
import org.overlord.sramp.query.xpath.ast.OrExpr;
import org.overlord.sramp.query.xpath.ast.Predicate;
import org.overlord.sramp.query.xpath.ast.PrimaryExpr;
import org.overlord.sramp.query.xpath.ast.Query;
import org.overlord.sramp.query.xpath.ast.RelationshipPath;
import org.overlord.sramp.query.xpath.ast.SubartifactSet;
import org.overlord.sramp.query.xpath.visitors.XPathVisitor;

/**
 * Visitor used to produce a JCR SQL2 query from an S-RAMP xpath query.
 * 
 * @author eric.wittmann@redhat.com
 */
public class SrampToJcrSql2QueryVisitor implements XPathVisitor {

	private StringBuilder builder = new StringBuilder();
	private int conditionCount = 0;

	/**
	 * Default constructor.
	 */
	public SrampToJcrSql2QueryVisitor() {
	}

	/**
	 * Resets the visitor back to an empty state.
	 */
	public void reset() {
		this.builder = new StringBuilder();
		this.conditionCount = 0;
	}

	/**
	 * Returns the sql-2 query created by this visitor.
	 */
	public String getSql2Query() {
		return this.builder.toString();
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.AndExpr)
	 */
	@Override
	public void visit(AndExpr node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Argument)
	 */
	@Override
	public void visit(Argument node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.ArtifactSet)
	 */
	@Override
	public void visit(ArtifactSet node) {
		node.getLocationPath().accept(this);
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.EqualityExpr)
	 */
	@Override
	public void visit(EqualityExpr node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Expr)
	 */
	@Override
	public void visit(Expr node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.ForwardPropertyStep)
	 */
	@Override
	public void visit(ForwardPropertyStep node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.FunctionCall)
	 */
	@Override
	public void visit(FunctionCall node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.LocationPath)
	 */
	@Override
	public void visit(LocationPath node) {
		if (node.getArtifactType() != null) {
			appendCondition("[sramp:artifactType] = '" + node.getArtifactType().replace("'", "''") + "'");
		} else if (node.getArtifactModel() != null) {
			appendCondition("[sramp:artifactModel] = '" + node.getArtifactModel().replace("'", "''") + "'");
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.OrExpr)
	 */
	@Override
	public void visit(OrExpr node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Predicate)
	 */
	@Override
	public void visit(Predicate node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.PrimaryExpr)
	 */
	@Override
	public void visit(PrimaryExpr node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Query)
	 */
	@Override
	public void visit(Query node) {
		this.builder.append("SELECT * FROM [overlord:artifact]");
		node.getArtifactSet().accept(this);
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.RelationshipPath)
	 */
	@Override
	public void visit(RelationshipPath node) {
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.SubartifactSet)
	 */
	@Override
	public void visit(SubartifactSet node) {
	}

	/**
	 * Appends a single WHERE condition to the query.
	 * @param condition
	 */
	private void appendCondition(String condition) {
		if (conditionCount == 0) {
			this.builder.append(" WHERE ");
		} else {
			this.builder.append(" AND ");
		}
		this.builder.append(condition);
		conditionCount++;
	}

}
