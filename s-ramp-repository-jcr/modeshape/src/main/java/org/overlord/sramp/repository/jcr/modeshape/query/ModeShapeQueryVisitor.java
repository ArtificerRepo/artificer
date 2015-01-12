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
package org.overlord.sramp.repository.jcr.modeshape.query;

import org.modeshape.jcr.api.query.qom.*;
import org.modeshape.jcr.query.model.Column;
import org.modeshape.jcr.query.model.DynamicOperand;
import org.modeshape.jcr.query.model.Join;
import org.modeshape.jcr.query.model.JoinCondition;
import org.modeshape.jcr.query.model.Ordering;
import org.modeshape.jcr.query.model.QueryObjectModel;
import org.modeshape.jcr.query.model.QueryObjectModelFactory;
import org.modeshape.jcr.query.model.Selector;
import org.modeshape.jcr.query.model.Source;
import org.modeshape.jcr.query.model.StaticOperand;
import org.overlord.sramp.common.*;
import org.overlord.sramp.common.query.xpath.ast.*;
import org.overlord.sramp.common.query.xpath.ast.ArtifactSet;
import org.overlord.sramp.common.query.xpath.ast.Query;
import org.overlord.sramp.repository.jcr.*;
import org.overlord.sramp.repository.jcr.i18n.*;
import org.overlord.sramp.repository.jcr.query.*;
import org.overlord.sramp.repository.query.*;

import javax.jcr.*;
import javax.jcr.query.*;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.xml.namespace.*;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 * Visitor used to produce a JCR SQL2 query rootFrom an S-RAMP xpath query.
 *
 * @author eric.wittmann@redhat.com
 * @author Brett Meyer
 */
public class ModeShapeQueryVisitor implements SrampToJcrSql2QueryVisitor {

    private static final QName CLASSIFIED_BY_ANY_OF = new QName(SrampConstants.SRAMP_NS, "classifiedByAnyOf");
    private static final QName CLASSIFIED_BY_ALL_OF = new QName(SrampConstants.SRAMP_NS, "classifiedByAllOf");
    private static final QName EXACTLY_CLASSIFIED_BY_ANY_OF = new QName(SrampConstants.SRAMP_NS, "exactlyClassifiedByAnyOf");
    private static final QName EXACTLY_CLASSIFIED_BY_ALL_OF = new QName(SrampConstants.SRAMP_NS, "exactlyClassifiedByAllOf");

    public static final QName GET_RELATIONSHIP_ATTRIBUTE = new QName(SrampConstants.SRAMP_NS, "getRelationshipAttribute");

    private static final QName MATCHES = new QName("http://www.w3.org/2005/xpath-functions", "matches");
    private static final QName NOT = new QName("http://www.w3.org/2005/xpath-functions", "not");

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

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

    private Session session;
    private QueryManager queryManager;
    // Note: We're relying on a few ModeShape-specific extensions, such as "in", subselects, sets, etc.  Technically,
    // we could use the JCR QueryObjectModelFactory for most of the visitor.  But we're using the ModeShape version
    // for simplicity.
    private QueryObjectModelFactory factory;

    private Source rootSource;
    private Selector rootFrom;

    private String order;
    private boolean orderAscending;

    private List<Constraint> rootConstraints = new ArrayList<Constraint>();
    private List<Constraint> constraintsContext = rootConstraints;

    private ClassificationHelper classificationHelper;

    private String selectorContext = null;
    private String propertyContext = null;
    private String relationshipContext = null;
    private String targetContext = null;
    private Value valueContext = null;

    private String singleUseSelectorContext = null;

    private int uniqueAliasCounter = 1;
    private SrampException error;

    /**
     * Default constructor.
     * @param session
     * @param classificationHelper
     */
    public ModeShapeQueryVisitor(Session session, ClassificationHelper classificationHelper) throws SrampException {
        this.session = session;
        try {
            this.queryManager = session.getWorkspace().getQueryManager();
            factory = (QueryObjectModelFactory) queryManager.getQOMFactory();
        } catch (RepositoryException e) {
            throw new QueryExecutionException(e);
        }
        this.classificationHelper = classificationHelper;
    }

    @Override
    public void setOrder(String order) {
        this.order = order;
    }

    @Override
    public void setOrderAscending(boolean orderAscending) {
        this.orderAscending = orderAscending;
    }

