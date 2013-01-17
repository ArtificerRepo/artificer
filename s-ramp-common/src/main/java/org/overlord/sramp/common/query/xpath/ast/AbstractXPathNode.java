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
package org.overlord.sramp.common.query.xpath.ast;

import org.overlord.sramp.common.query.xpath.visitors.XPathVisitor;

/**
 * Base class for all S-RAMP XPath Query AST models.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractXPathNode {
	
	/**
	 * Default constructor.
	 */
	public AbstractXPathNode() {
	}

	/**
	 * Accepts a visitor.
	 * @param visitor
	 */
	public abstract void accept(XPathVisitor visitor);

}
