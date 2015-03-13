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
package org.artificer.repository.jcr.query;

import org.artificer.common.ArtifactTypeEnum;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.ArtificerException;
import org.artificer.common.query.xpath.ast.AbstractXPathNode;
import org.artificer.common.query.xpath.ast.AndExpr;
import org.artificer.common.query.xpath.ast.Argument;
import org.artificer.common.query.xpath.ast.ArtifactSet;
import org.artificer.common.query.xpath.ast.EqualityExpr;
import org.artificer.common.query.xpath.ast.Expr;
import org.artificer.common.query.xpath.ast.ForwardPropertyStep;
import org.artificer.common.query.xpath.ast.FunctionCall;
import org.artificer.common.query.xpath.ast.LocationPath;
import org.artificer.common.query.xpath.ast.OrExpr;
import org.artificer.common.query.xpath.ast.Predicate;
import org.artificer.common.query.xpath.ast.PrimaryExpr;
import org.artificer.common.query.xpath.ast.Query;
import org.artificer.common.query.xpath.ast.RelationshipPath;
import org.artificer.common.query.xpath.ast.SubartifactSet;
import org.artificer.common.query.xpath.visitors.XPathVisitor;
import org.artificer.repository.error.QueryExecutionException;
import org.artificer.repository.jcr.ClassificationHelper;
import org.artificer.repository.jcr.JCRConstants;
import org.artificer.repository.jcr.i18n.Messages;
import org.modeshape.jcr.api.query.qom.Operator;
import org.modeshape.jcr.query.model.Column;
import org.modeshape.jcr.query.model.DynamicOperand;
import org.modeshape.jcr.query.model.JoinCondition;
import org.modeshape.jcr.query.model.Ordering;
import org.modeshape.jcr.query.model.QueryCommand;
import org.modeshape.jcr.query.model.QueryObjectModel;
import org.modeshape.jcr.query.model.QueryObjectModelFactory;
import org.modeshape.jcr.query.model.SelectQuery;
import org.modeshape.jcr.query.model.Selector;
import org.modeshape.jcr.query.model.Source;
import org.modeshape.jcr.query.model.StaticOperand;
import org.modeshape.jcr.query.model.Subquery;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.xml.namespace.QName;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Visitor used to produce a JCR SQL2 query from an S-RAMP xpath query.
 *
 * @author Brett Meyer
 */
public class ArtificerToJcrSql2QueryVisitor implements XPathVisitor {

    private static final QName CLASSIFIED_BY_ANY_OF = new QName(ArtificerConstants.SRAMP_NS, "classifiedByAnyOf");
    private static final QName CLASSIFIED_BY_ALL_OF = new QName(ArtificerConstants.SRAMP_NS, "classifiedByAllOf");
    private static final QName EXACTLY_CLASSIFIED_BY_ANY_OF = new QName(ArtificerConstants.SRAMP_NS, "exactlyClassifiedByAnyOf");
    private static final QName EXACTLY_CLASSIFIED_BY_ALL_OF = new QName(ArtificerConstants.SRAMP_NS, "exactlyClassifiedByAllOf");

    public static final QName GET_RELATIONSHIP_ATTRIBUTE = new QName(ArtificerConstants.SRAMP_NS, "getRelationshipAttribute");
    public static final QName GET_TARGET_ATTRIBUTE = new QName(ArtificerConstants.SRAMP_NS, "getTargetAttribute");