    @Override
    public javax.jcr.query.Query buildQuery() throws SrampException {
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
        QueryObjectModel query = factory.createQuery(rootSource, compileAnd(rootConstraints), orderings, columns);
        return query;
    }

    /**
     * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.Query)
     */
    @Override
    public void visit(Query node) {
        this.error = null;
        String selectAlias = newArtifactAlias();
        this.selectorContext = selectAlias;

        rootFrom = factory.selector("sramp:baseArtifactType", selectAlias);
        rootSource = rootFrom;

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
                this.relationshipContext = oldRelationshipPredicateContext;
                this.targetContext = oldTargetPredicateContext;

                // Now add another JOIN back around on the "artifact table"
                joinEq("sramp:baseArtifactType", targetAlias, "sramp:targetArtifact", artifactAlias, "jcr:uuid");

                // Now add any additional predicates included.
                if (subartifactSet.getPredicate() != null) {
                    String oldArtifactPredicateContext = this.selectorContext;
                    this.selectorContext = artifactAlias;
                    subartifactSet.getPredicate().accept(this);
                    this.selectorContext = oldArtifactPredicateContext;
                }
            }
            if (subartifactSet.getFunctionCall() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_SUBARTIFACTSET_NOT_SUPPORTED"));
            }
            if (subartifactSet.getSubartifactSet() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_TOPLEVEL_SUBARTIFACTSET_ONLY"));
            }
        }

        // Filter out the trash
        descendant("sramp:baseArtifactType", JCRConstants.ROOT_PATH);
    }

    /**
     * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.LocationPath)
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
     * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.AndExpr)
     */
    @Override
    public void visit(AndExpr node) {
        if (node.getRight() == null) {
            node.getLeft().accept(this);
        } else {
            List<Constraint> oldConstraintsContext = constraintsContext;
            constraintsContext = new ArrayList<Constraint>();
            node.getLeft().accept(this);
            node.getRight().accept(this);
            oldConstraintsContext.add(compileAnd(constraintsContext));
            constraintsContext = oldConstraintsContext;
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
     * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.FunctionCall)
     */
    @Override
    public void visit(FunctionCall node) {
        if (SrampConstants.SRAMP_NS.equals(node.getFunctionName().getNamespaceURI())) {
            if (node.getFunctionName().equals(CLASSIFIED_BY_ALL_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY, false);
            } else if (node.getFunctionName().equals(CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY, true);
            } else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ALL_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_CLASSIFIED_BY, false);
            } else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, JCRConstants.SRAMP_CLASSIFIED_BY, true);
            } else if (node.getFunctionName().equals(GET_RELATIONSHIP_ATTRIBUTE)) {
                if (node.getArguments().size() != 2) {
                    // TODO: throw?
                }
                String otherAttributeKey = reduceStringLiteralArgument(node.getArguments().get(1));
                // Ex. query: /s-ramp/wsdl/WsdlDocument[someRelationship[s-ramp:getRelationshipAttribute(., 'someAttribute') = 'true']]
                // Note that the predicate function needs to add a condition on the relationship selector itself, *not*
                // the artifact targeted by the relationship.
                singleUseSelectorContext = relationshipContext;
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

            ForwardPropertyStep attribute = reducePropertyArgument(attributeArg);
            String pattern = reduceStringLiteralArgument(patternArg);
            pattern = pattern.replace(".*", "%"); // the only valid wildcard

            attribute.accept(this);
            operation(getSelectorContext(), propertyContext, QueryObjectModelConstants.JCR_OPERATOR_LIKE, pattern);
        } else if (NOT.equals(node.getFunctionName())) {
            if (node.getArguments().size() != 1) {
                throw new RuntimeException(Messages.i18n.format("XP_NOT_FUNC_NUM_ARGS_ERROR", node.getArguments().size()));
            }

            Argument argument = node.getArguments().get(0);
            if (argument.getExpr() != null) {
                List<Constraint> oldConstraintsContext = constraintsContext;
                constraintsContext = new ArrayList<Constraint>();
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
        constraintsContext = new ArrayList<Constraint>();
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
            List<Constraint> oldConstraintsContext = constraintsContext;
            constraintsContext = new ArrayList<Constraint>();
            node.getLeft().accept(this);
            node.getRight().accept(this);
            oldConstraintsContext.add(compileOr(constraintsContext));
            constraintsContext = oldConstraintsContext;
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

    /**
     * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.RelationshipPath)
     */
    @Override
    public void visit(RelationshipPath node) {
        joinChild("sramp:relationship", relationshipContext, getSelectorContext());
        if (targetContext != null) {
            joinChild("sramp:target", targetContext, relationshipContext);
        }
        operation(relationshipContext, "sramp:relationshipType", QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, node.getRelationshipType());
    }

    /**
     * @see org.overlord.sramp.common.query.xpath.visitors.XPathVisitor#visit(org.overlord.sramp.common.query.xpath.ast.SubartifactSet)
     */
    @Override
    public void visit(SubartifactSet node) {
        if (node.getFunctionCall() != null) {
            node.getFunctionCall().accept(this);
        } else if (node.getRelationshipPath() != null) {
            String relationshipAlias = newRelationshipAlias();
            String oldRelationshipPredicateContext = this.relationshipContext;
            this.relationshipContext = relationshipAlias;
            // TODO: Incredibly hacky.  Should not create the sramp:target JOIN in visit(RelationshipPath) unless
            // the target is actually needed below.
            String targetAlias = node.getPredicate() != null ? newTargetAlias() : null;
            String oldTargetPredicateContext = this.targetContext;
            this.targetContext = targetAlias;
            node.getRelationshipPath().accept(this);
            if (node.getPredicate() != null) {
                String artifactAlias = newArtifactAlias();

                joinEq("sramp:baseArtifactType", targetAlias, "sramp:targetArtifact", artifactAlias, "jcr:uuid");

                String oldArtifactPredicateContext = this.selectorContext;
                this.selectorContext = artifactAlias;
                node.getPredicate().accept(this);
                this.selectorContext = oldArtifactPredicateContext;
            }

            this.relationshipContext = oldRelationshipPredicateContext;
            this.targetContext = oldTargetPredicateContext;

            if (node.getSubartifactSet() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_MULTILEVEL_SUBARTYSETS_NOT_SUPPORTED"));
            }
        }
    }

    /**
     * @return a new (unused) alias for a relationship (typically used in JOINs)
     */
    private String newRelationshipAlias() {
        return "relationship" + uniqueAliasCounter++;
    }

    /**
     * @return a new (unused) alias for a target (typically used in JOINs)
     */
    private String newTargetAlias() {
        return "target" + uniqueAliasCounter++;
    }

    /**
     * @return a new (unused) alias for an artifact (typically used in JOINs)
     */
    private String newArtifactAlias() {
        return "artifact" + uniqueAliasCounter++;
    }

    /**
     * @return a new (unused) alias for a column
     */
    private String newColumnAlias() {
        return "column" + uniqueAliasCounter++;
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

    private void joinEq(String nodeType, String leftSelectorName, String leftPropertyName,
            String rightSelectorName, String rightPropertyName) {
        Selector rightSelector = factory.selector(nodeType, rightSelectorName);
        JoinCondition condition =  factory.equiJoinCondition(
                leftSelectorName, leftPropertyName, rightSelectorName, rightPropertyName);
        // If this is the first join, convert the rootSource into the Join itself.  If it's not, rootSource is already
        // a Join and we should simply add to it.
        if (rootSource instanceof Join) {
            rootSource = factory.join(rootSource, rightSelector, QueryObjectModelConstants.JCR_JOIN_TYPE_INNER, condition);
        } else {
            rootSource = factory.join(rootFrom, rightSelector, QueryObjectModelConstants.JCR_JOIN_TYPE_INNER, condition);
        }
    }

    private void joinChild(String nodeType, String childSelectorName, String parentSelectorName) {
        Selector childSelector = factory.selector(nodeType, childSelectorName);
        JoinCondition condition = factory.childNodeJoinCondition(childSelectorName, parentSelectorName);
        // If this is the first join, convert the rootSource into the Join itself.  If it's not, rootSource is already
        // a Join and we should simply add to it.
        if (rootSource instanceof Join) {
            rootSource = factory.join(rootSource, childSelector, QueryObjectModelConstants.JCR_JOIN_TYPE_INNER, condition);
        } else {
            rootSource = factory.join(rootFrom, childSelector, QueryObjectModelConstants.JCR_JOIN_TYPE_INNER, condition);
        }
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

}
