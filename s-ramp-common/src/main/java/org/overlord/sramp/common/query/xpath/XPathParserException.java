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
 * Thrown by the XPathParser when it encounters a parse problem.
 *
 * @author eric.wittmann@redhat.com
 */
public class XPathParserException extends RuntimeException {

	private static final long serialVersionUID = XPathParserException.class.hashCode();

	/**
	 * @param message
	 */
	public XPathParserException(String message) {
		super(message);
	}

}
