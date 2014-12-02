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
package org.overlord.sramp.common.query.xpath;

import java.util.ArrayList;
import java.util.List;

import org.overlord.sramp.common.i18n.Messages;

/**
 * A stream of tokens produced by the tokenizer.
 *
 * @author eric.wittmann@redhat.com
 * @author Brett Meyer
 */
public class TokenStream {

	private List<Token> tokens = new ArrayList<Token>();

	private Token[] stream;
	private int nextTokenPos;

	/**
	 * Constructor.
	 */
	public TokenStream() {
	}

	/**
	 * Adds a token to the stream.
	 * @param value
	 * @param type
	 */
	public void addToken(String value, TokenType type) {
		if (tokens == null) {
			throw new RuntimeException(Messages.i18n.format("TOKEN_STREAM_ALREADY_BUILT")); //$NON-NLS-1$
		}
		tokens.add(new Token(type, value));
	}

	/**
	 * Builds the stream for consumption.
	 */
	public TokenStream build() {
		if (tokens == null) {
			throw new RuntimeException(Messages.i18n.format("TOKEN_STREAM_ALREADY_BUILT_2")); //$NON-NLS-1$
		}
		stream = tokens.toArray(new Token[tokens.size()]);
		tokens = null;
		nextTokenPos = 0;
		return this;
	}

	/**
	 * Returns true if there are more tokens in the stream.
	 */
	public boolean hasNext() {
		return this.nextTokenPos < this.stream.length;
	}

	/**
	 * Attempts to consume a token that matches any of the given token values.  Returns true if
	 * the token was able to be consumed.
	 * @param tokenValue
	 */
	public boolean canConsume(String tokenValue) {
		if (hasNext()) {
			Token nextToken = this.stream[this.nextTokenPos];
			if (nextToken.matches(tokenValue)) {
				this.nextTokenPos++;
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts to consume the next n tokens, but only if they match the given token values.
	 * Returns true only if all tokens matched and were consumed.
	 * @param tokenValues
	 */
	public boolean canConsume(String... tokenValues) {
        boolean rval = matches(tokenValues);
        if (rval) {
            this.nextTokenPos += tokenValues.length;
        }
        return rval;
	}

	/**
	 * Attempts to consume a token that matches any of the given token values.  Returns true if
	 * the token was able to be consumed.
	 * @param tokenValues
	 */
	public boolean canConsumeAnyOf(String ... tokenValues) {
		if (hasNext()) {
			Token nextToken = this.stream[this.nextTokenPos];
			if (nextToken.matches(tokenValues)) {
				this.nextTokenPos++;
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the next token in the stream matches any of the given types.
	 * @param tokenType
	 */
	public boolean matches(TokenType tokenType) {
		if (hasNext()) {
			Token nextToken = this.stream[this.nextTokenPos];
			return nextToken.matches(tokenType);
		} else {
			return false;
		}
	}

	/**
	 * Returns true if the next token in the stream matches any of the given types.
	 * @param tokenTypes
	 */
	public boolean matchesAnyOf(TokenType ... tokenTypes) {
		if (hasNext()) {
			Token nextToken = this.stream[this.nextTokenPos];
			return nextToken.matches(tokenTypes);
		} else {
			return false;
		}
	}

    /**
     * Returns true if the next token in the stream matches any of the given token values.
     * @param tokenValue
     */
    public boolean matches(String tokenValue) {
        if (hasNext()) {
            Token nextToken = this.stream[this.nextTokenPos];
            return nextToken.matches(tokenValue);
        } else {
            return false;
        }
    }

    /**
     * Returns true if the next tokens in the stream match the given token values.
     * @param tokenValues
     */
    public boolean matches(String... tokenValues) {
        int n = tokenValues.length;
        if ((this.nextTokenPos + n) <= this.stream.length) {
            for (int i = 0; i < n; i++) {
                Token token = this.stream[this.nextTokenPos + i];
                if (!token.matches(tokenValues[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

	/**
	 * Returns true if the next token in the stream matches any of the given token values.
	 * @param tokenValues
	 */
	public boolean matchesAnyOf(String ... tokenValues) {
		if (hasNext()) {
			Token nextToken = this.stream[this.nextTokenPos];
			return nextToken.matches(tokenValues);
		} else {
			return false;
		}
	}

	/**
	 * Consume the next token in the stream, and return what it was.
	 */
	public Token consume() {
		if (hasNext()) {
			return this.stream[this.nextTokenPos++];
		} else {
			return null;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		StringBuilder pbuilder = new StringBuilder();
		if (this.stream != null) {
			int count = 0;
			boolean first = true;
			for (Token token : this.stream) {
				if (!first) {
					builder.append(" "); //$NON-NLS-1$
					pbuilder.append(" "); //$NON-NLS-1$
				} else {
					first = false;
				}
				builder.append(token.getValue());
				char filler = ' ';
				if (count == this.nextTokenPos) {
					filler = '^';
				}
				for (int idx = 0; idx < token.getValue().length(); idx++) {
					pbuilder.append(filler);
				}
				count++;
			}
		}
		return builder.toString() + "\n" + pbuilder.toString(); //$NON-NLS-1$
	}

}
