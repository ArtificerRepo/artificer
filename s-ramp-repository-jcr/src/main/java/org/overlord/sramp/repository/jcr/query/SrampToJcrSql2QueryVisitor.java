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

import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.query.xpath.ast.*;
import org.overlord.sramp.common.query.xpath.visitors.XPathVisitor;
import org.overlord.sramp.repository.jcr.ClassificationHelper;
import org.overlord.sramp.repository.jcr.JCRConstants;
import org.overlord.sramp.repository.jcr.i18n.Messages;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Visitor used to produce a JCR SQL2 query from an S-RAMP xpath query.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampToJcrSql2QueryVisitor implements XPathVisitor {

	private static final QName CLASSIFIED_BY_ANY_OF = new QName(SrampConstants.SRAMP_NS, "classifiedByAnyOf");
	private static final QName CLASSIFIED_BY_ALL_OF = new QName(SrampConstants.SRAMP_NS, "classifiedByAllOf");
	private static final QName EXACTLY_CLASSIFIED_BY_ANY_OF = new QName(SrampConstants.SRAMP_NS, "exactlyClassifiedByAnyOf");
	private static final QName EXACTLY_CLASSIFIED_BY_ALL_OF = new QName(SrampConstants.SRAMP_NS, "exactlyClassifiedByAllOf");

    public static final QName GET_RELATIONSHIP_ATTRIBUTE = new QName(SrampConstants.SRAMP_NS, "getRelationshipAttribute");

    private static final QName MATCHES = new QName("http://www.w3.org/2005/xpath-functions", "matches");
    private static final QName NOT = new QName("http://www.w3.org/2005/xpath-functions", "not");

	private static final Map<QName, String> corePropertyMap = new HashMap<QName, String>();
	static {
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "createdBy"), JCRConstants.JCR_CREATED_BY);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "version"), "version");
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "uuid"), JCRConstants.SRAMP_UUID);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "createdTimestamp"), JCRConstants.JCR_CREATED);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "lastModifiedTimestamp"), JCRConstants.JCR_LAST_MODIFIED);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "lastModifiedBy"), JCRConstants.JCR_LAST_MODIFIED_BY);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "description"), JCRConstants.SRAMP_DESCRIPTION);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "name"), JCRConstants.SRAMP_NAME);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "contentType"), JCRConstants.SRAMP_CONTENT_TYPE);
        corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "contentSize"), JCRConstants.SRAMP_CONTENT_SIZE);
        corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "contentHash"), JCRConstants.SRAMP_CONTENT_HASH);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "contentEncoding"), JCRConstants.SRAMP_CONTENT_ENCODING);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "extendedType"), JCRConstants.SRAMP_EXTENDED_TYPE);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "ncName"), JCRConstants.SRAMP_NC_NAME);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "namespace"), JCRConstants.SRAMP_NAMESPACE);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "targetNamespace"), JCRConstants.SRAMP_TARGET_NAMESPACE);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "style"), JCRConstants.SRAMP_STYLE);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "transport"), JCRConstants.SRAMP_TRANSPORT);
		corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "soapLocation"), JCRConstants.SRAMP_SOAP_LOCATION);

        corePropertyMap.put(new QName(SrampConstants.SRAMP_NS, "derived"), JCRConstants.SRAMP_DERIVED);
	}
	
	private String selectAlias;
	private StringBuilder fromBuilder = new StringBuilder();
	private String notDeletedFilter = null;
	private StringBuilder whereBuilder = new StringBuilder();
    private String artifactPredicateContext = null;
	private String relationshipPredicateContext = null;
    private int relationshipJoinCounter = 1;
    private String targetPredicateContext = null;
    private int targetJoinCounter = 1;
	private int artifactJoinCounter = 1;
	private ClassificationHelper classificationHelper;
	private String lastFPS = null;
    private Pattern datePattern = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
	private SrampException error;
	// Ugly, dirty hack.  Ex query: /s-ramp/wsdl/WsdlDocument[someRelationship[s-ramp:getRelationshipAttribute(., 'someAttribute') = 'true']]
	// Note that the predicate function needs to affect the preceding relationship itself, *not* the target artifact
	// subselect.  The relevant visitor will set this String, which will block the subselect and instead appear
	// on the root where condition.
	// TODO: This isn't introduced in every method (nor should it), so you may need to add handling elsewhere.  See
	// the switch in visit(PrimaryExpr node) as an example.
    private StringBuilder relationshipWhereBuilder = null;

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
		String query = "SELECT " + selectAlias + ".* FROM " + fromBuilder.toString();
		String where = whereBuilder.toString() + notDeletedFilter;
		if (where.length() > 0) {
		    if (where.startsWith("AND")) {
		        where = where.substring(4);
		    } else if (where.startsWith(" AND")) {
                where = where.substring(5);
            } else if (where.startsWith("OR")) {
                where = where.substring(3);
            } else if (where.startsWith(" OR")) {
                where = where.substring(4);
		    }
		    query = query  + " WHERE " + where;
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
		this.fromBuilder.append("[sramp:baseArtifactType] AS " + selectAlias);
		this.notDeletedFilter = JCRConstants.NOT_DELETED_FILTER;
		node.getArtifactSet().accept(this);
		if (node.getPredicate() != null) {
			this.whereBuilder.append(" AND (");
			node.getPredicate().accept(this);
			this.whereBuilder.append(")");
		}
		if (node.getSubartifactSet() != null) {
		    SubartifactSet subartifactSet = node.getSubartifactSet();
		    if (subartifactSet.getRelationshipPath() != null) {
		        String relationshipAlias = newRelationshipAlias();
                String targetAlias = newTargetAlias();
		        String artifactAlias = newArtifactAlias();

		        // Add the JOIN on the relationship
                String oldRelationshipPredicateContext = this.relationshipPredicateContext;
		        this.relationshipPredicateContext = relationshipAlias;
                String oldTargetPredicateContext = this.targetPredicateContext;
                this.targetPredicateContext = targetAlias;
		        this.whereBuilder.append(" AND ");
		        subartifactSet.getRelationshipPath().accept(this);
                this.relationshipPredicateContext = oldRelationshipPredicateContext;
                this.targetPredicateContext = oldTargetPredicateContext;

		        // Now add another JOIN back around on the "artifact table"
                this.fromBuilder.append(" JOIN [sramp:baseArtifactType] AS ");
                this.fromBuilder.append(artifactAlias);
                this.fromBuilder.append(" ON ");
                this.fromBuilder.append(targetAlias);
                this.fromBuilder.append(".[sramp:targetArtifact] = ");
                this.fromBuilder.append(artifactAlias);
                this.fromBuilder.append(".[jcr:uuid]");

                // Now add any additional predicates included.
		        if (subartifactSet.getPredicate() != null) {
		            String oldArtifactPredicateContext = this.artifactPredicateContext;
		            this.artifactPredicateContext = artifactAlias;
		            this.whereBuilder.append(" AND (");
		            subartifactSet.getPredicate().accept(this);
		            this.whereBuilder.append(")");
                    this.artifactPredicateContext = oldArtifactPredicateContext;
		        }

                this.selectAlias = artifactAlias;
		    }
		    if (subartifactSet.getFunctionCall() != null) {
		          throw new RuntimeException(Messages.i18n.format("XP_SUBARTIFACTSET_NOT_SUPPORTED"));
		    }
		    if (subartifactSet.getSubartifactSet() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_TOPLEVEL_SUBARTIFACTSET_ONLY"));
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
			if ("ext".equals(node.getArtifactModel()) || !ArtifactTypeEnum.hasEnum(node.getArtifactType())) {
                this.whereBuilder.append(artifactPredicateContext);
                this.whereBuilder.append(".[sramp:artifactType] IN ('"
                        + ArtifactTypeEnum.ExtendedArtifactType + "', '" + ArtifactTypeEnum.ExtendedDocument
                        + "')");
				this.whereBuilder.append(" AND ");
                this.whereBuilder.append(artifactPredicateContext);
				this.whereBuilder.append(".[sramp:extendedType] = '" + escapeStringLiteral(node.getArtifactType()) + "'");
			} else {
                this.whereBuilder.append(artifactPredicateContext);
				this.whereBuilder.append(".[sramp:artifactType] = '" + escapeStringLiteral(node.getArtifactType()) + "'");
			}
		} else if (node.getArtifactModel() != null) {
            this.whereBuilder.append(artifactPredicateContext);
			this.whereBuilder.append(".[sramp:artifactModel] = '" + escapeStringLiteral(node.getArtifactModel()) + "'");
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
			this.whereBuilder.append(" AND ");
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
			throw new RuntimeException(Messages.i18n.format("XP_ONLY_PRIMARY_FUNC_ARGS"));
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
			this.whereBuilder.append(" ( ");
			node.getExpr().accept(this);
			this.whereBuilder.append(" ) ");
		} else if (node.getOperator() == null) {
			node.getLeft().accept(this);
			if (relationshipWhereBuilder != null) {
				relationshipWhereBuilder.append(" IS NOT NULL");
			} else {
				this.whereBuilder.append(" IS NOT NULL");
			}
		} else {
			node.getLeft().accept(this);
			if (relationshipWhereBuilder != null) {
				relationshipWhereBuilder.append(" ");
				relationshipWhereBuilder.append(node.getOperator().symbol());
				relationshipWhereBuilder.append(" ");
				node.getRight().accept(this);
			} else {
				this.whereBuilder.append(" ");
				this.whereBuilder.append(node.getOperator().symbol());
				this.whereBuilder.append(" ");
				node.getRight().accept(this);
			}
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
			if (property.getNamespaceURI() == null || "".equals(property.getNamespaceURI()))
				property = new QName(SrampConstants.SRAMP_NS, property.getLocalPart());

			if (property.getNamespaceURI().equals(SrampConstants.SRAMP_NS)) {
				String jcrPropName = null;
				if (corePropertyMap.containsKey(property)) {
					jcrPropName = corePropertyMap.get(property);
				} else {
					jcrPropName = JCRConstants.SRAMP_PROPERTIES + ":" + property.getLocalPart();
				}
				this.whereBuilder.append(this.artifactPredicateContext);
				this.whereBuilder.append(".[");
				this.whereBuilder.append(jcrPropName);
				this.whereBuilder.append("]");
				this.lastFPS = jcrPropName;
			} else {
				throw new RuntimeException(Messages.i18n.format("XP_INVALID_PROPERTY_NS", property.getNamespaceURI()));
			}
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.FunctionCall)
	 */
	@Override
	public void visit(FunctionCall node) {
		if (SrampConstants.SRAMP_NS.equals(node.getFunctionName().getNamespaceURI())) {
			if (node.getFunctionName().equals(CLASSIFIED_BY_ALL_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY, "AND");
			} else if (node.getFunctionName().equals(CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY, "OR");
			} else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ALL_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_CLASSIFIED_BY, "AND");
			} else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_CLASSIFIED_BY, "OR");
			} else if (node.getFunctionName().equals(GET_RELATIONSHIP_ATTRIBUTE)) {
                if (node.getArguments().size() != 2) {
                    // TODO: throw?
                }
				// Dirty hack.  Ex query: /s-ramp/wsdl/WsdlDocument[someRelationship[s-ramp:getRelationshipAttribute(., 'someAttribute') = 'true']]
				// Note that the predicate function needs to affect the preceding relationship itself, *not* the target artifact
				// subselect.  The relevant visitor will set this String, which will block the subselect and instead appear
				// on the root where condition.
                String otherAttributeKey = reduceStringLiteralArgument(node.getArguments().get(1));
				relationshipWhereBuilder = new StringBuilder("%1$s.[" + JCRConstants.SRAMP_OTHER_ATTRIBUTES + ":" + otherAttributeKey + "]");
            } else {
			    if (node.getFunctionName().getLocalPart().equals("matches") || node.getFunctionName().getLocalPart().equals("not")) {
                    throw new RuntimeException(Messages.i18n.format("XP_BAD_FUNC_NS", node.getFunctionName().getLocalPart()) );
			    }
				throw new RuntimeException(Messages.i18n.format("XP_FUNC_NOT_SUPPORTED", node.getFunctionName().toString()));
			}
		} else if (MATCHES.equals(node.getFunctionName())) {
			if (node.getArguments().size() != 2) {
				throw new RuntimeException(Messages.i18n.format("XP_MATCHES_FUNC_NUM_ARGS_ERROR", node.getArguments().size())); 
			}
			Argument attributeArg = node.getArguments().get(0);
			Argument patternArg = node.getArguments().get(1);

			ForwardPropertyStep attribute = reducePropertyArgument(attributeArg);
			String pattern = reduceStringLiteralArgument(patternArg);
			pattern = pattern.replace(".*", "%"); // the only valid wildcard

			attribute.accept(this);
			this.whereBuilder.append(" LIKE '");
			this.whereBuilder.append(escapeStringLiteral(pattern));
			this.whereBuilder.append("'");
		} else if (NOT.equals(node.getFunctionName())) {
		    if (node.getArguments().size() != 1) {
		        throw new RuntimeException(Messages.i18n.format("XP_NOT_FUNC_NUM_ARGS_ERROR", node.getArguments().size())); 
		    }
		    this.whereBuilder.append("NOT (");
		    Argument argument = node.getArguments().get(0);
		    if (argument.getExpr() != null) {
		        argument.getExpr().accept(this);
		    } else {
		        argument.accept(this);
		    }
		    this.whereBuilder.append(")");
		} else {
			throw new RuntimeException(Messages.i18n.format("XP_FUNCTION_NOT_SUPPORTED", node.getFunctionName().toString()));
		}
	}

    private void visitClassifications(FunctionCall node, String propertyName, String operator) {
        Collection<URI> classifications = resolveArgumentsToClassifications(node.getArguments());

        if (classifications.size() > 1) {
            this.whereBuilder.append("(");
        }
        boolean first = true;
        for (URI classification : classifications) {
            if (!first) {
                this.whereBuilder.append(" ");
                this.whereBuilder.append(operator);
                this.whereBuilder.append(" ");
            }
            this.whereBuilder.append(artifactPredicateContext);
            this.whereBuilder.append(".[");
            this.whereBuilder.append(propertyName);
            this.whereBuilder.append("] = '");
            this.whereBuilder.append(escapeStringLiteral(classification.toString()));
            this.whereBuilder.append("'");
            first = false;
        }
        if (classifications.size() > 1) {
            this.whereBuilder.append(")");
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
                throw new RuntimeException(Messages.i18n.format("XP_INVALID_CLASSIFIER_FORMAT"));
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
			this.whereBuilder.append(" OR ");
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
		StringBuilder sb;
		if (relationshipWhereBuilder != null) {
			sb = relationshipWhereBuilder;
		} else {
			sb = whereBuilder;
		}

		if (node.getLiteral() != null) {
		    boolean isDate = (JCRConstants.JCR_LAST_MODIFIED.equals(this.lastFPS) || JCRConstants.JCR_CREATED.equals(this.lastFPS))
		            && this.datePattern.matcher(node.getLiteral()).find();
		    if (isDate) {
		        sb.append("CAST(");
		    }
			sb.append("'");
			// TODO prevent injection here
			sb.append(node.getLiteral());
			sb.append("'");
            if (isDate) {
                sb.append(" AS DATE)");
            }
		} else if (node.getNumber() != null) {
			sb.append(node.getNumber());
		} else if (node.getPropertyQName() != null) {
			throw new RuntimeException(Messages.i18n.format("XP_PROPERTY_PRIMARY_EXPR_NOT_SUPPORTED"));
		}
	}

	/**
	 * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.RelationshipPath)
	 */
	@Override
	public void visit(RelationshipPath node) {
		String relationshipAlias = this.relationshipPredicateContext;
        String targetAlias = this.targetPredicateContext;

		fromBuilder.append(" JOIN [sramp:relationship] AS ");
		fromBuilder.append(relationshipAlias);
		fromBuilder.append(" ON ISCHILDNODE(");
		fromBuilder.append(relationshipAlias);
		fromBuilder.append(", ");
        fromBuilder.append(this.artifactPredicateContext);
        fromBuilder.append(")");

        if (targetAlias != null) {
            fromBuilder.append(" JOIN [sramp:target] AS ");
            fromBuilder.append(targetAlias);
            fromBuilder.append(" ON ISCHILDNODE(");
            fromBuilder.append(targetAlias);
            fromBuilder.append(", ");
            fromBuilder.append(relationshipAlias);
            fromBuilder.append(")");
        }

		whereBuilder.append(relationshipAlias);
		whereBuilder.append(".[sramp:relationshipType] = '");
		whereBuilder.append(node.getRelationshipType());
		whereBuilder.append("'");
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
				whereBuilder.append("("); // open the predicate paren
			}
			String relationshipAlias = newRelationshipAlias();
			String oldRelationshipPredicateContext = this.relationshipPredicateContext;
			this.relationshipPredicateContext = relationshipAlias;
            // TODO: Incredibly hacky.  Should not create the sramp:target JOIN in visit(RelationshipPath) unless
            // the target is actually needed below.
            String targetAlias = node.getPredicate() != null ? newTargetAlias() : null;
            String oldTargetPredicateContext = this.targetPredicateContext;
            this.targetPredicateContext = targetAlias;
			node.getRelationshipPath().accept(this);
            this.relationshipPredicateContext = oldRelationshipPredicateContext;
            this.targetPredicateContext = oldTargetPredicateContext;
			if (node.getPredicate() != null) {
			    String artifactAlias = newArtifactAlias();
				this.whereBuilder.append(" AND ");
				this.whereBuilder.append(targetAlias);
				this.whereBuilder.append(".[sramp:targetArtifact] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS ");
				this.whereBuilder.append(artifactAlias);
				this.whereBuilder.append(" WHERE ");

				String oldArtifactPredicateContext = this.artifactPredicateContext;
				this.artifactPredicateContext = artifactAlias;
				node.getPredicate().accept(this);
				this.artifactPredicateContext = oldArtifactPredicateContext;

				this.whereBuilder.append(")"); // close the sub-query paren

				// If the predicate resulted in the *sole* use of relationshipWhereBuilder, we need to remove the
				// useless " WHERE " from the end of the sub-query.
				// Holy crap, this is terrible.
				int i = this.whereBuilder.indexOf(" WHERE )");
				if (i != -1) {
					this.whereBuilder.delete(i, i + 7);
				}

				if (relationshipWhereBuilder != null) {
					String relationshipWhere = relationshipWhereBuilder.toString();
					// Includes '%1$s' as a placeholders.
					relationshipWhere = String.format(relationshipWhere, relationshipAlias);
					this.whereBuilder.append(" AND " + relationshipWhere);
					relationshipWhereBuilder = null;
				}
			}
			if (node.getPredicate() != null) {
				whereBuilder.append(")"); // Close the predicate paren
			}

			if (node.getSubartifactSet() != null) {
				throw new RuntimeException(Messages.i18n.format("XP_MULTILEVEL_SUBARTYSETS_NOT_SUPPORTED"));
			}
		}
	}

    /**
     * @return a new (unused) alias for a relationship (typically used in JOINs)
     */
    protected String newRelationshipAlias() {
        return "relationship" + relationshipJoinCounter++;
    }

    /**
     * @return a new (unused) alias for a target (typically used in JOINs)
     */
    protected String newTargetAlias() {
        return "target" + targetJoinCounter++;
    }

    /**
     * @return a new (unused) alias for an artifact (typically used in JOINs)
     */
    protected String newArtifactAlias() {
        return "artifact" + artifactJoinCounter++;
    }

	/**
	 * Reduces an Argument subtree to the final {@link ForwardPropertyStep} that is it's (supposed)
	 * final node.  This method will throw a runtime exception if it doesn't find the expected
	 * {@link ForwardPropertyStep}.
	 * @param argument
	 */
	private ForwardPropertyStep reducePropertyArgument(Argument argument) {
		try {
			ForwardPropertyStep fps = (ForwardPropertyStep) argument.getExpr().getAndExpr().getLeft().getLeft().getLeft();
			if (fps == null) {
				throw new NullPointerException();
			}
			return fps;
		} catch (Throwable t) {
			throw new RuntimeException(Messages.i18n.format("XP_EXPECTED_PROPERTY_ARG"));
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
			throw new RuntimeException(Messages.i18n.format("XP_EXPECTED_STRING_LITERAL_ARG"));
		}
	}

	/**
	 * Escape string literals to prevent injection.
	 * @param literal
	 */
	private String escapeStringLiteral(String literal) {
		return literal.replace("'", "''");
	}

}
