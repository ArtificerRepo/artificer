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
package org.overlord.sramp.query.xpath;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.modeshape.common.text.ParsingException;
import org.modeshape.common.text.TokenStream;
import org.modeshape.common.text.TokenStream.Tokenizer;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.SrampConstants;
import org.overlord.sramp.query.xpath.ast.AndExpr;
import org.overlord.sramp.query.xpath.ast.Argument;
import org.overlord.sramp.query.xpath.ast.ArtifactSet;
import org.overlord.sramp.query.xpath.ast.EqualityExpr;
import org.overlord.sramp.query.xpath.ast.EqualityExpr.Operator;
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
    	setDefaultPrefix(SrampConstants.SRAMP_PREFIX);
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
		Tokenizer tokenizer = new XPathTokenizer(false); // skip comments
		try {
			TokenStream tokens = new TokenStream(xpath, tokenizer, true).start(); // case sensitive!!
			return parseQuery(tokens);
		} catch (ParsingException e) {
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

		if (tokens.canConsume('[')) {
			Predicate predicate = parsePredicate(tokens);
			query.setPredicate(predicate);
			if (!tokens.canConsume(']'))
				throw new XPathParserException("Artifact-set predicate not terminated.");
		}

		if (tokens.canConsume('/')) {
			SubartifactSet subartifactSet = parseSubartifactSet(tokens);
			query.setSubartifactSet(subartifactSet);
		}

		if (tokens.hasNext())
			throw new XPathParserException("Query string improperly terminated (found extra data)");

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

		if (!tokens.canConsume('/'))
			throw new XPathParserException("Relative XPath queries not supported.");
		if (!tokens.matches(XPathTokenizer.NAME) && !tokens.matches('/'))
			throw new XPathParserException("Invalid artifact set (step 1).");

		// Is this of the form //{artifactType} ?
		if (tokens.matches('/')) {
			tokens.consume();
			if (!tokens.matches(XPathTokenizer.NAME))
				throw new XPathParserException("Empty // is an invalid query (expected '//{artifactType}').");
			artifactType = tokens.consume();
			artifactModel = resolveArtifactModel(artifactType);
		} else {
			String rootSrampSegment = tokens.consume();
			if (!"s-ramp".equals(rootSrampSegment))
				throw new XPathParserException("Query must begin with /s-ramp or //).");

			// Next is the artifact model
			if (tokens.hasNext() && !tokens.matches('[')) {
				if (!tokens.canConsume('/'))
					throw new XPathParserException("Invalid artifact set (step 2).");
				if (!tokens.matches(XPathTokenizer.NAME))
					throw new XPathParserException("Invalid artifact set (step 2).");
				artifactModel = tokens.consume();

				// And now the artifact type
				if (tokens.hasNext() && !tokens.matches('[')) {
					if (!tokens.canConsume('/'))
						throw new XPathParserException("Invalid artifact set (step 3).");
					if (!tokens.matches(XPathTokenizer.NAME))
						throw new XPathParserException("Invalid artifact set (step 3).");
					artifactType = tokens.consume();
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
		if (tokens.canConsume("and")) {
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
		if (tokens.canConsume("or")) {
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
		if (tokens.canConsume('(')) {
			Expr expr = parseExpr(tokens);
			equalityExpr.setExpr(expr);
			if (!tokens.canConsume(')'))
				throw new XPathParserException("Missing close-paren ')' in expression.");
		} else {
			ForwardPropertyStep forwardPropertyStep = parseForwardPropertyStep(tokens);
			PrimaryExpr primaryExpr = null;
			if (tokens.canConsume("!", "=")) {
				equalityExpr.setOperator(Operator.NE);
				primaryExpr = parsePrimaryExpr(tokens);
			} else if (tokens.canConsume("<", "=")) {
				equalityExpr.setOperator(Operator.LTE);
				primaryExpr = parsePrimaryExpr(tokens);
			} else if (tokens.canConsume(">", "=")) {
				equalityExpr.setOperator(Operator.GTE);
				primaryExpr = parsePrimaryExpr(tokens);
			} else if (tokens.matchesAnyOf("=", "<", ">")) {
				String symbol = tokens.consume();
				Operator operator = Operator.valueOfSymbol(symbol);
				equalityExpr.setOperator(operator);
				primaryExpr = parsePrimaryExpr(tokens);
			}

			equalityExpr.setLeft(forwardPropertyStep);
			equalityExpr.setRight(primaryExpr);
		}
		return equalityExpr;
	}

	/**
	 * Parses a {@link ForwardPropertyStep} from the token stream.
	 * @param tokens the token stream
	 * @return a {@link ForwardPropertyStep}
	 */
	private ForwardPropertyStep parseForwardPropertyStep(TokenStream tokens) {
		ForwardPropertyStep forwardPropertyStep = new ForwardPropertyStep();
		QName propertyQName = null;
		SubartifactSet subartifactSet = null;

		if (tokens.canConsume('@')) {
			propertyQName = parseQName(tokens, null);
		} else {
			subartifactSet = parseSubartifactSet(tokens);
			if (tokens.canConsume('/')) {
				if (!tokens.canConsume('@'))
					throw new XPathParserException("Missing '@' from forward property step.");
				propertyQName = parseQName(tokens, null);
			}
		}

		forwardPropertyStep.setSubartifactSet(subartifactSet);
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

		if (!tokens.matches(XPathTokenizer.NAME))
			throw new XPathParserException("Expected NAME type token.");
		String ncname1 = tokens.consume();
		if (tokens.canConsume(":")) {
			if (!tokens.matches(XPathTokenizer.NAME))
				throw new XPathParserException("Expected NAME type token.");
			String ncname2 = tokens.consume();
			prefix = ncname1;
			localPart = ncname2;
		} else {
			prefix = defaultPrefix;
			localPart = ncname1;
		}
		namespace = getNamespaceContext().getNamespaceURI(prefix);
		if (prefix == null)
			prefix = "";
		return new QName(namespace, localPart, prefix);
	}

	/**
	 * Parses a {@link PrimaryExpr} from the token stream.
	 * @param tokens the token stream
	 * @return a {@link PrimaryExpr}
	 */
	private PrimaryExpr parsePrimaryExpr(TokenStream tokens) {
		PrimaryExpr primaryExpr = new PrimaryExpr();
		if (tokens.canConsume('$')) {
			QName propertyQName = parseQName(tokens, null);
			primaryExpr.setPropertyQName(propertyQName);
		} else if (tokens.matches(XPathTokenizer.QUOTED_STRING)) {
			String literal = tokens.consume();
			literal = removeQuotes(literal);
			primaryExpr.setLiteral(literal);
		} else if (tokens.matches(XPathTokenizer.NUMERIC)) {
			String numberStr = tokens.consume();
			Number number = null;
			try {
				if (numberStr.contains(".")) {
					number = new Double(numberStr);
				} else {
					number = new BigInteger(numberStr);
				}
			} catch (NumberFormatException e) {
				// This wasn't a number after all.
				throw new XPathParserException("Invalid numeric literal.");
			}
			primaryExpr.setNumber(number);
		} else {
			throw new XPathParserException("Expected a primary expression (string literal, number, etc).");
		}
		return primaryExpr;
	}

	/**
	 * Parses a {@link SubartifactSet} from the token stream.
	 * @param tokens the token stream
	 * @return a {@link SubartifactSet}
	 */
	private SubartifactSet parseSubartifactSet(TokenStream tokens) {
		if (!tokens.matches(XPathTokenizer.NAME) && !tokens.matches('.'))
			throw new XPathParserException("Expression expected.");

		SubartifactSet subartifactSet = new SubartifactSet();
		String relationshipOrFunction = tokens.consume();

		// If the next token is a [ then we have a relationship
		// If the next token is a : or a ( then we have a (qualified or unqualified) function call
		// If none of the above, then we have a relationship

		if (tokens.canConsume('[')) {
			RelationshipPath relationshipPath = new RelationshipPath(relationshipOrFunction);
			Predicate predicate = parsePredicate(tokens);
			if (!tokens.canConsume(']'))
				throw new XPathParserException("Unterminated predicate in subartifact-set.");

			subartifactSet.setRelationshipPath(relationshipPath);
			subartifactSet.setPredicate(predicate);

			if (tokens.canConsume('/')) {
				SubartifactSet sub_subartifactSet = parseSubartifactSet(tokens);
				subartifactSet.setSubartifactSet(sub_subartifactSet);
			}
		} else if (tokens.canConsume(':')) {
			String prefix = relationshipOrFunction;
			if (!tokens.matches(XPathTokenizer.NAME))
				throw new XPathParserException("Expected function name.");
			String localName = tokens.consume();
			String namespace = getNamespaceContext().getNamespaceURI(prefix);
			QName functionName = new QName(namespace, localName, prefix);
			if (!tokens.matches('('))
				throw new XPathParserException("Expected function arguments.");
			List<Argument> arguments = parseFunctionArguments(tokens);

			FunctionCall functionCall = new FunctionCall();
			functionCall.setFunctionName(functionName);
			functionCall.setArguments(arguments);
			subartifactSet.setFunctionCall(functionCall);
		} else if (tokens.matches('(')) {
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

    /**
     * Parses a list of {@link Argument}s from the token stream.
	 * @param tokens the token stream
	 * @return a list of {@link Argument}s
	 */
	private List<Argument> parseFunctionArguments(TokenStream tokens) {
		tokens.consume(); // Consume the open paren
		List<Argument> arguments = new ArrayList<Argument>();
		if (!tokens.matches(')')) {
			boolean hasMoreArguments = true;
			while (hasMoreArguments) {
				Argument argument = parseArgument(tokens);
				arguments.add(argument);
				hasMoreArguments = tokens.canConsume(',');
			}
		}
		if (!tokens.canConsume(')')) // Consume the close paren
			throw new XPathParserException("Unterminated argument list.");
		return arguments;
	}

	/**
	 * Parses a single {@link Argument} from the token stream.
	 * @param tokens the token stream
	 * @return an {@link Argument}
	 */
	private Argument parseArgument(TokenStream tokens) {
		Argument argument = new Argument();
		if (tokens.matchesAnyOf(XPathTokenizer.QUOTED_STRING, XPathTokenizer.NUMERIC) || tokens.matches('$')) {
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
