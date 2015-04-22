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
package org.artificer.repository.hibernate.query;

import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.ArtificerException;
import org.artificer.common.query.xpath.ast.AndExpr;
import org.artificer.common.query.xpath.ast.Argument;
import org.artificer.common.query.xpath.ast.EqualityExpr;
import org.artificer.common.query.xpath.ast.ForwardPropertyStep;
import org.artificer.common.query.xpath.ast.FunctionCall;
import org.artificer.common.query.xpath.ast.LocationPath;
import org.artificer.common.query.xpath.ast.OrExpr;
import org.artificer.common.query.xpath.ast.PrimaryExpr;
import org.artificer.common.query.xpath.ast.Query;
import org.artificer.common.query.xpath.ast.RelationshipPath;
import org.artificer.common.query.xpath.ast.SubartifactSet;
import org.artificer.repository.ClassificationHelper;
import org.artificer.repository.error.QueryExecutionException;
import org.artificer.repository.hibernate.data.HibernateEntityCreator;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerRelationship;
import org.artificer.repository.hibernate.entity.ArtificerTarget;
import org.artificer.repository.hibernate.i18n.Messages;
import org.artificer.repository.query.AbstractArtificerQueryVisitor;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.QueryBuilder;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import javax.xml.namespace.QName;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Visitor used to produce a JCR SQL2 query from an S-RAMP xpath query.
 *
 * @author Brett Meyer
 */
public class ArtificerToHibernateQueryVisitor extends AbstractArtificerQueryVisitor {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    private CriteriaQuery query = null;
    private From from = null;

    private From relationshipFrom = null;
    private From targetFrom = null;

    private Subquery customPropertySubquery = null;
    private Path customPropertyValuePath = null;
    private List<Predicate> customPropertyPredicates = null;

    private String propertyContext = null;
    private Object valueContext = null;

    private List<Predicate> predicates = new ArrayList<>();

