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

/**
 * A stream of tokens produced by the tokenizer.
 *
 * @author eric.wittmann@redhat.com
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
			throw new RuntimeException("Attempt to add a token after the stream was built.");
		}
		tokens.add(new Token(type, value));
	}

	/**
	 * Builds the stream for consumption.
	 */
	public TokenStream build() {
		if (tokens == null) {
			throw new RuntimeException("Token stream already built.");
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
	 * Attempts to consume the next two tokens, but only if they match the given token values.
	 * Returns true only if both tokens matched and were consumed.
	 * @param tokenValue1
	 * @param tokenValue2
	 */
	public boolean canConsume(String tokenValue1, String tokenValue2) {
		if ((this.nextTokenPos + 1) < this.stream.length) {
			Token token1 = this.stream[this.nextTokenPos];
			Token token2 = this.stream[this.nextTokenPos + 1];
			if (token1.matches(tokenValue1) && token2.matches(tokenValue2)) {
				this.nextTokenPos += 2;
				return true;
			}
		}
		return false;
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
					builder.append(" ");
					pbuilder.append(" ");
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
		return builder.toString() + "\n" + pbuilder.toString();
	}

}
