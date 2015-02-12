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

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.artificer.common.query.xpath.ast.AndExpr;
import org.artificer.common.query.xpath.ast.FunctionCall;
import org.artificer.common.query.xpath.ast.PrimaryExpr;
import org.artificer.common.query.xpath.ast.SubartifactSet;
import org.artificer.common.query.xpath.ast.Argument;
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
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.AndExpr)
	 */
	@Override
	public void visit(AndExpr node) {
		node.getLeft().accept(this);
		if (node.getRight() != null) {
			this.builder.append(" and "); //$NON-NLS-1$
			node.getRight().accept(this);
		}
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.Argument)
	 */
	@Override
	public void visit(Argument node) {
		if (node.getPrimaryExpr() != null)
			visit(node.getPrimaryExpr());
		else if (node.getExpr() != null)
			node.getExpr().accept(this);
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.ArtifactSet)
	 */
	@Override
	public void visit(ArtifactSet node) {
		node.getLocationPath().accept(this);
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.EqualityExpr)
	 */
	@Override
	public void visit(EqualityExpr node) {
		if (node.getSubartifactSet() != null) {
			node.getSubartifactSet().accept(this);
		} else if (node.getExpr() != null) {
			this.builder.append("("); //$NON-NLS-1$
			node.getExpr().accept(this);
			this.builder.append(")"); //$NON-NLS-1$
		} else {
			node.getLeft().accept(this);
			if (node.getOperator() != null) {
				this.builder.append(' ');
				this.builder.append(node.getOperator().symbol());
				this.builder.append(' ');
				node.getRight().accept(this);
			}
		}
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.Expr)
	 */
	@Override
	public void visit(Expr node) {
		node.getAndExpr().accept(this);
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.ForwardPropertyStep)
	 */
	@Override
	public void visit(ForwardPropertyStep node) {
		if (node.getPropertyQName() != null) {
			this.builder.append('@');
			appendQName(node.getPropertyQName());
		}
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.FunctionCall)
	 */
	@Override
	public void visit(FunctionCall node) {
		QName functionName = node.getFunctionName();
		if (functionName.getPrefix() != null && functionName.getPrefix().trim().length() > 0 && !"s-ramp".equals(functionName.getPrefix())) { //$NON-NLS-1$
			this.builder.append(functionName.getPrefix());
			this.builder.append(':');
		}
		this.builder.append(functionName.getLocalPart());
		this.builder.append('(');
		Iterator<Argument> iterator = node.getArguments().iterator();
		while (iterator.hasNext()) {
			iterator.next().accept(this);
			if (iterator.hasNext())
				this.builder.append(", "); //$NON-NLS-1$
		}
		this.builder.append(')');
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.LocationPath)
	 */
	@Override
	public void visit(LocationPath node) {
		this.builder.append("/s-ramp"); //$NON-NLS-1$
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
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.OrExpr)
	 */
	@Override
	public void visit(OrExpr node) {
		node.getLeft().accept(this);
		if (node.getRight() != null) {
			this.builder.append(" or "); //$NON-NLS-1$
			node.getRight().accept(this);
		}
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.Predicate)
	 */
	@Override
	public void visit(Predicate node) {
		node.getExpr().accept(this);
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.PrimaryExpr)
	 */
	@Override
	public void visit(PrimaryExpr node) {
		if (node.getLiteral() != null) {
			this.builder.append("'"); //$NON-NLS-1$
			this.builder.append(node.getLiteral().replace("'", "''")); //$NON-NLS-1$ //$NON-NLS-2$
			this.builder.append("'"); //$NON-NLS-1$
		} else if (node.getNumber() != null) {
			this.builder.append(node.getNumber().toString());
		} else if (node.getPropertyQName() != null) {
			this.builder.append("$"); //$NON-NLS-1$
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
			this.builder.append(":"); //$NON-NLS-1$
		}
		this.builder.append(qname.getLocalPart());
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.Query)
	 */
	@Override
	public void visit(Query node) {
		node.getArtifactSet().accept(this);
		if (node.getPredicate() != null) {
			this.builder.append('[');
			node.getPredicate().accept(this);
			this.builder.append(']');
		}
		if (node.getSubartifactSet() != null) {
			this.builder.append('/');
			node.getSubartifactSet().accept(this);
		}
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.RelationshipPath)
	 */
	@Override
	public void visit(RelationshipPath node) {
		this.builder.append(node.getRelationshipType());
	}

	/**
	 * @see XPathVisitor#visit(org.artificer.common.query.xpath.ast.SubartifactSet)
	 */
	@Override
	public void visit(SubartifactSet node) {
		if (node.getFunctionCall() != null) {
			node.getFunctionCall().accept(this);
		} else {
			node.getRelationshipPath().accept(this);
			if (node.getPredicate() != null) {
				this.builder.append('[');
				node.getPredicate().accept(this);
				this.builder.append(']');
			}
			if (node.getSubartifactSet() != null) {
				this.builder.append('/');
				node.getSubartifactSet().accept(this);
			}
		}
	}
}
