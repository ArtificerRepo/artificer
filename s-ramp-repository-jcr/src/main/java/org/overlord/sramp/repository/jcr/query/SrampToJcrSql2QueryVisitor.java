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

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.query.xpath.ast.AndExpr;
import org.overlord.sramp.common.query.xpath.ast.Argument;
import org.overlord.sramp.common.query.xpath.ast.ArtifactSet;
import org.overlord.sramp.common.query.xpath.ast.EqualityExpr;
import org.overlord.sramp.common.query.xpath.ast.Expr;
import org.overlord.sramp.common.query.xpath.ast.ForwardPropertyStep;
import org.overlord.sramp.common.query.xpath.ast.FunctionCall;
import org.overlord.sramp.common.query.xpath.ast.LocationPath;
import org.overlord.sramp.common.query.xpath.ast.OrExpr;
import org.overlord.sramp.common.query.xpath.ast.Predicate;
import org.overlord.sramp.common.query.xpath.ast.PrimaryExpr;
import org.overlord.sramp.common.query.xpath.ast.Query;
import org.overlord.sramp.common.query.xpath.ast.RelationshipPath;
import org.overlord.sramp.common.query.xpath.ast.SubartifactSet;
import org.overlord.sramp.common.query.xpath.visitors.XPathVisitor;
import org.overlord.sramp.repository.jcr.ClassificationHelper;
import org.overlord.sramp.repository.jcr.JCRConstants;
import org.overlord.sramp.repository.jcr.i18n.Messages;