    private static final QName MATCHES = new QName("http://www.w3.org/2005/xpath-functions", "matches");
    private static final QName NOT = new QName("http://www.w3.org/2005/xpath-functions", "not");

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private static final Map<QName, String> corePropertyMap = new HashMap<>();
    static {
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "createdBy"), JCRConstants.JCR_CREATED_BY);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "version"), "version");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "uuid"), JCRConstants.SRAMP_UUID);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "createdTimestamp"), JCRConstants.JCR_CREATED);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "lastModifiedTimestamp"), JCRConstants.JCR_LAST_MODIFIED);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "lastModifiedBy"), JCRConstants.JCR_LAST_MODIFIED_BY);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "description"), JCRConstants.SRAMP_DESCRIPTION);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "name"), JCRConstants.SRAMP_NAME);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "contentType"), JCRConstants.SRAMP_CONTENT_TYPE);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "contentSize"), JCRConstants.SRAMP_CONTENT_SIZE);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "contentHash"), JCRConstants.SRAMP_CONTENT_HASH);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "contentEncoding"), JCRConstants.SRAMP_CONTENT_ENCODING);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "extendedType"), JCRConstants.SRAMP_EXTENDED_TYPE);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "ncName"), JCRConstants.SRAMP_NC_NAME);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "namespace"), JCRConstants.SRAMP_NAMESPACE);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "targetNamespace"), JCRConstants.SRAMP_TARGET_NAMESPACE);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "style"), JCRConstants.SRAMP_STYLE);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "transport"), JCRConstants.SRAMP_TRANSPORT);
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "soapLocation"), JCRConstants.SRAMP_SOAP_LOCATION);

        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "derived"), JCRConstants.SRAMP_DERIVED);
    }

    private Session session;
    private QueryManager queryManager;
    // Note: We're relying on a few ModeShape-specific extensions, such as "in", subselects, sets, etc.  Technically,
    // we could use the JCR QueryObjectModelFactory for most of the visitor.  But we're using the ModeShape version
    // for simplicity.
    private QueryObjectModelFactory factory;

    private String order;
    private boolean orderAscending;

    private List<Constraint> rootConstraints = new ArrayList<>();
    private List<Constraint> constraintsContext = rootConstraints;

    private ClassificationHelper classificationHelper;

    private Source sourceContext = null;
    private String selectorContext = null;
    private String propertyContext = null;
    private String relationshipContext = null;
    private String targetContext = null;
    private Value valueContext = null;

    private String singleUseSelectorContext = null;

    private boolean contentChildJoined = false;

    private ArtificerException error;

    /**
     * Default constructor.
     * @param session
     * @param classificationHelper
     */
    public ArtificerToJcrSql2QueryVisitor(Session session, ClassificationHelper classificationHelper) throws ArtificerException {
        this.session = session;
        try {
            this.queryManager = session.getWorkspace().getQueryManager();
            factory = (QueryObjectModelFactory) queryManager.getQOMFactory();
        } catch (RepositoryException e) {
            throw new QueryExecutionException(e);
        }
        this.classificationHelper = classificationHelper;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void setOrderAscending(boolean orderAscending) {
        this.orderAscending = orderAscending;
    }

    public javax.jcr.query.Query buildQuery() throws ArtificerException {
        if (this.error != null) {
            throw this.error;
        }
        Ordering[] orderings = null;
        if (order != null) {
            orderings = new Ordering[1];
            if (orderAscending) {
                orderings[0] = factory.ascending(factory.propertyValue(selectorContext, order));
            } else {
                orderings[0] = factory.descending(factory.propertyValue(selectorContext, order));
            }
        }
        Column[] columns = new Column[1];
        columns[0] = factory.column(selectorContext, null, null);

        QueryObjectModel query = factory.createQuery(sourceContext, compileAnd(rootConstraints), orderings, columns);
        return query;
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.Query)
     */
    @Override
    public void visit(Query node) {
        this.error = null;
        String selectAlias = newArtifactAlias();
        this.selectorContext = selectAlias;

        sourceContext = factory.selector("sramp:baseArtifactType", selectAlias);

        node.getArtifactSet().accept(this);
        if (node.getPredicate() != null) {
            node.getPredicate().accept(this);
        }
        if (node.getSubartifactSet() != null) {
            SubartifactSet subartifactSet = node.getSubartifactSet();
            if (subartifactSet.getRelationshipPath() != null) {
                String relationshipAlias = newRelationshipAlias();
                String targetAlias = newTargetAlias();
                String artifactAlias = newArtifactAlias();

                // Add the JOIN on the relationship
                String oldRelationshipPredicateContext = this.relationshipContext;
                this.relationshipContext = relationshipAlias;
                String oldTargetPredicateContext = this.targetContext;
                this.targetContext = targetAlias;
                subartifactSet.getRelationshipPath().accept(this);

                // Now add another JOIN back around on the "artifact table"
                joinEq("sramp:baseArtifactType", targetAlias, "sramp:targetArtifact", artifactAlias, "jcr:uuid",
                        QueryObjectModelConstants.JCR_JOIN_TYPE_INNER);

                // Root selector now needs to be the relationship targets.
                selectorContext = artifactAlias;

                // Now add any additional predicates included.
                if (subartifactSet.getPredicate() != null) {
                    subartifactSet.getPredicate().accept(this);
                }

                this.relationshipContext = oldRelationshipPredicateContext;
                this.targetContext = oldTargetPredicateContext;
            }
            if (subartifactSet.getFunctionCall() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_SUBARTIFACTSET_NOT_SUPPORTED"));
            }
            if (subartifactSet.getSubartifactSet() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_TOPLEVEL_SUBARTIFACTSET_ONLY"));
            }
        }

        // Filter out the trash
        descendant(selectAlias, JCRConstants.ROOT_PATH);
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.LocationPath)
     */
    @Override
    public void visit(LocationPath node) {
        if (node.getArtifactType() != null) {
            // If this is explicitely *or* implicitely a extended type search...
            if ("ext".equals(node.getArtifactModel()) || !ArtifactTypeEnum.hasEnum(node.getArtifactType())) {
                in(getSelectorContext(), "sramp:artifactType",
                        ArtifactTypeEnum.ExtendedArtifactType.toString(), ArtifactTypeEnum.ExtendedDocument.toString());
                operation(getSelectorContext(), "sramp:extendedType", QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, node.getArtifactType());
            } else {
                operation(getSelectorContext(), "sramp:artifactType", QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, node.getArtifactType());
            }
        } else if (node.getArtifactModel() != null) {
            operation(getSelectorContext(), "sramp:artifactModel", QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, node.getArtifactModel());
        }
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.AndExpr)
     */
    @Override
    public void visit(AndExpr node) {
        if (node.getRight() == null) {
            node.getLeft().accept(this);
        } else {
            List<Constraint> oldConstraintsContext = constraintsContext;
            constraintsContext = new ArrayList<>();
            node.getLeft().accept(this);
            node.getRight().accept(this);
            oldConstraintsContext.add(compileAnd(constraintsContext));
            constraintsContext = oldConstraintsContext;
        }
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.Argument)
     */
    @Override
    public void visit(Argument node) {
        if (node.getPrimaryExpr() != null)
            node.getPrimaryExpr().accept(this);
        else
            throw new RuntimeException(Messages.i18n.format("XP_ONLY_PRIMARY_FUNC_ARGS"));
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.ArtifactSet)
     */
    @Override
    public void visit(ArtifactSet node) {
        node.getLocationPath().accept(this);
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.EqualityExpr)
     */
    @Override
    public void visit(EqualityExpr node) {

        if (node.getSubartifactSet() != null) {
            node.getSubartifactSet().accept(this);
        } else if (node.getExpr() != null) {
            node.getExpr().accept(this);
        } else if (node.getOperator() == null) {
            node.getLeft().accept(this);
            exists(getSelectorContext(), propertyContext);
        } else {
            // TODO: Not guaranteed to be propertyContext -- may be function, etc.
            node.getLeft().accept(this);
            node.getRight().accept(this);
            operation(getSelectorContext(), propertyContext, getJcrOperator(node.getOperator().symbol()), valueContext);
            valueContext = null;
        }
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.Expr)
     */
    @Override
    public void visit(Expr node) {
        node.getAndExpr().accept(this);
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.ForwardPropertyStep)
     */
    @Override
    public void visit(ForwardPropertyStep node) {
        if (node.getPropertyQName() != null) {
            QName property = node.getPropertyQName();
            if (property.getNamespaceURI() == null || "".equals(property.getNamespaceURI()))
                property = new QName(ArtificerConstants.SRAMP_NS, property.getLocalPart());

            if (property.getNamespaceURI().equals(ArtificerConstants.SRAMP_NS)) {
                String jcrPropName;
                if (corePropertyMap.containsKey(property)) {
                    jcrPropName = corePropertyMap.get(property);
                } else {
                    jcrPropName = JCRConstants.SRAMP_PROPERTIES + ":" + property.getLocalPart();
                }
                propertyContext = jcrPropName;
            } else {
                throw new RuntimeException(Messages.i18n.format("XP_INVALID_PROPERTY_NS", property.getNamespaceURI()));
            }
        }
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.FunctionCall)
     */
    @Override
    public void visit(FunctionCall node) {
        if (ArtificerConstants.SRAMP_NS.equals(node.getFunctionName().getNamespaceURI())) {
            if (node.getFunctionName().equals(CLASSIFIED_BY_ALL_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY, false);
            } else if (node.getFunctionName().equals(CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY, true);
            } else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ALL_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_CLASSIFIED_BY, false);
            } else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_CLASSIFIED_BY, true);
            } else if (node.getFunctionName().equals(GET_RELATIONSHIP_ATTRIBUTE)) {
                String otherAttributeKey = reduceStringLiteralArgument(node.getArguments().get(1));
                // Ex. query: /s-ramp/wsdl/WsdlDocument[someRelationship[s-ramp:getRelationshipAttribute(., 'someAttribute') = 'true']]
                // Note that the predicate function needs to add a condition on the relationship selector itself, *not*
                // the artifact targeted by the relationship.
                singleUseSelectorContext = relationshipContext;
                propertyContext = JCRConstants.SRAMP_OTHER_ATTRIBUTES + ":" + otherAttributeKey;
            } else if (node.getFunctionName().equals(GET_TARGET_ATTRIBUTE)) {
                String otherAttributeKey = reduceStringLiteralArgument(node.getArguments().get(1));
                // Ex. query: /s-ramp/wsdl/WsdlDocument[someRelationship[s-ramp:getTargetAttribute(., 'someAttribute') = 'true']]
                // Note that the predicate function needs to add a condition on the relationship target selector itself, *not*
                // the artifact targeted by the relationship.
                singleUseSelectorContext = targetContext;
                propertyContext = JCRConstants.SRAMP_OTHER_ATTRIBUTES + ":" + otherAttributeKey;
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

            String pattern = reduceStringLiteralArgument(patternArg);

            if (isFullTextSearch(attributeArg)) {
                fullTextSearch(pattern);
            } else {
                pattern = pattern.replace(".*", "%"); // the only valid wildcard
                ForwardPropertyStep attribute = reducePropertyArgument(attributeArg);
                attribute.accept(this);
                operation(getSelectorContext(), propertyContext, QueryObjectModelConstants.JCR_OPERATOR_LIKE, pattern);
            }
        } else if (NOT.equals(node.getFunctionName())) {
            if (node.getArguments().size() != 1) {
                throw new RuntimeException(Messages.i18n.format("XP_NOT_FUNC_NUM_ARGS_ERROR", node.getArguments().size()));
            }

            Argument argument = node.getArguments().get(0);
            if (argument.getExpr() != null) {
                List<Constraint> oldConstraintsContext = constraintsContext;
                constraintsContext = new ArrayList<>();
                argument.getExpr().accept(this);
                // Should have resulted in only 1 constraint -- negate it and add to the original list.
                oldConstraintsContext.add(factory.not((constraintsContext.get(0))));
                constraintsContext = oldConstraintsContext;
            } else {
                // TODO: When would not() be given a literal?  That's what this implies.  As-is, it won't be negated...
                argument.accept(this);
            }
        } else {
            throw new RuntimeException(Messages.i18n.format("XP_FUNCTION_NOT_SUPPORTED", node.getFunctionName().toString()));
        }
    }

    private void visitClassifications(FunctionCall node, String propertyName, boolean isOr) {
        Collection<URI> classifications = resolveArgumentsToClassifications(node.getArguments());
        List<Constraint> oldConstraintsContext = constraintsContext;
        constraintsContext = new ArrayList<>();
        for (URI classification : classifications) {
            operation(getSelectorContext(), propertyName, QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, classification.toString());
        }
        if (isOr) {
            oldConstraintsContext.add(compileOr(constraintsContext));
        } else {
            oldConstraintsContext.add(compileAnd(constraintsContext));
        }
        constraintsContext = oldConstraintsContext;
    }

    /**
     * Resolves the list of arguments to a collection of classification URIs.
     * @param arguments
     */
    private Collection<URI> resolveArgumentsToClassifications(List<Argument> arguments) {
        Collection<String> classifiedBy = new HashSet<>();
        for (int idx = 1; idx < arguments.size(); idx++) {
            Argument arg = arguments.get(idx);
            if (arg.getPrimaryExpr() == null || arg.getPrimaryExpr().getLiteral() == null) {
                throw new RuntimeException(Messages.i18n.format("XP_INVALID_CLASSIFIER_FORMAT"));
            }
            classifiedBy.add(arg.getPrimaryExpr().getLiteral());
        }
        try {
            return this.classificationHelper.resolveAll(classifiedBy);
        } catch (ArtificerException e) {
            this.error = e;
            return Collections.emptySet();
        }
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.OrExpr)
     */
    @Override
    public void visit(OrExpr node) {
        if (node.getRight() == null) {
            node.getLeft().accept(this);
        } else {
            List<Constraint> oldConstraintsContext = constraintsContext;
            constraintsContext = new ArrayList<>();
            node.getLeft().accept(this);
            node.getRight().accept(this);
            oldConstraintsContext.add(compileOr(constraintsContext));
            constraintsContext = oldConstraintsContext;
        }
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.Predicate)
     */
    @Override
    public void visit(Predicate node) {
        node.getExpr().accept(this);
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.PrimaryExpr)
     */
    @Override
    public void visit(PrimaryExpr node) {
        try {
            if (node.getLiteral() != null) {
                if (JCRConstants.JCR_LAST_MODIFIED.equals(propertyContext) || JCRConstants.JCR_CREATED.equals(propertyContext)) {
                    Date date = null;
                    try {
                        date = SDF.parse(node.getLiteral());
                    } catch (ParseException e) {
                        error = new QueryExecutionException(e);
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    valueContext = session.getValueFactory().createValue(calendar);
                } else {
                    valueContext = session.getValueFactory().createValue(node.getLiteral());
                }
            } else if (node.getNumber() != null) {
                // TODO: may be an int?
                valueContext = session.getValueFactory().createValue(node.getNumber().doubleValue());
            } else if (node.getPropertyQName() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_PROPERTY_PRIMARY_EXPR_NOT_SUPPORTED"));
            }
        } catch (RepositoryException e) {
            error = new QueryExecutionException(e);
        }
    }

    @Override
    public void visit(RelationshipPath node) {
        joinChild("sramp:relationship", relationshipContext, relationshipContext, getSelectorContext(),
                QueryObjectModelConstants.JCR_JOIN_TYPE_INNER);
        if (targetContext != null) {
            joinChild("sramp:target", targetContext, targetContext, relationshipContext,
                    QueryObjectModelConstants.JCR_JOIN_TYPE_INNER);
        }
        operation(relationshipContext, "sramp:relationshipType", QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                node.getRelationshipType());
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.SubartifactSet)
     */
    @Override
    public void visit(SubartifactSet node) {
        if (node.getFunctionCall() != null) {
            node.getFunctionCall().accept(this);
        } else if (node.getRelationshipPath() != null) {
            // Relationship within a predicate
            visitRelationshipPredicate(node.getRelationshipPath(), node.getPredicate());

            if (node.getSubartifactSet() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_MULTILEVEL_SUBARTYSETS_NOT_SUPPORTED"));
            }
        }
    }

    private void visitRelationshipPredicate(RelationshipPath relationshipPath, Predicate predicate) {
        // NOTE: Relationships within a predicate are a bit hard to deal with since ModeShape doesn't support correlated
        // subqueries.  So, we use something like this:
        //
        // AND artifact1.[jcr:uuid] IN (
        //   SELECT artifact3.[jcr:uuid] AS uuid FROM [sramp:relationship] AS relationship1
        //   INNER JOIN [sramp:target] AS target1 ON ISCHILDNODE(target1,relationship1)
        //   INNER JOIN [sramp:baseArtifactType] AS artifact2 ON target1.[sramp:targetArtifact] = artifact2.[jcr:uuid]
        //   INNER JOIN [sramp:baseArtifactType] AS artifact3 ON ISCHILDNODE(relationship1,artifact3)
        //   WHERE <predicate constraints>)
        //
        // Essentially, we're creating a non-correlated subquery that selects relationships based on the predicate.
        // The predicate constraints are acting upon the artifact *targeted by* (!!!) the relationship.
        // We then map the results back to the primary query: artifact3 (the relationship's parent) == artifact1.

        // set up contexts for the subquery
        relationshipContext = newRelationshipAlias();
        List<Constraint> oldConstraintsContext = constraintsContext;
        constraintsContext = new ArrayList<>();
        Source oldSourceContext = sourceContext;
        sourceContext = factory.selector(JCRConstants.SRAMP_RELATIONSHIP, relationshipContext);

        // process the constraints on the relationship itself
        operation(relationshipContext, "sramp:relationshipType", QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                relationshipPath.getRelationshipType());

        // process the predicates, adding them as constraints to a subquery
        if (predicate != null) {
            // If there's a predicate, then we need to add in Target handling.
            String oldSelectorContext = selectorContext;
            selectorContext = newArtifactAlias();
            targetContext = newTargetAlias();

            predicate.accept(this);

            // join the target
            joinChild(JCRConstants.SRAMP_TARGET, targetContext, targetContext, relationshipContext,
                    QueryObjectModelConstants.JCR_JOIN_TYPE_INNER);
            // join the artifact targeted by the relationship
            joinEq(JCRConstants.SRAMP_BASE_ARTIFACT_TYPE, targetContext, JCRConstants.SRAMP_TARGET_ARTIFACT,
                    selectorContext, JCRConstants.JCR_UUID, QueryObjectModelConstants.JCR_JOIN_TYPE_INNER);

            targetContext = null;
            selectorContext = oldSelectorContext;
        }

        // Filter out the trash
        descendant(relationshipContext, JCRConstants.ROOT_PATH);

        // create the subquery
        // join the relationship's parent (an artifact)
        String parentArtifact = newArtifactAlias();
        joinChild(JCRConstants.SRAMP_BASE_ARTIFACT_TYPE, parentArtifact, relationshipContext, parentArtifact,
                QueryObjectModelConstants.JCR_JOIN_TYPE_INNER);
        Column[] columns = new Column[1];
        columns[0] = factory.column(parentArtifact, JCRConstants.JCR_UUID, "uuid");
        SelectQuery query = factory.select(sourceContext, compileAnd(constraintsContext),
                null, columns, null, false);

        // reset constraints
        sourceContext = oldSourceContext;
        constraintsContext = oldConstraintsContext;
        relationshipContext = null;

        // create the artifact.uuid IN (subquery) on the original constraints
        in(selectorContext, JCRConstants.JCR_UUID, query);
    }

    private String getSelectorContext() {
        try {
            return singleUseSelectorContext != null ? singleUseSelectorContext : selectorContext;
        } finally {
            singleUseSelectorContext = null;
        }
    }

    /**
     * Reduces an Argument subtree to the final {@link ForwardPropertyStep} that is it's (supposed)
     * final node.  This method will throw a runtime exception if it doesn't find the expected
     * {@link ForwardPropertyStep}.
     *
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
     * Returns true if the Argument subtree's final node is the full-text seach wildcard.
     * Example: /s-ramp[xp2:matches(., '.*foo.*')] The primary expression will be the xpath value (".") used for free-text
     * searches.
     *
     * @param argument
     */
    private boolean isFullTextSearch(Argument argument) {
        AbstractXPathNode node = argument.getExpr().getAndExpr().getLeft().getLeft().getLeft();
        if (node instanceof PrimaryExpr) {
            PrimaryExpr primaryExpr = (PrimaryExpr) node;
            return primaryExpr.getXpathValue().equals(".");
        }
        return false;
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

    private void joinEq(String nodeType, String leftSelectorName, String leftPropertyName,
                        String rightSelectorName, String rightPropertyName, String joinType) {
        Selector rightSelector = factory.selector(nodeType, rightSelectorName);
        JoinCondition condition =  factory.equiJoinCondition(
                leftSelectorName, leftPropertyName, rightSelectorName, rightPropertyName);
        // If this is the first join, convert the sourceContext into the Join itself.  If it's not, sourceContext is already
        // a Join and we should simply add to it.
        sourceContext = factory.join(sourceContext, rightSelector, joinType, condition);
    }

    private void joinChild(String nodeType, String selectorName, String childSelectorName, String parentSelectorName,
            String joinType) {
        Selector childSelector = factory.selector(nodeType, selectorName);
        JoinCondition condition = factory.childNodeJoinCondition(childSelectorName, parentSelectorName);
        // If this is the first join, convert the sourceContext into the Join itself.  If it's not, sourceContext is already
        // a Join and we should simply add to it.
        sourceContext = factory.join(sourceContext, childSelector, joinType, condition);
    }

    private void child(String childSelectorName, String parentSelectorName) {
        constraintsContext.add(factory.childNode(childSelectorName, parentSelectorName));
    }

    private void operation(String selectorName, String propertyName, String operator, String value) {
        operation(factory.propertyValue(selectorName, propertyName), operator, value);
    }

    private void operation(String selectorName, String propertyName, String operator, Value value) {
        operation(factory.propertyValue(selectorName, propertyName), operator, value);
    }

    private void operation(DynamicOperand operand, String operator, String value) {
        try {
            operation(operand, operator, session.getValueFactory().createValue(value));
        } catch (RepositoryException e) {
            error = new QueryExecutionException(e);
        }
    }

    private void operation(DynamicOperand operand, String operator, Value value) {
        try {
            constraintsContext.add(factory.comparison(operand, operator, factory.literal(value)));
        } catch (RepositoryException e) {
            error = new QueryExecutionException(e);
        }
    }

    private void fullTextSearch(String query) {
        try {
            Value queryValue = session.getValueFactory().createValue(query);

            // full text search: artifact metadata *or* the file content (if there is any)
            // note that file content is a nt:resource *child* of the primary node

            String contentSelector = "content1";
            if (!contentChildJoined) {
                joinChild(JCRConstants.NT_RESOURCE, contentSelector, contentSelector, selectorContext,
                        QueryObjectModelConstants.JCR_JOIN_TYPE_LEFT_OUTER);
                contentChildJoined = true;
            }

            Constraint metadataConstraint = factory.fullTextSearch(selectorContext, null, factory.literal(queryValue));
            Constraint contentConstraint = factory.fullTextSearch(contentSelector, null, factory.literal(queryValue));
            Constraint or = factory.or(metadataConstraint, contentConstraint);
            constraintsContext.add(or);


        } catch (RepositoryException e) {
            error = new QueryExecutionException(e);
        }
    }

    private void in(String selectorName, String propertyName, String... values) {
        try {
            StaticOperand[] literalValues = new StaticOperand[values.length];
            for (int i = 0; i < values.length; i++) {
                literalValues[i] = factory.literal((session.getValueFactory().createValue(values[i])));
            }
            constraintsContext.add(factory.in(factory.propertyValue(selectorName, propertyName), literalValues));
        } catch (RepositoryException e) {
            error = new QueryExecutionException(e);
        }
    }

    private void in(String selectorName, String propertyName, QueryCommand queryCommand) {
        Subquery subquery = factory.subquery(queryCommand);
        constraintsContext.add(factory.in(factory.propertyValue(selectorName, propertyName), subquery));
    }

    private void exists(String selectorName, String propertyName) {
        constraintsContext.add(factory.propertyExistence(selectorName, propertyName));
    }

    private void descendant(String selectorName, String ancestorPath) {
        constraintsContext.add(factory.descendantNode(selectorName, ancestorPath));
    }

    private Constraint compileAnd(List<Constraint> constraints) {
        if (constraints.size() == 0) {
            return null;
        } else if (constraints.size() == 1) {
            return constraints.get(0);
        } else {
            return factory.and(constraints.get(0), compileAnd(constraints.subList(1, constraints.size())));
        }
    }

    private Constraint compileOr(List<Constraint> constraints) {
        if (constraints.size() == 0) {
            return null;
        } else if (constraints.size() == 1) {
            return constraints.get(0);
        } else {
            return factory.or(constraints.get(0), compileOr(constraints.subList(1, constraints.size())));
        }
    }

    private String getJcrOperator(String operator) {
        if (Operator.EQUAL_TO.symbol().equalsIgnoreCase(operator)) return QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;
        if (Operator.GREATER_THAN.symbol().equalsIgnoreCase(operator)) return QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN;
        if (Operator.GREATER_THAN_OR_EQUAL_TO.symbol().equalsIgnoreCase(operator)) return QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO;
        if (Operator.LESS_THAN.symbol().equalsIgnoreCase(operator)) return QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN;
        if (Operator.LESS_THAN_OR_EQUAL_TO.symbol().equalsIgnoreCase(operator)) return QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO;
        if (Operator.LIKE.symbol().equalsIgnoreCase(operator)) return QueryObjectModelConstants.JCR_OPERATOR_LIKE;
        if (Operator.NOT_EQUAL_TO.symbol().equalsIgnoreCase(operator)) return QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO;
        return null;
    }

    private int uniqueArtifactCounter = 1;
    private int uniqueRelationshipCounter = 1;
    private int uniqueTargetCounter = 1;
    private String newArtifactAlias() {
        return "artifact" + uniqueArtifactCounter++;
    }
    private String newRelationshipAlias() {
        return "relationship" + uniqueRelationshipCounter++;
    }
    private String newTargetAlias() {
        return "target" + uniqueTargetCounter++;
    }

}