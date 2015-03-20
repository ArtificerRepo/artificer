/*
 * ModeShape (http://www.modeshape.org)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of
 * individual contributors.
 *
 * ModeShape is free software. Unless otherwise indicated, all code in ModeShape
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * ModeShape is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.artificer.common.query.xpath;

import org.artificer.common.ArtifactType;
import org.artificer.common.i18n.Messages;
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
import org.artificer.common.ArtificerConstants;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses an XPath query string and creates an abstract syntax tree representation. The supported grammar is
 * defined by the S-RAMP specification and is a subset of the XPath 2.0 grammar.
 */
public class XPathParser {

	private NamespaceContext namespaceContext;
	private String defaultPrefix;

    /**
     * Default constructor.
     */
    public XPathParser() {
    	setNamespaceContext(new DefaultNamespaceContext());
    	setDefaultPrefix(ArtificerConstants.SRAMP_PREFIX);
    }

	/**
	 * @return the namespaceContext
	 */
	public NamespaceContext getNamespaceContext() {
		return namespaceContext;
	}

	/**
	 * @param namespaceContext the namespaceContext to set
	 */
	public void setNamespaceContext(NamespaceContext namespaceContext) {
		this.namespaceContext = namespaceContext;
	}

	/**
	 * @return the defaultPrefix
	 */
	public String getDefaultPrefix() {
		return defaultPrefix;
	}

	/**
	 * @param defaultPrefix the defaultPrefix to set
	 */
	public void setDefaultPrefix(String defaultPrefix) {
		this.defaultPrefix = defaultPrefix;
	}

    /**
     * Called to parse the XPath query into an S-RAMP XPath AST.
     * @param xpath the S-RAMP Query being parsed
     * @return an S-RAMP XPath AST
     */
	public Query parseXPath(String xpath) {
		XPathTokenizer tokenizer = new XPathTokenizer();
		try {
			TokenStream tokens = tokenizer.tokenize(xpath);
			return parseQuery(tokens);
		} catch (ParseException e) {
			throw new XPathParserException(e.getMessage());
		}
	}

	/**
	 * Parses a {@link Query} from the given token stream.
	 * @param tokens the X-Path token stream
	 * @return a {@link Query}
	 */
	protected Query parseQuery(TokenStream tokens) {
		Query query = new Query();

		ArtifactSet artifactSet = parseArtifactSet(tokens);
		query.setArtifactSet(artifactSet);

		if (tokens.canConsume("[")) { //$NON-NLS-1$
			Predicate predicate = parsePredicate(tokens);
			query.setPredicate(predicate);
			if (!tokens.canConsume("]")) //$NON-NLS-1$
				throw new XPathParserException(Messages.i18n.format("XPATH_PREDICATE_NOT_TERMINATED")); //$NON-NLS-1$
		}

		if (tokens.canConsume("/")) { //$NON-NLS-1$
			SubartifactSet subartifactSet = parseSubartifactSet(tokens);
			query.setSubartifactSet(subartifactSet);
		}

		if (tokens.hasNext())
			throw new XPathParserException(Messages.i18n.format("XPATH_TERMINATION")); //$NON-NLS-1$

		return query;
	}

	/**
     * Parses an artifact-set from the token stream.
	 * @param tokens the X-Path token stream
	 * @return an artifact-set
	 */
	private ArtifactSet parseArtifactSet(TokenStream tokens) {
		ArtifactSet artifactSet = new ArtifactSet();
		LocationPath locationPath = parseLocationPath(tokens);
		artifactSet.setLocationPath(locationPath);
		return artifactSet;
	}

