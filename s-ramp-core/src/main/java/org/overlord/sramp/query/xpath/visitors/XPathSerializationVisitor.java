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
package org.overlord.sramp.query.xpath.visitors;

import java.util.Iterator;

import javax.xml.namespace.QName;

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

/**
 * Visitor used to serialize a query to a string.
 * 
 * @author eric.wittmann@redhat.com
 */
public class XPathSerializationVisitor implements XPathVisitor {

	private StringBuilder builder = new StringBuilder();

	/**
	 * Default constructor.
	 */
	public XPathSerializationVisitor() {
	}

	/**
	 * Resets the visitor back to an empty state.
	 */
	public void reset() {
		this.builder = new StringBuilder();
	}

	/**
	 * Returns the x-path created by this visitor.
	 */
	public String getXPath() {
		return this.builder.toString();
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.AndExpr)
	 */
	@Override
	public void visit(AndExpr node) {
		visit(node.getLeft());
		if (node.getRight() != null) {
			this.builder.append(" and ");
			visit(node.getRight());
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Argument)
	 */
	@Override
	public void visit(Argument node) {
		visit(node.getExpr());
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.ArtifactSet)
	 */
	@Override
	public void visit(ArtifactSet node) {
		visit(node.getLocationPath());
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.EqualityExpr)
	 */
	@Override
	public void visit(EqualityExpr node) {
		if (node.getExpr() != null) {
			this.builder.append("(");
			visit(node.getExpr());
			this.builder.append(")");
		} else {
			visit(node.getLeft());
			if (node.getOperator() != null) {
				this.builder.append(' ');
				this.builder.append(node.getOperator().symbol());
				this.builder.append(' ');
				visit(node.getRight());
			}
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Expr)
	 */
	@Override
	public void visit(Expr node) {
		visit(node.getAndExpr());
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.ForwardPropertyStep)
	 */
	@Override
	public void visit(ForwardPropertyStep node) {
		if (node.getSubartifactSet() != null) {
			visit(node.getSubartifactSet());
			if (node.getPropertyQName() != null)
				this.builder.append('/');
		}
		if (node.getPropertyQName() != null) {
			this.builder.append('@');
			appendQName(node.getPropertyQName());
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.FunctionCall)
	 */
	@Override
	public void visit(FunctionCall node) {
		QName functionName = node.getFunctionName();
		if (functionName.getPrefix() != null && functionName.getPrefix().trim().length() > 0 && !"s-ramp".equals(functionName.getPrefix())) {
			this.builder.append(functionName.getPrefix());
			this.builder.append(':');
		}
		this.builder.append(functionName.getLocalPart());
		this.builder.append('(');
		Iterator<Argument> iterator = node.getArguments().iterator();
		while (iterator.hasNext()) {
			visit(iterator.next());
			if (iterator.hasNext())
				this.builder.append(", ");
		}
		this.builder.append(')');
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.LocationPath)
	 */
	@Override
	public void visit(LocationPath node) {
		this.builder.append("/s-ramp");
		if (node.getArtifactModel() != null) {
			this.builder.append('/');
			this.builder.append(node.getArtifactModel());
			if (node.getArtifactType() != null) {
				this.builder.append('/');
				this.builder.append(node.getArtifactType());
			}
		}
		
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.OrExpr)
	 */
	@Override
	public void visit(OrExpr node) {
		visit(node.getLeft());
		if (node.getRight() != null) {
			this.builder.append(" or ");
			visit(node.getRight());
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Predicate)
	 */
	@Override
	public void visit(Predicate node) {
		visit(node.getExpr());
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.PrimaryExpr)
	 */
	@Override
	public void visit(PrimaryExpr node) {
		if (node.getLiteral() != null) {
			this.builder.append("'");
			this.builder.append(node.getLiteral().replace("'", "\\'"));
			this.builder.append("'");
		} else if (node.getNumber() != null) {
			this.builder.append(node.getNumber().toString());
		} else if (node.getPropertyQName() != null) {
			this.builder.append("$");
			appendQName(node.getPropertyQName());
		}
	}

	/**
	 * Appends a {@link QName}.
	 * @param qname a {@link QName}
	 */
	private void appendQName(QName qname) {
		if (qname.getPrefix() != null && qname.getPrefix().trim().length() > 0) {
			this.builder.append(qname.getPrefix());
			this.builder.append(":");
		}
		this.builder.append(qname.getLocalPart());
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Query)
	 */
	@Override
	public void visit(Query node) {
		visit(node.getArtifactSet());
		if (node.getPredicate() != null) {
			this.builder.append('[');
			visit(node.getPredicate());
			this.builder.append(']');
		}
		if (node.getSubartifactSet() != null) {
			this.builder.append('/');
			visit(node.getSubartifactSet());
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.RelationshipPath)
	 */
	@Override
	public void visit(RelationshipPath node) {
		if (node.isAnyOutgoingRelationship())
			this.builder.append("outgoing");
		else
			this.builder.append(node.getRelationshipType());
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.SubartifactSet)
	 */
	@Override
	public void visit(SubartifactSet node) {
		if (node.getFunctionCall() != null) {
			visit(node.getFunctionCall());
		} else {
			visit(node.getRelationshipPath());
			if (node.getPredicate() != null) {
				this.builder.append('[');
				visit(node.getPredicate());
				this.builder.append(']');
			}
			if (node.getSubartifactSet() != null) {
				this.builder.append('/');
				visit(node.getSubartifactSet());
			}
		}
	}
}
