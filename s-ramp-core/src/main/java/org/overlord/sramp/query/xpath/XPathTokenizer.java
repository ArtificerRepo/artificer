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
package org.overlord.sramp.query.xpath;

import org.modeshape.common.CommonI18n;
import org.modeshape.common.text.ParsingException;
import org.modeshape.common.text.Position;
import org.modeshape.common.text.TokenStream;
import org.modeshape.common.text.TokenStream.CharacterStream;
import org.modeshape.common.text.TokenStream.Tokens;
import org.modeshape.common.xml.XmlCharacters;

/**
 * An extension of the ModeShape tokenizer for XPath queries.  Thanks to the ModeShape project
 * for this class.  Please see org.modeshape.jcr.query.xpath.XPathParser.XPathTokenizer in
 * the ModeShape project for the original class.  The original class was copied here in case
 * it needs to be altered separately from the ModeShape implementation.
 */
public class XPathTokenizer implements TokenStream.Tokenizer {
    /**
     * The token type for tokens that represent an unquoted string containing a character sequence made up of non-whitespace
     * and non-symbol characters.
     */
    public static final int NAME = 2 << 0;
    /**
     * The token type for tokens that consist of an individual "symbol" character. The set of characters includes:
     * <code>(){}*.,;+%?$!<>|=:-[]^/\#@</code>
     */
    public static final int SYMBOL = 2 << 1;
    /**
     * The token type for tokens that consist of all the characters within single-quotes, double-quotes.
     */
    public static final int QUOTED_STRING = 2 << 2;
    /**
     * The token type for tokens that consist of all the characters between "(:" and ":)".
     */
    public static final int COMMENT = 2 << 3;
    /**
     * The token type for tokens that consist of single characters that are not a {@link #SYMBOL}, valid {@link #NAME}, or
     * {@link #QUOTED_STRING}.
     */
    public static final int OTHER = 2 << 4;
    /**
     * The token type for tokens that consist of a numeric literal.
     */
    public static final int NUMERIC = 2 << 5;

    private final boolean useComments;

    public XPathTokenizer( boolean useComments ) {
        this.useComments = useComments;
    }

    @Override
    public void tokenize( CharacterStream input,
                          Tokens tokens ) throws ParsingException {
        while (input.hasNext()) {
            char c = input.next();
            int startIndex, endIndex, tokenType;
            Position pos;
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
                	if (input.isNextAnyOf("0123456789")) {
                        startIndex = input.index();
                        pos = input.position(startIndex);
                    	while (isNextValidNumeric(input)) {
                    		c = input.next();
                    	}
                        endIndex = input.index() + 1; // beyond last character that was included
                        tokens.addToken(pos, startIndex, endIndex, NUMERIC);
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
                    tokens.addToken(input.position(input.index()), input.index(), input.index() + 1, SYMBOL);
                    break;
                case '\'':
                case '\"':
                    startIndex = input.index();
                    char closingChar = c;
                    pos = input.position(startIndex);
                    boolean foundClosingQuote = false;
                    while (input.hasNext()) {
                        c = input.next();
                        if (c == closingChar && input.isNext(closingChar)) {
                            c = input.next(); // consume the next closeChar since it is escaped
                        } else if (c == closingChar) {
                            foundClosingQuote = true;
                            break;
                        }
                    }
                    if (!foundClosingQuote) {
                        String msg = CommonI18n.noMatchingDoubleQuoteFound.text(pos.getLine(), pos.getColumn());
                        if (closingChar == '\'') {
                            msg = CommonI18n.noMatchingSingleQuoteFound.text(pos.getLine(), pos.getColumn());
                        }
                        throw new ParsingException(pos, msg);
                    }
                    endIndex = input.index() + 1; // beyond last character read
                    tokens.addToken(pos, startIndex, endIndex, QUOTED_STRING);
                    break;
                case '(':
                    startIndex = input.index();
                    if (input.isNext(':')) {
                        // This is a comment ...
                        pos = input.position(startIndex);
                        while (input.hasNext() && !input.isNext(':', ')')) {
                            c = input.next();
                        }
                        if (input.hasNext()) input.next(); // consume the ':'
                        if (input.hasNext()) input.next(); // consume the ')'
                        if (useComments) {
                            endIndex = input.index() + 1; // the token will include the closing ':' and ')' characters
                            tokens.addToken(pos, startIndex, endIndex, COMMENT);
                        }
                    } else {
                        tokens.addToken(input.position(startIndex), input.index(), input.index() + 1, SYMBOL);
                        break;
                    }
                    break;
                default:
                    startIndex = input.index();
                    pos = input.position(startIndex);
                    if (XmlCharacters.isValidNcNameStart(c)) {
                    	tokenType = NAME;
                        // Read as long as there is a valid XML character ...
                        while (input.isNextValidXmlNcNameCharacter()) {
                            c = input.next();
                        }
                    } else if (isValidNumericStart(c)) {
                    	tokenType = NUMERIC;
                        // Read as long as there is a valid numeric character ...
                    	while (isNextValidNumeric(input)) {
                    		c = input.next();
                    	}
                    } else {
                    	tokenType = OTHER;
                        // Read as long as there is a valid XML character ...
                        while (input.isNextValidXmlNcNameCharacter()) {
                            c = input.next();
                        }
                    }
                    endIndex = input.index() + 1; // beyond last character that was included
                    tokens.addToken(pos, startIndex, endIndex, tokenType);
            }
        }
    }

	/**
	 * Returns true if the given character is a valid numeric start character.
	 * @param c the character
	 * @return boolean
	 */
	private boolean isValidNumericStart(char c) {
		return Character.isDigit(c);
	}

	/**
	 * Returns true if the next character on the given stream is a valid numeric
	 * character.
	 * @param input the character stream
	 * @return boolean
	 */
	private boolean isNextValidNumeric(CharacterStream input) {
		return input.isNextAnyOf("0123456789.-+eE");
	}
}