/**
 * Visitor used to produce a JCR SQL2 query from an S-RAMP xpath query.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampToJcrSql2QueryVisitor implements XPathVisitor {

	private static QName CLASSIFIED_BY_ANY_OF = new QName(SrampConstants.SRAMP_NS, "classifiedByAnyOf"); //$NON-NLS-1$
	private static QName CLASSIFIED_BY_ALL_OF = new QName(SrampConstants.SRAMP_NS, "classifiedByAllOf"); //$NON-NLS-1$
	private static QName EXACTLY_CLASSIFIED_BY_ANY_OF = new QName(SrampConstants.SRAMP_NS, "exactlyClassifiedByAnyOf"); //$NON-NLS-1$
	private static QName EXACTLY_CLASSIFIED_BY_ALL_OF = new QName(SrampConstants.SRAMP_NS, "exactlyClassifiedByAllOf"); //$NON-NLS-1$
    private static QName MATCHES = new QName("http://www.w3.org/2005/xpath-functions", "matches"); //$NON-NLS-1$ //$NON-NLS-2$
    private static QName NOT = new QName("http://www.w3.org/2005/xpath-functions", "not"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Map<QName, String> corePropertyMap = new HashMap<QName, String>();
	static {
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "createdBy"), "jcr:createdBy"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "version"), "version"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "uuid"), "sramp:uuid"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "createdTimestamp"), "jcr:created"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "lastModifiedTimestamp"), "jcr:lastModified"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "lastModifiedBy"), "jcr:lastModifiedBy"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "description"), "sramp:description"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "name"), "sramp:name"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "contentType"), "sramp:contentType"); //$NON-NLS-1$ //$NON-NLS-2$
        corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "contentSize"), "sramp:contentSize"); //$NON-NLS-1$ //$NON-NLS-2$
        corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "contentHash"), "sramp:contentHash"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "contentEncoding"), "sramp:contentEncoding"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "extendedType"), "sramp:extendedType"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "ncName"), "sramp:ncName"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "namespace"), "sramp:namespace"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "targetNamespace"), "sramp:targetNamespace"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "style"), "sramp:style"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "transport"), "sramp:transport"); //$NON-NLS-1$ //$NON-NLS-2$
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "soapLocation"), "sramp:soapLocation"); //$NON-NLS-1$ //$NON-NLS-2$

        corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "derived"), "sramp:derived"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private String selectAlias;
	private StringBuilder fromBuilder = new StringBuilder();
	private String notDeletedFilter = null;
	private StringBuilder whereBuilder = new StringBuilder();
    private String artifactPredicateContext = null;
	private String relationshipPredicateContext = null;
	private int relationshipJoinCounter = 1;
	private int artifactJoinCounter = 1;
	private ClassificationHelper classificationHelper;
	private String lastFPS = null;
    private Pattern datePattern = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d"); //$NON-NLS-1$
	private SrampException error;

	/**
	 * Default constructor.
	 * @param classificationHelper
	 */
	public SrampToJcrSql2QueryVisitor(ClassificationHelper classificationHelper) {
		this.classificationHelper = classificationHelper;
	}

	/**
	 * @return the final select alias
	 */
	public String getSelectAlias() {
	    return this.selectAlias;
	}

	/**
	 * Returns the sql-2 query created by this visitor.
	 */
	public String getSql2Query() throws SrampException {
	    if (this.error != null) {
	        throw this.error;
	    }
		String query = "SELECT " + selectAlias + ".* FROM " + fromBuilder.toString(); //$NON-NLS-1$ //$NON-NLS-2$
		String where = whereBuilder.toString() + notDeletedFilter;
		if (where.length() > 0) {
		    if (where.startsWith("AND")) { //$NON-NLS-1$
		        where = where.substring(4);
		    } else if (where.startsWith(" AND")) { //$NON-NLS-1$
                where = where.substring(5);
            } else if (where.startsWith("OR")) { //$NON-NLS-1$
                where = where.substring(3);
            } else if (where.startsWith(" OR")) { //$NON-NLS-1$
                where = where.substring(4);
		    }
		    query = query  + " WHERE " + where; //$NON-NLS-1$
		}
		return query;
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.Query)
	 */
	@Override
	public void visit(Query node) {
	    this.error = null;
	    selectAlias = newArtifactAlias();
	    this.artifactPredicateContext = selectAlias;
		this.fromBuilder.append("[sramp:baseArtifactType] AS " + selectAlias); //$NON-NLS-1$
		this.notDeletedFilter = JCRConstants.NOT_DELETED_FILTER;
		node.getArtifactSet().accept(this);
		if (node.getPredicate() != null) {
			this.whereBuilder.append(" AND ("); //$NON-NLS-1$
			node.getPredicate().accept(this);
			this.whereBuilder.append(")"); //$NON-NLS-1$
		}
		if (node.getSubartifactSet() != null) {
		    SubartifactSet subartifactSet = node.getSubartifactSet();
		    if (subartifactSet.getRelationshipPath() != null) {
		        String relationshipAlias = newRelationshipAlias();
		        String artifactAlias = newArtifactAlias();

		        // Add the JOIN on the relationship
                String oldRelationshipPredicateContext = this.relationshipPredicateContext;
		        this.relationshipPredicateContext = relationshipAlias;
		        this.whereBuilder.append(" AND "); //$NON-NLS-1$
		        subartifactSet.getRelationshipPath().accept(this);
                this.relationshipPredicateContext = oldRelationshipPredicateContext;

		        // Now add another JOIN back around on the "artifact table"
                this.fromBuilder.append(" JOIN [sramp:baseArtifactType] AS "); //$NON-NLS-1$
                this.fromBuilder.append(artifactAlias);
                this.fromBuilder.append(" ON "); //$NON-NLS-1$
                this.fromBuilder.append(relationshipAlias);
                this.fromBuilder.append(".[sramp:relationshipTarget] = "); //$NON-NLS-1$
                this.fromBuilder.append(artifactAlias);
                this.fromBuilder.append(".[jcr:uuid]"); //$NON-NLS-1$

                // Now add any additional predicates included.
		        if (subartifactSet.getPredicate() != null) {
		            String oldArtifactPredicateContext = this.artifactPredicateContext;
		            this.artifactPredicateContext = artifactAlias;
		            this.whereBuilder.append(" AND ("); //$NON-NLS-1$
		            subartifactSet.getPredicate().accept(this);
		            this.whereBuilder.append(")"); //$NON-NLS-1$
                    this.artifactPredicateContext = oldArtifactPredicateContext;
		        }

                this.selectAlias = artifactAlias;
		    }
		    if (subartifactSet.getFunctionCall() != null) {
		          throw new RuntimeException(Messages.i18n.format("XP_SUBARTIFACTSET_NOT_SUPPORTED")); //$NON-NLS-1$
		    }
		    if (subartifactSet.getSubartifactSet() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_TOPLEVEL_SUBARTIFACTSET_ONLY")); //$NON-NLS-1$
		    }
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.LocationPath)
	 */
	@Override
	public void visit(LocationPath node) {
		if (node.getArtifactType() != null) {
			// If this is explicitely *or* implicitely a extended type search...
			if ("ext".equals(node.getArtifactModel()) || !ArtifactTypeEnum.hasEnum(node.getArtifactType())) { //$NON-NLS-1$
                this.whereBuilder.append(artifactPredicateContext);
                this.whereBuilder.append(".[sramp:artifactType] IN ('" //$NON-NLS-1$
                        + ArtifactTypeEnum.ExtendedArtifactType + "', '" + ArtifactTypeEnum.ExtendedDocument //$NON-NLS-1$
                        + "')"); //$NON-NLS-1$
				this.whereBuilder.append(" AND "); //$NON-NLS-1$
                this.whereBuilder.append(artifactPredicateContext);
				this.whereBuilder.append(".[sramp:extendedType] = '" + escapeStringLiteral(node.getArtifactType()) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
                this.whereBuilder.append(artifactPredicateContext);
				this.whereBuilder.append(".[sramp:artifactType] = '" + escapeStringLiteral(node.getArtifactType()) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else if (node.getArtifactModel() != null) {
            this.whereBuilder.append(artifactPredicateContext);
			this.whereBuilder.append(".[sramp:artifactModel] = '" + escapeStringLiteral(node.getArtifactModel()) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.AndExpr)
	 */
	@Override
	public void visit(AndExpr node) {
		if (node.getRight() == null) {
			node.getLeft().accept(this);
		} else {
			node.getLeft().accept(this);
			this.whereBuilder.append(" AND "); //$NON-NLS-1$
			node.getRight().accept(this);
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.Argument)
	 */
	@Override
	public void visit(Argument node) {
		if (node.getPrimaryExpr() != null)
			node.getPrimaryExpr().accept(this);
		else
			throw new RuntimeException(Messages.i18n.format("XP_ONLY_PRIMARY_FUNC_ARGS")); //$NON-NLS-1$
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.ArtifactSet)
	 */
	@Override
	public void visit(ArtifactSet node) {
		node.getLocationPath().accept(this);
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.EqualityExpr)
	 */
	@Override
	public void visit(EqualityExpr node) {
		if (node.getSubartifactSet() != null) {
			node.getSubartifactSet().accept(this);
		} else if (node.getExpr() != null) {
			this.whereBuilder.append(" ( "); //$NON-NLS-1$
			node.getExpr().accept(this);
			this.whereBuilder.append(" ) "); //$NON-NLS-1$
		} else if (node.getOperator() == null) {
			node.getLeft().accept(this);
			this.whereBuilder.append(" IS NOT NULL"); //$NON-NLS-1$
		} else {
			node.getLeft().accept(this);
			this.whereBuilder.append(" "); //$NON-NLS-1$
			this.whereBuilder.append(node.getOperator().symbol());
			this.whereBuilder.append(" "); //$NON-NLS-1$
			node.getRight().accept(this);
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.Expr)
	 */
	@Override
	public void visit(Expr node) {
		node.getAndExpr().accept(this);
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.ForwardPropertyStep)
	 */
	@Override
	public void visit(ForwardPropertyStep node) {
		if (node.getPropertyQName() != null) {
			QName property = node.getPropertyQName();
			if (property.getNamespaceURI() == null || "".equals(property.getNamespaceURI())) //$NON-NLS-1$
				property = new QName(SrampConstants.SRAMP_NS, property.getLocalPart());

			if (property.getNamespaceURI().equals(SrampConstants.SRAMP_NS)) {
				String jcrPropName = null;
				if (corePropertyMap.containsKey(property)) {
					jcrPropName = corePropertyMap.get(property);
				} else {
					jcrPropName = JCRConstants.SRAMP_PROPERTIES + ":" + property.getLocalPart(); //$NON-NLS-1$
				}
				this.whereBuilder.append(this.artifactPredicateContext);
				this.whereBuilder.append(".["); //$NON-NLS-1$
				this.whereBuilder.append(jcrPropName);
				this.whereBuilder.append("]"); //$NON-NLS-1$
				this.lastFPS = jcrPropName;
			} else {
				throw new RuntimeException(Messages.i18n.format("XP_INVALID_PROPERTY_NS", property.getNamespaceURI())); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.FunctionCall)
	 */
	@Override
	public void visit(FunctionCall node) {
		if (SrampConstants.SRAMP_NS.equals(node.getFunctionName().getNamespaceURI())) {
			String propertyName = null, operator = null;
			Collection<URI> classifications = resolveArgumentsToClassifications(node.getArguments());
			if (node.getFunctionName().equals(CLASSIFIED_BY_ALL_OF)) {
				propertyName = "sramp:normalizedClassifiedBy"; //$NON-NLS-1$
				operator = "AND"; //$NON-NLS-1$
			} else if (node.getFunctionName().equals(CLASSIFIED_BY_ANY_OF)) {
				propertyName = "sramp:normalizedClassifiedBy"; //$NON-NLS-1$
				operator = "OR"; //$NON-NLS-1$
			} else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ALL_OF)) {
				propertyName = "sramp:classifiedBy"; //$NON-NLS-1$
				operator = "AND"; //$NON-NLS-1$
			} else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ANY_OF)) {
				propertyName = "sramp:classifiedBy"; //$NON-NLS-1$
				operator = "OR"; //$NON-NLS-1$
			} else {
			    if (node.getFunctionName().getLocalPart().equals("matches") || node.getFunctionName().getLocalPart().equals("not")) { //$NON-NLS-1$ //$NON-NLS-2$
                    throw new RuntimeException(Messages.i18n.format("XP_BAD_FUNC_NS", node.getFunctionName().getLocalPart()) ); //$NON-NLS-1$
			    }
				throw new RuntimeException(Messages.i18n.format("XP_FUNC_NOT_SUPPORTED", node.getFunctionName().toString())); //$NON-NLS-1$
			}

			if (classifications.size() > 1) {
				this.whereBuilder.append("("); //$NON-NLS-1$
			}
			boolean first = true;
			for (URI classification : classifications) {
				if (!first) {
					this.whereBuilder.append(" "); //$NON-NLS-1$
					this.whereBuilder.append(operator);
					this.whereBuilder.append(" "); //$NON-NLS-1$
				}
                this.whereBuilder.append(artifactPredicateContext);
				this.whereBuilder.append(".["); //$NON-NLS-1$
				this.whereBuilder.append(propertyName);
				this.whereBuilder.append("] = '"); //$NON-NLS-1$
				this.whereBuilder.append(escapeStringLiteral(classification.toString()));
				this.whereBuilder.append("'"); //$NON-NLS-1$
				first = false;
			}
			if (classifications.size() > 1) {
				this.whereBuilder.append(")"); //$NON-NLS-1$
			}
		} else if (MATCHES.equals(node.getFunctionName())) {
			if (node.getArguments().size() != 2) {
				throw new RuntimeException(Messages.i18n.format("XP_MATCHES_FUNC_NUM_ARGS_ERROR", node.getArguments().size()));  //$NON-NLS-1$
			}
			Argument attributeArg = node.getArguments().get(0);
			Argument patternArg = node.getArguments().get(1);

			ForwardPropertyStep attribute = reducePropertyArgument(attributeArg);
			String pattern = reduceStringLiteralArgument(patternArg);
			pattern = pattern.replace(".*", "%"); // the only valid wildcard //$NON-NLS-1$ //$NON-NLS-2$

			attribute.accept(this);
			this.whereBuilder.append(" LIKE '"); //$NON-NLS-1$
			this.whereBuilder.append(escapeStringLiteral(pattern));
			this.whereBuilder.append("'"); //$NON-NLS-1$
		} else if (NOT.equals(node.getFunctionName())) {
		    if (node.getArguments().size() != 1) {
		        throw new RuntimeException(Messages.i18n.format("XP_NOT_FUNC_NUM_ARGS_ERROR", node.getArguments().size()));  //$NON-NLS-1$
		    }
		    this.whereBuilder.append("NOT ("); //$NON-NLS-1$
		    Argument argument = node.getArguments().get(0);
		    if (argument.getExpr() != null) {
		        argument.getExpr().accept(this);
		    } else {
		        argument.accept(this);
		    }
		    this.whereBuilder.append(")"); //$NON-NLS-1$
		} else {
			throw new RuntimeException(Messages.i18n.format("XP_FUNCTION_NOT_SUPPORTED", node.getFunctionName().toString())); //$NON-NLS-1$
		}
	}

	/**
	 * Resolves the list of arguments to a collection of classification URIs.
	 * @param arguments
	 */
	private Collection<URI> resolveArgumentsToClassifications(List<Argument> arguments) {
		Collection<String> classifiedBy = new HashSet<String>();
		for (int idx = 1; idx < arguments.size(); idx++) {
			Argument arg = arguments.get(idx);
			if (arg.getPrimaryExpr() == null || arg.getPrimaryExpr().getLiteral() == null) {
				throw new RuntimeException(Messages.i18n.format("XP_INVALID_CLASSIFIER_FORMAT")); //$NON-NLS-1$
			}
			classifiedBy.add(arg.getPrimaryExpr().getLiteral());
		}
		try {
			return this.classificationHelper.resolveAll(classifiedBy);
		} catch (SrampException e) {
		    this.error = e;
		    return Collections.emptySet();
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.OrExpr)
	 */
	@Override
	public void visit(OrExpr node) {
		if (node.getRight() == null) {
			node.getLeft().accept(this);
		} else {
			node.getLeft().accept(this);
			this.whereBuilder.append(" OR "); //$NON-NLS-1$
			node.getRight().accept(this);
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.Predicate)
	 */
	@Override
	public void visit(Predicate node) {
		node.getExpr().accept(this);
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.PrimaryExpr)
	 */
	@Override
	public void visit(PrimaryExpr node) {
		if (node.getLiteral() != null) {
		    boolean isDate = ("jcr:lastModified".equals(this.lastFPS) || "jcr:created".equals(this.lastFPS)) //$NON-NLS-1$ //$NON-NLS-2$
		            && this.datePattern.matcher(node.getLiteral()).find();
		    if (isDate) {
		        this.whereBuilder.append("CAST("); //$NON-NLS-1$
		    }
			this.whereBuilder.append("'"); //$NON-NLS-1$
			// TODO prevent injection here
			this.whereBuilder.append(node.getLiteral());
			this.whereBuilder.append("'"); //$NON-NLS-1$
            if (isDate) {
                this.whereBuilder.append(" AS DATE)"); //$NON-NLS-1$
            }
		} else if (node.getNumber() != null) {
			this.whereBuilder.append(node.getNumber());
		} else if (node.getPropertyQName() != null) {
			throw new RuntimeException(Messages.i18n.format("XP_PROPERTY_PRIMARY_EXPR_NOT_SUPPORTED")); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.RelationshipPath)
	 */
	@Override
	public void visit(RelationshipPath node) {
		String alias = this.relationshipPredicateContext;

		fromBuilder.append(" JOIN [sramp:relationship] AS "); //$NON-NLS-1$
		fromBuilder.append(alias);
		fromBuilder.append(" ON ISCHILDNODE("); //$NON-NLS-1$
		fromBuilder.append(alias);
		fromBuilder.append(", "); //$NON-NLS-1$
        fromBuilder.append(this.artifactPredicateContext);
        fromBuilder.append(")"); //$NON-NLS-1$

		whereBuilder.append(alias);
		whereBuilder.append(".[sramp:relationshipType] = '"); //$NON-NLS-1$
		whereBuilder.append(node.getRelationshipType());
		whereBuilder.append("'"); //$NON-NLS-1$
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.SubartifactSet)
	 */
	@Override
	public void visit(SubartifactSet node) {
		if (node.getFunctionCall() != null) {
			node.getFunctionCall().accept(this);
		} else if (node.getRelationshipPath() != null) {
			if (node.getPredicate() != null) {
				whereBuilder.append("("); // open the predicate paren //$NON-NLS-1$
			}
			String relationshipAlias = newRelationshipAlias();
			String oldRelationshipPredicateContext = this.relationshipPredicateContext;
			this.relationshipPredicateContext = relationshipAlias;
			node.getRelationshipPath().accept(this);
			this.relationshipPredicateContext = oldRelationshipPredicateContext;
			if (node.getPredicate() != null) {
			    String artifactAlias = newArtifactAlias();
				this.whereBuilder.append(" AND "); //$NON-NLS-1$
				this.whereBuilder.append(relationshipAlias);
				this.whereBuilder.append(".[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS "); //$NON-NLS-1$
				this.whereBuilder.append(artifactAlias);
				this.whereBuilder.append(" WHERE "); //$NON-NLS-1$

				String oldArtifactPredicateContext = this.artifactPredicateContext;
				this.artifactPredicateContext = artifactAlias;
				node.getPredicate().accept(this);
				this.artifactPredicateContext = oldArtifactPredicateContext;

				this.whereBuilder.append(")"); // close the sub-query paren //$NON-NLS-1$
			}
			if (node.getPredicate() != null) {
				whereBuilder.append(")"); // Close the predicate paren //$NON-NLS-1$
			}

			if (node.getSubartifactSet() != null) {
				throw new RuntimeException(Messages.i18n.format("XP_MULTILEVEL_SUBARTYSETS_NOT_SUPPORTED")); //$NON-NLS-1$
			}
		}
	}

    /**
     * @return a new (unused) alias for a relationship (typically used in JOINs)
     */
    protected String newRelationshipAlias() {
        return "relationship" + relationshipJoinCounter++; //$NON-NLS-1$
    }

    /**
     * @return a new (unused) alias for an artifact (typically used in JOINs)
     */
    protected String newArtifactAlias() {
        return "artifact" + artifactJoinCounter++; //$NON-NLS-1$
    }

	/**
	 * Reduces an Argument subtree to the final {@link ForwardPropertyStep} that is it's (supposed)
	 * final node.  This method will throw a runtime exception if it doesn't find the expected
	 * {@link ForwardPropertyStep}.
	 * @param argument
	 */
	private ForwardPropertyStep reducePropertyArgument(Argument argument) {
		try {
			ForwardPropertyStep fps = argument.getExpr().getAndExpr().getLeft().getLeft().getLeft();
			if (fps == null) {
				throw new NullPointerException();
			}
			return fps;
		} catch (Throwable t) {
			throw new RuntimeException(Messages.i18n.format("XP_EXPECTED_PROPERTY_ARG")); //$NON-NLS-1$
		}
	}

	/**
	 * Reduces an Argument to a string literal.  This method will throw a runtime exception if it
	 * doesn't find the expected string literal.
	 * @param argument
	 */
	private String reduceStringLiteralArgument(Argument argument) {
		try {
			String l = argument.getPrimaryExpr().getLiteral();
			if (l == null) {
				throw new NullPointerException();
			}
			return l;
		} catch (Throwable t) {
			throw new RuntimeException(Messages.i18n.format("XP_EXPECTED_STRING_LITERAL_ARG")); //$NON-NLS-1$
		}
	}

	/**
	 * Escape string literals to prevent injection.
	 * @param literal
	 */
	private String escapeStringLiteral(String literal) {
		return literal.replace("'", "''"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
