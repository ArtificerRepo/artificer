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

import org.modeshape.common.text.TokenStream;
import org.modeshape.common.text.TokenStream.Tokenizer;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.query.xpath.ast.ArtifactSet;
import org.overlord.sramp.query.xpath.ast.LocationPath;
import org.overlord.sramp.query.xpath.ast.Predicate;
import org.overlord.sramp.query.xpath.ast.Query;
import org.overlord.sramp.query.xpath.ast.SubartifactSet;

/**
 * Parses an XPath query string and creates an abstract syntax tree representation. The supported grammar is
 * defined by the S-RAMP specification and is a subset of the XPath 2.0 grammar.
 */
public class XPathParser {

    /**
     * Default constructor.
     */
    public XPathParser() {
    }

    /**
     * Called to parse the XPath query into an S-RAMP XPath AST.
     * @param xpath the S-RAMP Query being parsed
     * @return an S-RAMP XPath AST
     */
	public Query parseXPath(String xpath) {
		Tokenizer tokenizer = new XPathTokenizer(false); // skip comments
		TokenStream tokens = new TokenStream(xpath, tokenizer, true).start(); // case sensitive!!
		return parseSrampQuery(tokens);
	}

	/**
	 * Parses an {@link Query} from the given token stream.
	 * @param tokens the X-Path token stream
	 * @return an {@link Query}
	 */
	protected Query parseSrampQuery(TokenStream tokens) {
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
			if (tokens.hasNext()) {
				if (!tokens.canConsume('/'))
					throw new XPathParserException("Invalid artifact set (step 2).");
				if (!tokens.matches(XPathTokenizer.NAME))
					throw new XPathParserException("Invalid artifact set (step 2).");
				artifactModel = tokens.consume();
				
				// And now the artifact type
				if (tokens.hasNext()) {
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
	 * @param tokens
	 * @return
	 */
	private Predicate parsePredicate(TokenStream tokens) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param tokens
	 * @return
	 */
	private SubartifactSet parseSubartifactSet(TokenStream tokens) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Resolves the proper artifact model given an artifact type.
	 * @param artifactType the S-RAMP artifact type
	 */
	private String resolveArtifactModel(String artifactType) {
		return ArtifactType.valueOf(artifactType).getModel();
	}
}