    /**
     * Default constructor.
     * @param entityManager
     * @param classificationHelper
     */
    public ArtificerToHibernateQueryVisitor(EntityManager entityManager, ClassificationHelper classificationHelper) throws ArtificerException {
        super(classificationHelper);

        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "createdBy"), "createdBy.username");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "createdTimestamp"), "createdBy.lastActionTime");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "version"), "version");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "uuid"), "uuid");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "lastModifiedTimestamp"), "modifiedBy.lastActionTime");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "lastModifiedBy"), "modifiedBy.username");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "description"), "description");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "name"), "name");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "contentType"), "contentType");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "contentSize"), "contentSize");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "contentHash"), "contentHash");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "contentEncoding"), "contentEncoding");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "extendedType"), "extendedType");
        corePropertyMap.put(new QName(ArtificerConstants.SRAMP_NS, "derived"), "derived");

        this.entityManager = entityManager;
        criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    public List<ArtificerArtifact> query() throws ArtificerException {
        if (this.error != null) {
            throw this.error;
        }

        query.select(from).distinct(true);

        // filter out the trash
        predicates.add(criteriaBuilder.equal(from.get("trashed"), Boolean.valueOf(false)));

        // build the full set of constraints
        query.where(compileAnd(predicates));

        if (order != null) {
            if (orderAscending) {
                query.orderBy(criteriaBuilder.asc(path(order)));
            } else {
                query.orderBy(criteriaBuilder.desc(path(order)));
            }
        }

        return entityManager.createQuery(query).getResultList();
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.Query)
     */
    @Override
    public void visit(Query node) {
        this.error = null;

        query = criteriaBuilder.createQuery(ArtificerArtifact.class);
        from = query.from(ArtificerArtifact.class);

        node.getArtifactSet().accept(this);
        if (node.getPredicate() != null) {
            node.getPredicate().accept(this);
        }
        if (node.getSubartifactSet() != null) {
            SubartifactSet subartifactSet = node.getSubartifactSet();
            if (subartifactSet.getRelationshipPath() != null) {
                if (subartifactSet.getRelationshipPath().getRelationshipType().equalsIgnoreCase("relatedDocument")) {
                    // derivedFrom
                    from = from.join("derivedFrom");

                    // Now add any additional predicates included.
                    if (subartifactSet.getPredicate() != null) {
                        subartifactSet.getPredicate().accept(this);
                    }
                } else {
                    // JOIN on the relationship and targets
                    relationshipFrom = from.join("relationships");
                    targetFrom = relationshipFrom.join("targets");

                    from = relationshipFrom;

                    // process constraints on the relationship itself
                    subartifactSet.getRelationshipPath().accept(this);

                    // context now needs to be the relationship targets (permanently)
                    from = targetFrom.join("target");

                    // Now add any additional predicates included.
                    if (subartifactSet.getPredicate() != null) {
                        subartifactSet.getPredicate().accept(this);
                    }
                }
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
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.LocationPath)
     */
    @Override
    public void visit(LocationPath node) {
        if (node.getArtifactType() != null) {
            // If an explicit type is given, we need to override the root 'from' in order to give the correct entity class.
            // This is so that certain fields can be restricted to their respective entities, rather than cramming
            // all possible fields into ArtificerArtifact itself.
            ArtificerArtifact artifact;
            try {
                artifact = HibernateEntityCreator.visit(ArtifactType.valueOf(node.getArtifactType()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            query = criteriaBuilder.createQuery(artifact.getClass());
            from = query.from(artifact.getClass());

            eq("type", node.getArtifactType());
        } else if (node.getArtifactModel() != null) {
            eq("model", node.getArtifactModel());
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
            node.getLeft().accept(this);
            node.getRight().accept(this);
            Predicate predicate1 = predicates.remove(predicates.size() - 1);
            Predicate predicate2 = predicates.remove(predicates.size() - 1);
            predicates.add(criteriaBuilder.and(predicate1, predicate2));
        }
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
            if (customPropertySubquery != null) {
                customPropertySubquery.where(compileAnd(customPropertyPredicates));
            } else if (propertyContext != null) {
                exists(propertyContext);
            }
        } else {
            node.getLeft().accept(this);
            node.getRight().accept(this);

            if (customPropertySubquery != null) {
                customPropertyPredicates.add(criteriaBuilder.equal(customPropertyValuePath, valueContext));
                customPropertySubquery.where(compileAnd(customPropertyPredicates));
            } else if (propertyContext != null) {
                // TODO: Not guaranteed to be propertyContext -- may be function, etc.
                operation(node.getOperator().symbol(), propertyContext, valueContext);
            }

            valueContext = null;
        }
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
                if (corePropertyMap.containsKey(property)) {
                    propertyContext = corePropertyMap.get(property);
                    customPropertySubquery = null;
                } else {
                    // Note: Typically, you'd expect to see a really simple MapJoin w/ key and value predicates.
                    // However, *negation* ("not()") is needed and is tricky when just using a join.  Instead, use
                    // an "a1.id in (select a2.id from ArtificerArtifact a2 [map join and predicates)" -- easily negated.

                    customPropertySubquery = query.subquery(ArtificerArtifact.class);
                    From customPropertyFrom = customPropertySubquery.from(ArtificerArtifact.class);
                    Join customPropertyJoin = customPropertyFrom.join("properties");
                    customPropertySubquery.select(customPropertyFrom.get("id"));
                    customPropertyPredicates = new ArrayList<>();
                    customPropertyPredicates.add(criteriaBuilder.equal(customPropertyFrom.get("id"), from.get("id")));
                    customPropertyPredicates.add(criteriaBuilder.equal(customPropertyJoin.get("key"), property.getLocalPart()));
                    customPropertyValuePath = customPropertyJoin.get("value");
                    predicates.add(criteriaBuilder.exists(customPropertySubquery));
                    propertyContext = null;
                }
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
                visitClassifications(node, false, true);
            } else if (node.getFunctionName().equals(CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, true, true);
            } else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ALL_OF)) {
                visitClassifications(node, false, false);
            } else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, true, false);
            } else if (node.getFunctionName().equals(GET_RELATIONSHIP_ATTRIBUTE)) {
                String otherAttributeKey = reduceStringLiteralArgument(node.getArguments().get(1));
                // Ex. query: /s-ramp/wsdl/WsdlDocument[someRelationship[s-ramp:getRelationshipAttribute(., 'someAttribute') = 'true']]
                // Note that the predicate function needs to add a condition on the relationship selector itself, *not*
                // the artifact targeted by the relationship.
                customPropertySubquery = query.subquery(ArtificerRelationship.class);
                From customPropertyFrom = customPropertySubquery.from(ArtificerRelationship.class);
                MapJoin customPropertyJoin = customPropertyFrom.joinMap("otherAttributes");
                customPropertySubquery.select(customPropertyFrom.get("id"));
                customPropertyPredicates = new ArrayList<>();
                customPropertyPredicates.add(criteriaBuilder.equal(customPropertyFrom.get("id"), relationshipFrom.get("id")));
                customPropertyPredicates.add(criteriaBuilder.equal(customPropertyJoin.key(), otherAttributeKey));
                customPropertyValuePath = customPropertyJoin.value();
                predicates.add(criteriaBuilder.exists(customPropertySubquery));
                propertyContext = null;
            } else if (node.getFunctionName().equals(GET_TARGET_ATTRIBUTE)) {
                String otherAttributeKey = reduceStringLiteralArgument(node.getArguments().get(1));
                // Ex. query: /s-ramp/wsdl/WsdlDocument[someRelationship[s-ramp:getTargetAttribute(., 'someAttribute') = 'true']]
                // Note that the predicate function needs to add a condition on the relationship target selector itself, *not*
                // the artifact targeted by the relationship.
                customPropertySubquery = query.subquery(ArtificerTarget.class);
                From customPropertyFrom = customPropertySubquery.from(ArtificerTarget.class);
                MapJoin customPropertyJoin = customPropertyFrom.joinMap("otherAttributes");
                customPropertySubquery.select(customPropertyFrom.get("id"));
                customPropertyPredicates = new ArrayList<>();
                customPropertyPredicates.add(criteriaBuilder.equal(customPropertyFrom.get("id"), targetFrom.get("id")));
                customPropertyPredicates.add(criteriaBuilder.equal(customPropertyJoin.key(), otherAttributeKey));
                customPropertyValuePath = customPropertyJoin.value();
                predicates.add(criteriaBuilder.exists(customPropertySubquery));
                propertyContext = null;
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
                like(propertyContext, pattern);
            }
        } else if (NOT.equals(node.getFunctionName())) {
            if (node.getArguments().size() != 1) {
                throw new RuntimeException(Messages.i18n.format("XP_NOT_FUNC_NUM_ARGS_ERROR", node.getArguments().size()));
            }

            Argument argument = node.getArguments().get(0);
            if (argument.getExpr() != null) {
                argument.getExpr().accept(this);
                // Should have resulted in only 1 constraint -- negate it and re-add
                Predicate predicate = predicates.remove(predicates.size() - 1);
                predicates.add(criteriaBuilder.not(predicate));
            } else {
                // TODO: When would not() be given a literal?  That's what this implies.  As-is, it won't be negated...
                argument.accept(this);
            }
        } else {
            throw new RuntimeException(Messages.i18n.format("XP_FUNCTION_NOT_SUPPORTED", node.getFunctionName().toString()));
        }
    }

    private void visitClassifications(FunctionCall node, boolean isOr, boolean allowSubtypes) {
        Collection<URI> classifications = resolveArgumentsToClassifications(node.getArguments());

        Path classifierPath;
        if (allowSubtypes) {
            classifierPath = from.get("normalizedClassifiers");
        } else {
            classifierPath = from.get("classifiers");
        }

        List<Predicate> classifierConstraints = new ArrayList<>();
        for (URI classification : classifications) {
            classifierConstraints.add(criteriaBuilder.isMember(classification.toString(), classifierPath));
        }

        if (isOr) {
            predicates.add(compileOr(classifierConstraints));
        } else {
            predicates.add(compileAnd(classifierConstraints));
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
            node.getLeft().accept(this);
            node.getRight().accept(this);
            Predicate predicate1 = predicates.remove(predicates.size() - 1);
            Predicate predicate2 = predicates.remove(predicates.size() - 1);
            predicates.add(criteriaBuilder.or(predicate1, predicate2));
        }
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.PrimaryExpr)
     */
    @Override
    public void visit(PrimaryExpr node) {
        if (node.getLiteral() != null) {
            if (propertyContext != null && propertyContext.contains("lastActionTime")) {
                Date date = null;
                try {
                    date = SDF.parse(node.getLiteral());
                } catch (ParseException e) {
                    error = new QueryExecutionException(e);
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                valueContext = calendar;
            } else if ("true".equalsIgnoreCase(node.getLiteral())) {
                valueContext = true;
            } else if ("false".equalsIgnoreCase(node.getLiteral())) {
                valueContext = false;
            } else {
                valueContext = node.getLiteral();
            }
        } else if (node.getNumber() != null) {
            // TODO: may be an int?
            valueContext = node.getNumber().doubleValue();
        } else if (node.getPropertyQName() != null) {
            throw new RuntimeException(Messages.i18n.format("XP_PROPERTY_PRIMARY_EXPR_NOT_SUPPORTED"));
        }
    }

    @Override
    public void visit(RelationshipPath node) {
        eq("name", node.getRelationshipType());
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.SubartifactSet)
     */
    @Override
    public void visit(SubartifactSet node) {
        if (node.getFunctionCall() != null) {
            node.getFunctionCall().accept(this);
        } else if (node.getRelationshipPath() != null) {
            From oldRootContext = from;

            if (node.getRelationshipPath().getRelationshipType().equalsIgnoreCase("relatedDocument")) {
                // derivedFrom
                from = from.join("derivedFrom", JoinType.LEFT);

                // Now add any additional predicates included.
                if (node.getPredicate() != null) {
                    node.getPredicate().accept(this);
                }
            } else {
                // Relationship within a predicate.
                // Create a subquery and 'exists' conditional.  The subquery is much easier to handle, later on, if this
                // predicate is negated, as opposed to removing the inner join or messing with left joins.

                List<Predicate> oldPredicates = predicates;
                predicates = new ArrayList<>();

                Subquery relationshipSubquery = query.subquery(ArtificerRelationship.class);
                relationshipFrom = relationshipSubquery.from(ArtificerRelationship.class);
                targetFrom = relationshipFrom.join("targets");
                relationshipSubquery.select(relationshipFrom.get("id"));

                Join relationshipOwnerJoin = relationshipFrom.join("owner");
                predicates.add(criteriaBuilder.equal(relationshipOwnerJoin.get("id"), oldRootContext.get("id")));

                from = relationshipFrom;

                // process constraints on the relationship itself
                node.getRelationshipPath().accept(this);

                // context now needs to be the relationship targets

                from = targetFrom.join("target");

                // Now add any additional predicates included.
                if (node.getPredicate() != null) {
                    node.getPredicate().accept(this);
                }

                // Add predicates to subquery
                relationshipSubquery.where(compileAnd(predicates));

                predicates = oldPredicates;

                // Add 'exists' predicate (using subquery) to original list
                predicates.add(criteriaBuilder.exists(relationshipSubquery));
            }

            // restore the original selector (since the relationship was in a predicate, not a path)
            from = oldRootContext;

            if (node.getSubartifactSet() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_MULTILEVEL_SUBARTYSETS_NOT_SUPPORTED"));
            }
        }
    }

    private void eq(String propertyName, Object value) {
        if (value == true) {
            predicates.add(criteriaBuilder.isTrue(path(propertyName)));
        } else if (value == false) {
            predicates.add(criteriaBuilder.isFalse(path(propertyName)));
        } else {
            predicates.add(criteriaBuilder.equal(path(propertyName), value));
        }
    }

    private void gt(String propertyName, Object value) {
        if (value instanceof Date) {
            predicates.add(criteriaBuilder.greaterThan(path(propertyName), (Date) value));
        } else if (value instanceof Calendar) {
            predicates.add(criteriaBuilder.greaterThan(path(propertyName), (Calendar) value));
        } else if (value instanceof Number) {
            predicates.add(criteriaBuilder.gt(path(propertyName), (Number) value));
        }
    }

    private void ge(String propertyName, Object value) {
        if (value instanceof Date) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path(propertyName), (Date) value));
        } else if (value instanceof Calendar) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path(propertyName), (Calendar) value));
        } else if (value instanceof Number) {
            predicates.add(criteriaBuilder.ge(path(propertyName), (Number) value));
        }
    }

    private void lt(String propertyName, Object value) {
        if (value instanceof Date) {
            predicates.add(criteriaBuilder.lessThan(path(propertyName), (Date) value));
        } else if (value instanceof Calendar) {
            predicates.add(criteriaBuilder.lessThan(path(propertyName), (Calendar) value));
        } else if (value instanceof Number) {
            predicates.add(criteriaBuilder.lt(path(propertyName), (Number) value));
        }
    }

    private void le(String propertyName, Object value) {
        if (value instanceof Date) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(path(propertyName), (Date) value));
        } else if (value instanceof Calendar) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(path(propertyName), (Calendar) value));
        } else if (value instanceof Number) {
            predicates.add(criteriaBuilder.le(path(propertyName), (Number) value));
        }
    }

    private void like(String propertyName, Object value) {
        predicates.add(criteriaBuilder.like(path(propertyName), (String) value));
    }

    private void ne(String propertyName, Object value) {
        predicates.add(criteriaBuilder.notEqual(path(propertyName), value));
    }

    private void operation(String operator, String propertyName, Object value) {
        if ("=".equalsIgnoreCase(operator)) eq(propertyName, value);
        else if (">".equalsIgnoreCase(operator)) gt(propertyName, value);
        else if (">=".equalsIgnoreCase(operator)) ge(propertyName, value);
        else if ("<".equalsIgnoreCase(operator)) lt(propertyName, value);
        else if ("<=".equalsIgnoreCase(operator)) le(propertyName, value);
        else if ("like".equalsIgnoreCase(operator)) like(propertyName, value);
        else if ("<>".equalsIgnoreCase(operator)) ne(propertyName, value);
    }

    private void fullTextSearch(String query) {
        // TODO: It would be more performant to delay this until execution.  Add additional fields to the index and
        // attempt to limit the full-text search's results.  For instance, if the query starts with
        // /s-ramp/xsd/XsdDocument, we could index 'model' and 'type'.  Then, match those indexes using the model/type,
        // helping to reduce the size of the eventual list of IDs.  The same is probably true for other types of predicates.

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(
                entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ArtificerArtifact.class).get();
        org.apache.lucene.search.Query luceneQuery = qb
                .keyword()
                .onFields("description", "name", "comments.text", "properties.key", "properties.value")
                .andField("content").ignoreFieldBridge()
                .andField("contentPath").ignoreFieldBridge()
                .matching(query)
                .createQuery();
        FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(
                luceneQuery, ArtificerArtifact.class);
        fullTextQuery.setProjection("id");
        List<Object[]> results = fullTextQuery.getResultList();

        // There is not currently a way to combine JPA Criteria Queries with Hibernate Search Queries.  Until then,
        // we need to build up a list of the full-text result IDs.  That list is then used as a "artifact.id IN ([list])"
        // predicate.
        // Note that some databases (Oracle especially) limit the number of elements in an "in" expression.  Even if
        // it's restricted, they typically allow for at least 1000 elements.  Just to be safe (and maintain
        // portability), break the expressions up into 1000-element chunks.

        for (int i = 0; i < results.size(); i += 1000) {
            List<Object[]> subResults;
            if (results.size() > i + 1000) {
                subResults = results.subList(i, i + 1000 - 1);
            } else {
                subResults = results;
            }
            Long[] ids = new Long[subResults.size()];
            for (int j = 0; j < subResults.size(); j++) {
                Object[] result = subResults.get(j);
                ids[j] = (Long) result[0];
            }
            predicates.add(from.get("id").in(ids));
        }
    }

    private void exists(String propertyName) {
        predicates.add(criteriaBuilder.isNotNull(path(propertyName)));
    }

    private Predicate compileAnd(List<Predicate> constraints) {
        if (constraints.size() == 0) {
            return null;
        } else if (constraints.size() == 1) {
            return constraints.get(0);
        } else {
            return criteriaBuilder.and(constraints.get(0), compileAnd(constraints.subList(1, constraints.size())));
        }
    }

    private Predicate compileOr(List<Predicate> constraints) {
        if (constraints.size() == 0) {
            return null;
        } else if (constraints.size() == 1) {
            return constraints.get(0);
        } else {
            return criteriaBuilder.or(constraints.get(0), compileOr(constraints.subList(1, constraints.size())));
        }
    }

    public Path path(String propertyName) {
        if (propertyName.contains(".")) {
            // The propertyName is a path.  Example: createdBy.username, where 'createdBy' is an @Embedded User.
            // Needs to become from.get("createdBy").get("username").
            String[] split = propertyName.split("\\.");
            Path path = from.get(split[0]);
            if (split.length > 1) {
                for (int i = 1; i < split.length; i++) {
                    path = path.get(split[i]);
                }
            }
            return path;
        } else {
            return from.get(propertyName);
        }
    }

}