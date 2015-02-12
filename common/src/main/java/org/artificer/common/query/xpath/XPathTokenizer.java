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
package org.artificer.common.query.xpath;

import java.text.ParseException;

import org.artificer.common.i18n.Messages;

/**
 * This class tokenizes an input string into a stream of tokens.
 */
public class XPathTokenizer {

    /**
     * Constructor.
     */
    public XPathTokenizer() {
    }

	/**
	 * Tokenize the input into a stream of tokens that the parser can then use.
	 * @param input
	 * @throws ParsingException
	 */
	public TokenStream tokenize(String input) throws ParseException {
		CharacterStream stream = new CharacterStream(input);
		TokenStream tokens = new TokenStream();
        while (stream.hasNext()) {
            char c = stream.next();
            int startIndex, endIndex;
            switch (c) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    // Just skip these whitespace characters ...
                    break;
                case '.':
                case '-':
                	// If it's followed by a digit, then tokenize it as a numeric
                	if (stream.isNextNumericStart()) {
                        startIndex = stream.index();
                    	while (stream.isNextNumeric()) {
                    		c = stream.next();
                    	}
                        endIndex = stream.index() + 1;
                        tokens.addToken(stream.get(startIndex, endIndex), TokenType.numeric);
                        break;
                	}
                case ')':
                case '{':
                case '}':
                case '*':
                case ',':
                case ';':
                case '+':
                case '%':
                case '?':
                case '$':
                case '!':
                case '<':
                case '>':
                case '|':
                case '=':
                case ':':
                case '[':
                case ']':
                case '^':
                case '/':
                case '\\':
                case '#':
                case '@':
                    tokens.addToken(stream.get(stream.index(), stream.index() + 1), TokenType.symbol);
                    break;
                case '\'':
                case '\"':
                    startIndex = stream.index();
                    char closingChar = c;
                    boolean foundClosingQuote = false;
                    while (stream.hasNext()) {
                        c = stream.next();
                        if (c == closingChar && stream.isNext(closingChar)) {
                            c = stream.next(); // consume the next closeChar since it is escaped
                        } else if (c == closingChar) {
                            foundClosingQuote = true;
                            break;
                        }
                    }
                    if (!foundClosingQuote) {
                    	throw new ParseException(Messages.i18n.format("XPATH_TOK_MISSING_QUOTE"), stream.index()); //$NON-NLS-1$
                    }
                    endIndex = stream.index() + 1; // beyond last character read
                    tokens.addToken(stream.get(startIndex, endIndex), TokenType.quotedString);
                    break;
                case '(':
                	startIndex = stream.index();
                	if (stream.isNext(':')) {
                		// This is a comment ...
                		while (stream.hasNext() && !stream.areNext(':', ')')) {
                			c = stream.next();
                		}
                		// consume the ':'
                		if (stream.hasNext())
                			stream.next();
                		// consume the ')'
                		if (stream.hasNext())
                			stream.next();
                	} else {
                		tokens.addToken(stream.get(stream.index(), stream.index() + 1), TokenType.symbol);
                		break;
                	}
                	break;
                default:
                    startIndex = stream.index();
                    TokenType tokenType;
                    if (isValidNcNameStart(c)) {
                    	tokenType = TokenType.name;
                        // Read as long as there is a valid XML character ...
                        while (stream.isNextValidXmlNcNameCharacter()) {
                            c = stream.next();
                        }
                    } else if (isValidNumericStart(c)) {
                    	tokenType = TokenType.numeric;
                        // Read as long as there is a valid numeric character ...
                    	while (stream.isNextNumeric()) {
                    		c = stream.next();
                    	}
                    } else {
                    	tokenType = TokenType.other;
                        // Read as long as there is a valid XML character ...
                        while (stream.isNextValidXmlNcNameCharacter()) {
                            c = stream.next();
                        }
                    }
                    endIndex = stream.index() + 1; // beyond last character that was included
                    tokens.addToken(stream.get(startIndex, endIndex), tokenType);
            }
        }
        return tokens.build();
    }

	/**
	 * Returns true if the given character is a valid start of an NCName.
	 * @param c
	 */
	private boolean isValidNcNameStart(char c) {
		return Character.isLetter(c) || c == '_';
	}

	/**
	 * Returns true if the given character is a valid numeric start character.
	 * @param c the character
	 * @return boolean
	 */
	private boolean isValidNumericStart(char c) {
		return Character.isDigit(c);
	}
}