	/**
	 * Parses a location path from the token stream.
	 * @param tokens the X-Path token stream
	 * @return a location-path
	 */
	private LocationPath parseLocationPath(TokenStream tokens) {
		String artifactModel = null;
		String artifactType = null;

		if (!tokens.canConsume("/")) //$NON-NLS-1$
			throw new XPathParserException(Messages.i18n.format("XPATH_RELATIVE_QUERY")); //$NON-NLS-1$
		if (!tokens.matches(TokenType.name) && !tokens.matches("/")) //$NON-NLS-1$
			throw new XPathParserException(Messages.i18n.format("XPATH_INVALID_STEP_1")); //$NON-NLS-1$

		// Is this of the form //{artifactType} ?
		if (tokens.matches("/")) { //$NON-NLS-1$
			tokens.consume().toString();
			if (!tokens.matches(TokenType.name))
				throw new XPathParserException(Messages.i18n.format("XPATH_EMPTY_ROOT")); //$NON-NLS-1$
			artifactType = tokens.consume().toString();
			artifactModel = resolveArtifactModel(artifactType);
		} else {
			String rootSrampSegment = tokens.consume().toString();
			if (!"s-ramp".equals(rootSrampSegment)) //$NON-NLS-1$
				throw new XPathParserException(Messages.i18n.format("XPATH_INVALID_ROOT")); //$NON-NLS-1$

			// Next is the artifact model
			if (tokens.hasNext() && !tokens.matches("[")) { //$NON-NLS-1$
				if (!tokens.canConsume("/")) //$NON-NLS-1$
					throw new XPathParserException(Messages.i18n.format("XPATH_INVALID_STEP_2")); //$NON-NLS-1$
				if (!tokens.matches(TokenType.name))
					throw new XPathParserException(Messages.i18n.format("XPATH_INVALID_STEP_2")); //$NON-NLS-1$
				artifactModel = tokens.consume().toString();

				// And now the artifact type
				if (tokens.hasNext() && !tokens.matches("[")) { //$NON-NLS-1$
					if (!tokens.canConsume("/")) //$NON-NLS-1$
						throw new XPathParserException(Messages.i18n.format("XPATH_INVALID_STEP_3")); //$NON-NLS-1$
					if (!tokens.matches(TokenType.name))
						throw new XPathParserException(Messages.i18n.format("XPATH_INVALID_STEP_3")); //$NON-NLS-1$
					artifactType = tokens.consume().toString();
				}
			}
		}

		LocationPath locationPath = new LocationPath();
		locationPath.setArtifactModel(artifactModel);
		locationPath.setArtifactType(artifactType);
		return locationPath;
	}

    /**
     * Parses a predicate from the token stream.
	 * @param tokens the token stream
	 * @return a {@link Predicate}
	 */
	private Predicate parsePredicate(TokenStream tokens) {
		Expr expr = parseExpr(tokens);
		Predicate predicate = new Predicate();
		predicate.setExpr(expr);
		return predicate;
	}

	/**
	 * Parses an {@link Expr} from the token stream.
	 * @param tokens the token stream
	 * @return an {@link Expr}
	 */
	private Expr parseExpr(TokenStream tokens) {
		AndExpr andExpr = parseAndExpr(tokens);
		Expr expr = new Expr();
		expr.setAndExpr(andExpr);
		return expr;
	}

	/**
	 * Parses an {@link AndExpr} from the token stream.
	 * @param tokens the token stream
	 * @return an {@link AndExpr}
	 */
	private AndExpr parseAndExpr(TokenStream tokens) {
		AndExpr andExpr = new AndExpr();
		OrExpr left = parseOrExpr(tokens);
		andExpr.setLeft(left);
		if (tokens.canConsume("and")) { //$NON-NLS-1$
			AndExpr right = parseAndExpr(tokens);
			andExpr.setRight(right);
		}
		return andExpr;
	}

	/**
	 * Parses an {@link OrExpr} from the token stream.
	 * @param tokens the token stream
	 * @return an {@link OrExpr}
	 */
	private OrExpr parseOrExpr(TokenStream tokens) {
		OrExpr orExpr = new OrExpr();
		EqualityExpr left = parseEqualityExpr(tokens);
		orExpr.setLeft(left);
		if (tokens.canConsume("or")) { //$NON-NLS-1$
			OrExpr right = parseOrExpr(tokens);
			orExpr.setRight(right);
		}
		return orExpr;
	}

