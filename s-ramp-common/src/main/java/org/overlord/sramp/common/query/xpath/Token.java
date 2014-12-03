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

/**
 * A single token in the token stream.  Every token has a String value and a token type.
 *
 * @author eric.wittmann@redhat.com
 */
public final class Token {

	private final TokenType type;
	private final String value;

	/**
	 * Constructor.
	 * @param type
	 * @param value
	 */
	public Token(TokenType type, String value) {
		this.type = type;
		this.value = value;
	}

	/**
	 * @return the type
	 */
	public TokenType getType() {
		return type;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns true if this token's value matches any of the values provided.
	 * @param values
	 */
	public boolean matches(String ... values) {
		for (String value : values) {
			if (getValue().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this token's type matches any of the token types provided.
	 * @param tokenTypes
	 */
	public boolean matches(TokenType ... tokenTypes) {
		for (TokenType tt : tokenTypes) {
			if (tt == this.type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.value;
	}

}
