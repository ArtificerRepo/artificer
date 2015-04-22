/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.repository.query;

import org.artificer.common.ArtificerConstants;
import org.artificer.common.ArtificerException;
import org.artificer.common.query.xpath.ast.AbstractXPathNode;
import org.artificer.common.query.xpath.ast.Argument;
import org.artificer.common.query.xpath.ast.Expr;
import org.artificer.common.query.xpath.ast.ForwardPropertyStep;
import org.artificer.common.query.xpath.ast.Predicate;
import org.artificer.common.query.xpath.ast.PrimaryExpr;
import org.artificer.common.query.xpath.visitors.XPathVisitor;
import org.artificer.repository.ClassificationHelper;
import org.artificer.repository.i18n.Messages;

import javax.xml.namespace.QName;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Brett Meyer.
 */
public abstract class AbstractArtificerQueryVisitor implements XPathVisitor {

    protected static final QName CLASSIFIED_BY_ANY_OF = new QName(ArtificerConstants.SRAMP_NS, "classifiedByAnyOf");
    protected static final QName CLASSIFIED_BY_ALL_OF = new QName(ArtificerConstants.SRAMP_NS, "classifiedByAllOf");
    protected static final QName EXACTLY_CLASSIFIED_BY_ANY_OF = new QName(ArtificerConstants.SRAMP_NS, "exactlyClassifiedByAnyOf");
    protected static final QName EXACTLY_CLASSIFIED_BY_ALL_OF = new QName(ArtificerConstants.SRAMP_NS, "exactlyClassifiedByAllOf");

    protected static final QName GET_RELATIONSHIP_ATTRIBUTE = new QName(ArtificerConstants.SRAMP_NS, "getRelationshipAttribute");
    protected static final QName GET_TARGET_ATTRIBUTE = new QName(ArtificerConstants.SRAMP_NS, "getTargetAttribute");

    protected static final QName MATCHES = new QName("http://www.w3.org/2005/xpath-functions", "matches");
    protected static final QName NOT = new QName("http://www.w3.org/2005/xpath-functions", "not");

    protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    protected final Map<QName, String> corePropertyMap = new HashMap<>();

    protected String order;
    protected boolean orderAscending;

    protected ClassificationHelper classificationHelper;

    protected ArtificerException error;

    protected AbstractArtificerQueryVisitor(ClassificationHelper classificationHelper) {
        this.classificationHelper = classificationHelper;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void setOrderAscending(boolean orderAscending) {
        this.orderAscending = orderAscending;
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
    public void visit(org.artificer.common.query.xpath.ast.ArtifactSet node) {
        node.getLocationPath().accept(this);
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.Expr)
     */
    @Override
    public void visit(Expr node) {
        node.getAndExpr().accept(this);
    }

    /**
     * @see org.artificer.common.query.xpath.visitors.XPathVisitor#visit(org.artificer.common.query.xpath.ast.Predicate)
     */
    @Override
    public void visit(Predicate node) {
        node.getExpr().accept(this);
    }

    /**
     * Resolves the list of arguments to a collection of classification URIs.
     * @param arguments
     */
    protected Collection<URI> resolveArgumentsToClassifications(List<Argument> arguments) {
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
     * Reduces an Argument subtree to the final {@link org.artificer.common.query.xpath.ast.ForwardPropertyStep} that is it's (supposed)
     * final node.  This method will throw a runtime exception if it doesn't find the expected
     * {@link org.artificer.common.query.xpath.ast.ForwardPropertyStep}.
     *
     * @param argument
     */
    protected ForwardPropertyStep reducePropertyArgument(Argument argument) {
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
    protected boolean isFullTextSearch(Argument argument) {
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
    protected String reduceStringLiteralArgument(Argument argument) {
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
    protected String escapeStringLiteral(String literal) {
        return literal.replace("'", "''");
    }

    private int uniqueArtifactCounter = 1;
    private int uniqueRelationshipCounter = 1;
    private int uniqueTargetCounter = 1;
    protected String newArtifactAlias() {
        return "artifact" + uniqueArtifactCounter++;
    }
    protected String newRelationshipAlias() {
        return "relationship" + uniqueRelationshipCounter++;
    }
    protected String newTargetAlias() {
        return "target" + uniqueTargetCounter++;
    }
}
