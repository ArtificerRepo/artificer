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
 * A stream of character data.
 *
 * @author eric.wittmann@redhat.com
 */
public class CharacterStream {

	private char [] stream;
	private int nextIndex;

	/**
	 * Constructor.
	 * @param input
	 */
	public CharacterStream(String input) {
		stream = input.toCharArray();
		nextIndex = 0;
	}

	/**
	 * Returns true if there are more characters in the stream.
	 */
	public boolean hasNext() {
		return nextIndex < stream.length;
	}

	/**
	 * Reads the next character in the stream and returns it.
	 */
	public char next() {
		return stream[nextIndex++];
	}

	/**
	 * Returns true if the next character in the stream is any of the characters
	 * included in the given string.
	 * @param values
	 */
	public boolean isNext(String values) {
		char [] charValues = values.toCharArray();
		return isNext(charValues);
	}

	/**
	 * Returns true if the next character in the stream is equal to any of the given
	 * characters.
	 * @param values
	 */
	public boolean isNext(char ... values) {
		if (!hasNext())
			return false;

		for (char value : values) {
			if (stream[nextIndex] == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the next character in the stream is a valid start character for
	 * a numeric.
	 */
	public boolean isNextNumericStart() {
		return isNext("0123456789"); //$NON-NLS-1$
	}

	/**
	 * Returns true if the next character in the stream is a valid numeric character.
	 */
	public boolean isNextNumeric() {
		return isNext("0123456789.-+eE"); //$NON-NLS-1$
	}

	/**
	 * Returns true if the next character in the stream is a valid XML NCName
	 * character.
	 */
	public boolean isNextValidXmlNcNameCharacter() {
		if (!hasNext())
			return false;

		char c = stream[nextIndex];
		// Note - this doens't include the Extenders and CombiningChars specified in the XML
		// specification, but it's good enough for our purposes.
		return Character.isLetterOrDigit(c) ||
				c == '.' || c == '-' || c == '_';
	}

	/**
	 * Returns true if the next N characters in the stream are equal to the sequence of
	 * characters provided.
	 * @param values
	 */
	public boolean areNext(char ... values) {
		for (int idx = 0; idx < values.length; idx++) {
			char expected = values[idx];
			int streamIdx = nextIndex + idx;
			if (streamIdx < stream.length) {
				char actual = stream[streamIdx];
				if (actual != expected) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns the current index into the stream.
	 */
	public int index() {
		return this.nextIndex - 1;
	}

	/**
	 * Gets the token value represented by the given range.
	 * @param startIndex
	 * @param endIndex
	 */
	public String get(int startIndex, int endIndex) {
		return String.valueOf(stream, startIndex, endIndex - startIndex);
	}

}