	/**
	 * Parses an {@link EqualityExpr} from the token stream.
	 * @param tokens the token stream
	 * @return an {@link EqualityExpr}
	 */
	private EqualityExpr parseEqualityExpr(TokenStream tokens) {
		EqualityExpr equalityExpr = new EqualityExpr();
		if (tokens.canConsume("(")) { //$NON-NLS-1$
			Expr expr = parseExpr(tokens);
			equalityExpr.setExpr(expr);
			if (!tokens.canConsume(")")) //$NON-NLS-1$
				throw new XPathParserException(Messages.i18n.format("XPATH_MISSING_PAREN")); //$NON-NLS-1$
		} else if (tokens.canConsume("@")) { //$NON-NLS-1$
			ForwardPropertyStep forwardPropertyStep = parseForwardPropertyStep(tokens);
			equalityExpr.setLeft(forwardPropertyStep);
            parseEqualityExprRight(tokens, equalityExpr);

		} else if (tokens.canConsume("s-ramp", ":", "getRelationshipAttribute")) {
            // Allow functions as the left side of equality expressions.
            // TODO: Some functions are equality expressions (ie, getRelationshipAttribute).  Most others
            // (classifiers, etc.) are not and need to be handled by parseSubartifactSet, below.  Not sure how to
            // better differentiate in a general way, rather than maintaining a list of functions here...
            FunctionCall functionCall = parseFunctionCall(tokens, "s-ramp", "getRelationshipAttribute");
            equalityExpr.setLeft(functionCall);
            parseEqualityExprRight(tokens, equalityExpr);
        } else if (tokens.canConsume("s-ramp", ":", "getTargetAttribute")) {
            // Allow functions as the left side of equality expressions.
            // TODO: Some functions are equality expressions (ie, getTargetAttribute).  Most others
            // (classifiers, etc.) are not and need to be handled by parseSubartifactSet, below.  Not sure how to
            // better differentiate in a general way, rather than maintaining a list of functions here...
            FunctionCall functionCall = parseFunctionCall(tokens, "s-ramp", "getTargetAttribute");
            equalityExpr.setLeft(functionCall);
            parseEqualityExprRight(tokens, equalityExpr);
        } else {
            // Else, assume the expression belongs in the subArtifactSelect.
            equalityExpr.setSubArtifactSet(parseSubartifactSet(tokens));
        }
		return equalityExpr;
	}

    private void parseEqualityExprRight(TokenStream tokens, EqualityExpr equalityExpr) {
        if (tokens.canConsume("!", "=")) { //$NON-NLS-1$ //$NON-NLS-2$
            equalityExpr.setOperator(EqualityExpr.Operator.NE);
			equalityExpr.setRight(parsePrimaryExpr(tokens));
        } else if (tokens.canConsume("<", "=")) { //$NON-NLS-1$ //$NON-NLS-2$
            equalityExpr.setOperator(EqualityExpr.Operator.LTE);
			equalityExpr.setRight(parsePrimaryExpr(tokens));
        } else if (tokens.canConsume(">", "=")) { //$NON-NLS-1$ //$NON-NLS-2$
            equalityExpr.setOperator(EqualityExpr.Operator.GTE);
			equalityExpr.setRight(parsePrimaryExpr(tokens));
        } else if (tokens.matchesAnyOf("=", "<", ">")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String symbol = tokens.consume().toString();
            EqualityExpr.Operator operator = EqualityExpr.Operator.valueOfSymbol(symbol);
            equalityExpr.setOperator(operator);
			equalityExpr.setRight(parsePrimaryExpr(tokens));
        }
    }

	/**
	 * Parses a {@link ForwardPropertyStep} from the token stream.
	 * @param tokens the token stream
	 * @return a {@link ForwardPropertyStep}
	 */
	private ForwardPropertyStep parseForwardPropertyStep(TokenStream tokens) {
		ForwardPropertyStep forwardPropertyStep = new ForwardPropertyStep();
		QName propertyQName = parseQName(tokens, null);
		forwardPropertyStep.setPropertyQName(propertyQName);
		return forwardPropertyStep;
	}

