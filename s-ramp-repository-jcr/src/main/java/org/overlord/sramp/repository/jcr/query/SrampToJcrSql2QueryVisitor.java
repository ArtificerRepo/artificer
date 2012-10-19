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

	private StringBuilder fromBuilder = new StringBuilder();
	private StringBuilder whereBuilder = new StringBuilder();
	private String predicateContext = "artifact";
	private int relationshipJoinCounter = 1;

	/**
	 * Default constructor.
	 */
	public SrampToJcrSql2QueryVisitor() {
	}

	/**
	 * Returns the sql-2 query created by this visitor.
	 */
	public String getSql2Query() {
		String query = "SELECT artifact.* FROM " + fromBuilder.toString() + " WHERE " + whereBuilder.toString();
		return query;
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.Query)
	 */
	@Override
	public void visit(Query node) {
		this.fromBuilder.append("[sramp:baseArtifactType] AS artifact");
		node.getArtifactSet().accept(this);
		if (node.getPredicate() != null) {
			this.whereBuilder.append(" AND (");
			node.getPredicate().accept(this);
			this.whereBuilder.append(")");
		}
		if (node.getSubartifactSet() != null)
			throw new RuntimeException("Top level sub-artifact-sets not supported.");
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.LocationPath)
	 */
	@Override
	public void visit(LocationPath node) {
		if (node.getArtifactType() != null) {
			// If this is explicitely *or* implicitely a user defined type search...
			if ("user".equals(node.getArtifactModel()) || !ArtifactTypeEnum.hasEnum(node.getArtifactType())) {
				this.whereBuilder.append("artifact.[sramp:artifactType] = '" + ArtifactTypeEnum.UserDefinedArtifactType + "'");
				this.whereBuilder.append(" AND ");
				this.whereBuilder.append("artifact.[sramp:userType] = '" + node.getArtifactType().replace("'", "''") + "'");
			} else {
				this.whereBuilder.append("artifact.[sramp:artifactType] = '" + node.getArtifactType().replace("'", "''") + "'");
			}
		} else if (node.getArtifactModel() != null) {
			this.whereBuilder.append("artifact.[sramp:artifactModel] = '" + node.getArtifactModel().replace("'", "''") + "'");
		} else {
			this.whereBuilder.append("artifact.[sramp:artifactModel] LIKE '%'");
		}
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
			this.whereBuilder.append(" AND ");
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
		if (node.getSubartifactSet() != null) {
			node.getSubartifactSet().accept(this);
		} else if (node.getExpr() != null) {
			this.whereBuilder.append(" ( ");
			node.getExpr().accept(this);
			this.whereBuilder.append(" ) ");
		} else if (node.getOperator() == null) {
			node.getLeft().accept(this);
			this.whereBuilder.append(" LIKE '%'");
		} else {
			node.getLeft().accept(this);
			this.whereBuilder.append(" ");
			this.whereBuilder.append(node.getOperator().symbol());
			this.whereBuilder.append(" ");
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
				this.whereBuilder.append(this.predicateContext);
				this.whereBuilder.append(".[");
				this.whereBuilder.append(jcrPropName);
				this.whereBuilder.append("]");
			} else {
				throw new RuntimeException("Properties from namespace '" + property.getNamespaceURI() + "' are not supported.");
			}
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
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.OrExpr)
	 */
	@Override
	public void visit(OrExpr node) {
		if (node.getRight() == null) {
			node.getLeft().accept(this);
		} else {
			node.getLeft().accept(this);
			this.whereBuilder.append(" OR ");
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
			this.whereBuilder.append("'");
			this.whereBuilder.append(node.getLiteral());
			this.whereBuilder.append("'");
		} else if (node.getNumber() != null) {
			this.whereBuilder.append(node.getNumber());
		} else if (node.getPropertyQName() != null) {
			throw new RuntimeException("Property primary expressions not yet supported.");
		}
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.RelationshipPath)
	 */
	@Override
	public void visit(RelationshipPath node) {
		String alias = this.predicateContext;

		fromBuilder.append(" JOIN [sramp:relationship] AS ");
		fromBuilder.append(alias);
		fromBuilder.append(" ON ISCHILDNODE(");
		fromBuilder.append(alias);
		fromBuilder.append(", artifact)");

		whereBuilder.append(alias);
		whereBuilder.append(".[sramp:relationshipType] = '");
		whereBuilder.append(node.getRelationshipType());
		whereBuilder.append("'");
	}

	/**
	 * @see org.overlord.sramp.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.query.xpath.ast.SubartifactSet)
	 */
	@Override
	public void visit(SubartifactSet node) {
		if (node.getFunctionCall() != null) {
			node.getFunctionCall().accept(this);
		} else if (node.getRelationshipPath() != null) {
			String oldCtx = this.predicateContext;

			if (node.getPredicate() != null) {
				whereBuilder.append("("); // open the predicate paren
			}
			String relationshipAlias = "relationship" + relationshipJoinCounter++;
			this.predicateContext = relationshipAlias;
			node.getRelationshipPath().accept(this);
			if (node.getPredicate() != null) {
				this.whereBuilder.append(" AND ");
				this.whereBuilder.append(relationshipAlias);
				this.whereBuilder.append(".[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS target WHERE ");

				this.predicateContext = "target";
				node.getPredicate().accept(this);

				this.whereBuilder.append(")"); // close the sub-query paren
			}
			if (node.getPredicate() != null) {
				whereBuilder.append(")"); // Close the predicate paren
			}

			this.predicateContext = oldCtx;
			if (node.getSubartifactSet() != null) {
				throw new RuntimeException("Multi-level sub-artifact-sets not supported.");
			}
		}
	}

}
