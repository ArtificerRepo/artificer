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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.overlord.sramp.ArtifactTypeEnum;
import org.overlord.sramp.SrampConstants;
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
import org.overlord.sramp.repository.jcr.JCRConstants;

/**
 * Visitor used to produce a JCR SQL2 query from an S-RAMP xpath query.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampToJcrSql2QueryVisitor implements XPathVisitor {

	private StringBuilder builder = new StringBuilder();
	private static Map<QName, String> corePropertyMap = new HashMap<QName, String>();
	static {
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "createdBy"), "jcr:createdBy");
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "version"), "version");
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "uuid"), "sramp:uuid");
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "createdTimestamp"), "jcr:created");
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "lastModifiedTimestamp"), "jcr:lastModified");
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "lastModifiedBy"), "jcr:lastModifiedBy");
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "description"), "sramp:description");
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "name"), "sramp:name");
	}

	/**
	 * Default constructor.
	 */
	public SrampToJcrSql2QueryVisitor() {
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
		if (node.getRight() == null) {
			node.getLeft().accept(this);
		} else {
			node.getLeft().accept(this);
			this.builder.append(" AND ");
			node.getRight().accept(this);
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Argument)
	 */
	@Override
	public void visit(Argument node) {
		throw new RuntimeException("Function arguments not yet supported.");
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
		if (node.getExpr() != null) {
			this.builder.append(" ( ");
			node.getExpr().accept(this);
			this.builder.append(" ) ");
		} else if (node.getOperator() == null) {
			node.getLeft().accept(this);
			this.builder.append(" LIKE '%'");
		} else {
			node.getLeft().accept(this);
			this.builder.append(" ");
			this.builder.append(node.getOperator().symbol());
			this.builder.append(" ");
			node.getRight().accept(this);
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Expr)
	 */
	@Override
	public void visit(Expr node) {
		node.getAndExpr().accept(this);
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.ForwardPropertyStep)
	 */
	@Override
	public void visit(ForwardPropertyStep node) {
		if (node.getPropertyQName() != null) {
			QName property = node.getPropertyQName();
			if (property.getNamespaceURI() == null || "".equals(property.getNamespaceURI()))
				property = new QName(SrampConstants.SRAMP_NS, property.getLocalPart());

			if (property.getNamespaceURI().equals(SrampConstants.SRAMP_NS)) {
				String jcrPropName = null;
				if (corePropertyMap.containsKey(property)) {
					jcrPropName = corePropertyMap.get(property);
				} else {
					jcrPropName = JCRConstants.SRAMP_PROPERTIES + ":" + property.getLocalPart();
				}
				this.builder.append("[");
				this.builder.append(jcrPropName);
				this.builder.append("]");
			} else {
				throw new RuntimeException("Properties from namespace '" + property.getNamespaceURI() + "' are not supported.");
			}
		}
		if (node.getSubartifactSet() != null) {
			throw new RuntimeException("Sub-artifact-set in a forward property step not yet supported.");
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.FunctionCall)
	 */
	@Override
	public void visit(FunctionCall node) {
		throw new RuntimeException("Function calls not yet supported.");
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.LocationPath)
	 */
	@Override
	public void visit(LocationPath node) {
		if (node.getArtifactType() != null) {
			// If this is explicitely *or* implicitely a user defined type search...
			if ("user".equals(node.getArtifactModel()) || !ArtifactTypeEnum.hasEnum(node.getArtifactType())) {
				this.builder.append(" WHERE [sramp:artifactType] = '" + ArtifactTypeEnum.UserDefinedArtifactType + "'");
				this.builder.append(" AND ");
				this.builder.append("[sramp:userType] = '" + node.getArtifactType().replace("'", "''") + "'");
			} else {
				this.builder.append(" WHERE [sramp:artifactType] = '" + node.getArtifactType().replace("'", "''") + "'");
			}
		} else if (node.getArtifactModel() != null) {
			this.builder.append(" WHERE [sramp:artifactModel] = '" + node.getArtifactModel().replace("'", "''") + "'");
		} else {
			this.builder.append(" WHERE [sramp:artifactModel] LIKE '%'");
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.OrExpr)
	 */
	@Override
	public void visit(OrExpr node) {
		if (node.getRight() == null) {
			node.getLeft().accept(this);
		} else {
			node.getLeft().accept(this);
			this.builder.append(" OR ");
			node.getRight().accept(this);
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Predicate)
	 */
	@Override
	public void visit(Predicate node) {
		node.getExpr().accept(this);
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.PrimaryExpr)
	 */
	@Override
	public void visit(PrimaryExpr node) {
		if (node.getLiteral() != null) {
			this.builder.append("'");
			this.builder.append(node.getLiteral());
			this.builder.append("'");
		} else if (node.getNumber() != null) {
			this.builder.append(node.getNumber());
		} else if (node.getPropertyQName() != null) {
			throw new RuntimeException("Property primary expressions not yet supported.");
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Query)
	 */
	@Override
	public void visit(Query node) {
		this.builder.append("SELECT * FROM [sramp:baseArtifactType]");
		node.getArtifactSet().accept(this);
		if (node.getPredicate() != null) {
			this.builder.append(" AND (");
			node.getPredicate().accept(this);
			this.builder.append(")");
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.RelationshipPath)
	 */
	@Override
	public void visit(RelationshipPath node) {
		throw new RuntimeException("Relationship paths not yet supported.");
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.SubartifactSet)
	 */
	@Override
	public void visit(SubartifactSet node) {
		throw new RuntimeException("Sub-artifact-sets not yet supported.");
	}

}
