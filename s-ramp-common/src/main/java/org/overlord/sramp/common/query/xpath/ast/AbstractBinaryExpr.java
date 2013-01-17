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

/**
 * Base class for binary expressions.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractBinaryExpr<L extends AbstractXPathNode, R extends AbstractXPathNode>
		extends AbstractXPathNode {

	private L left;
	private R right;
	
	/**
	 * Default constructor.
	 */
	public AbstractBinaryExpr() {
	}

	/**
	 * @return the left
	 */
	public L getLeft() {
		return left;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(L left) {
		this.left = left;
	}

	/**
	 * @return the right
	 */
	public R getRight() {
		return right;
	}

	/**
	 * @param right the right to set
	 */
	public void setRight(R right) {
		this.right = right;
	}
	
}