	/**
	 * Parses a QName from the token stream.
	 * @param tokens the token stream
	 * @param defaultPrefix the prefix to use if none is provided
	 * @return a {@link QName}
	 */
	private QName parseQName(TokenStream tokens, String defaultPrefix) {
		// TODO Perhaps instead of 'defaultPrefix' this should be 'defaultNamespace' to better work with custom namespace contexts
		String prefix = null;
		String localPart = null;
		String namespace = null;

		if (!tokens.matches(TokenType.name))
			throw new XPathParserException(Messages.i18n.format("XPATH_EXPECTED_NAME_TOKEN")); //$NON-NLS-1$
		String ncname1 = tokens.consume().toString();
		if (tokens.canConsume(":")) { //$NON-NLS-1$
			if (!tokens.matches(TokenType.name))
				throw new XPathParserException(Messages.i18n.format("XPATH_EXPECTED_NAME_TOKEN")); //$NON-NLS-1$
			String ncname2 = tokens.consume().toString();
			prefix = ncname1;
			localPart = ncname2;
		} else {
			prefix = defaultPrefix;
			localPart = ncname1;
		}
		namespace = getNamespaceContext().getNamespaceURI(prefix);
		if (prefix == null)
			prefix = ""; //$NON-NLS-1$
		return new QName(namespace, localPart, prefix);
	}

	/**
	 * Parses a {@link org.artificer.common.query.xpath.ast.PrimaryExpr} from the token stream.
	 * @param tokens the token stream
	 * @return a {@link org.artificer.common.query.xpath.ast.PrimaryExpr}
	 */
	private PrimaryExpr parsePrimaryExpr(TokenStream tokens) {
		PrimaryExpr primaryExpr = new PrimaryExpr();
		if (tokens.canConsume("$")) { //$NON-NLS-1$
			QName propertyQName = parseQName(tokens, null);
			primaryExpr.setPropertyQName(propertyQName);
		} else if (tokens.matches(TokenType.quotedString)) {
			String literal = tokens.consume().toString();
			literal = removeQuotes(literal);
			primaryExpr.setLiteral(literal);
		} else if (tokens.matches(TokenType.numeric)) {
			String numberStr = tokens.consume().toString();
			Number number = null;
			try {
				if (numberStr.contains(".")) { //$NON-NLS-1$
					number = new Double(numberStr);
				} else {
					number = new BigInteger(numberStr);
				}
			} catch (NumberFormatException e) {
				// This wasn't a number after all.
				throw new XPathParserException(Messages.i18n.format("XPATH_INVALID_NUMERIC_LITERAL")); //$NON-NLS-1$
			}
			primaryExpr.setNumber(number);
		} else {
			throw new XPathParserException(Messages.i18n.format("XPATH_PRIMARY_EXPR_EXPECTED")); //$NON-NLS-1$
		}
		return primaryExpr;
	}

	/**
	 * Parses a {@link SubartifactSet} from the token stream.
	 * @param tokens the token stream
	 * @return a {@link SubartifactSet}
	 */
	private SubartifactSet parseSubartifactSet(TokenStream tokens) {
		if (!tokens.matches(TokenType.name) && !tokens.matches(".")) //$NON-NLS-1$
			throw new XPathParserException(Messages.i18n.format("XPATH_EXPR_EXPECTED")); //$NON-NLS-1$

		SubartifactSet subartifactSet = new SubartifactSet();
		String relationshipOrFunction = tokens.consume().toString();

		// If the next token is a [ then we have a relationship
		// If the next token is a : or a ( then we have a (qualified or unqualified) function call
		// If none of the above, then we have a relationship

		if (tokens.canConsume("[")) { //$NON-NLS-1$
			RelationshipPath relationshipPath = new RelationshipPath(relationshipOrFunction);
			Predicate predicate = parsePredicate(tokens);
			if (!tokens.canConsume("]")) { //$NON-NLS-1$
				throw new XPathParserException(Messages.i18n.format("XPATH_UNTERMINATED_PREDICATE")); //$NON-NLS-1$
            }

			subartifactSet.setRelationshipPath(relationshipPath);
			subartifactSet.setPredicate(predicate);

            if (tokens.canConsume("/")) { //$NON-NLS-1$
                SubartifactSet sub_subartifactSet = parseSubartifactSet(tokens);
                subartifactSet.setSubartifactSet(sub_subartifactSet);
            }
		} else if (tokens.canConsume(":")) { //$NON-NLS-1$
			String prefix = relationshipOrFunction;
			if (!tokens.matches(TokenType.name))
				throw new XPathParserException(Messages.i18n.format("XPATH_FUNCTION_EXPECTED")); //$NON-NLS-1$
			String localName = tokens.consume().toString();
            FunctionCall functionCall = parseFunctionCall(tokens, prefix, localName);
            subartifactSet.setFunctionCall(functionCall);
		} else if (tokens.matches("(")) { //$NON-NLS-1$
			String prefix = getDefaultPrefix();
			String localName = relationshipOrFunction;
			String namespace = getNamespaceContext().getNamespaceURI(prefix);
			QName functionName = new QName(namespace, localName, prefix);
			List<Argument> arguments = parseFunctionArguments(tokens);

			FunctionCall functionCall = new FunctionCall();
			functionCall.setFunctionName(functionName);
			functionCall.setArguments(arguments);
			subartifactSet.setFunctionCall(functionCall);
		} else {
			RelationshipPath relationshipPath = new RelationshipPath(relationshipOrFunction);
			subartifactSet.setRelationshipPath(relationshipPath);
		}

		return subartifactSet;
	}

    private FunctionCall parseFunctionCall(TokenStream tokens, String prefix, String localName) {
        String namespace = getNamespaceContext().getNamespaceURI(prefix);
        QName functionName = new QName(namespace, localName, prefix);
        if (!tokens.matches("(")) //$NON-NLS-1$
            throw new XPathParserException(Messages.i18n.format("XPATH_ARGS_EXPECTED")); //$NON-NLS-1$
        List<Argument> arguments = parseFunctionArguments(tokens);

        FunctionCall functionCall = new FunctionCall();
        functionCall.setFunctionName(functionName);
        functionCall.setArguments(arguments);
        return functionCall;
    }

    /**
     * Parses a list of {@link Argument}s from the token stream.
	 * @param tokens the token stream
	 * @return a list of {@link Argument}s
	 */
	private List<Argument> parseFunctionArguments(TokenStream tokens) {
		tokens.consume().toString(); // Consume the open paren
		List<Argument> arguments = new ArrayList<Argument>();
		if (!tokens.matches(")")) { //$NON-NLS-1$
			boolean hasMoreArguments = true;
			while (hasMoreArguments) {
				Argument argument = parseArgument(tokens);
				arguments.add(argument);
				hasMoreArguments = tokens.canConsume(","); //$NON-NLS-1$
			}
		}
		if (!tokens.canConsume(")")) // Consume the close paren //$NON-NLS-1$
			throw new XPathParserException(Messages.i18n.format("XPATH_UNTERMINATED_ARG_LIST")); //$NON-NLS-1$
		return arguments;
	}

	/**
	 * Parses a single {@link Argument} from the token stream.
	 * @param tokens the token stream
	 * @return an {@link Argument}
	 */
	private Argument parseArgument(TokenStream tokens) {
		Argument argument = new Argument();
		if (tokens.matchesAnyOf(TokenType.quotedString, TokenType.numeric) || tokens.matches("$")) { //$NON-NLS-1$
			PrimaryExpr primaryExpr = parsePrimaryExpr(tokens);
			argument.setPrimaryExpr(primaryExpr);
		} else {
			Expr expr = parseExpr(tokens);
			argument.setExpr(expr);
		}
		return argument;
	}

	/**
     * Remove any leading and trailing single-quotes or double-quotes from the supplied text.  Also
     * unescape any possibly escaped quote characters.  The result of calling this method will be
     * the real value of a quoted string from the query.
     *
     * @param text the input text
     * @return the text without leading and trailing quotes
     */
	protected String removeQuotes(String text) {
		char first = text.charAt(0);
		text = text.substring(1, text.length() - 1);
		String unescapeFrom = String.valueOf(first) + String.valueOf(first);
		String unescapeTo = String.valueOf(first);
		text = text.replace(unescapeFrom, unescapeTo);
		return text;
	}

	/**
	 * Resolves the proper artifact model given an artifact type.
	 * @param artifactType the S-RAMP artifact type
	 */
	private String resolveArtifactModel(String artifactType) {
		return ArtifactType.valueOf(artifactType).getArtifactType().getModel();
	}
}
