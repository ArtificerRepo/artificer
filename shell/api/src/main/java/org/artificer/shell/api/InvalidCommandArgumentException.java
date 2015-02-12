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
package org.artificer.shell.api;

/**
 * An exception thrown when an argument to a command is invalid.
 *
 * @author eric.wittmann@redhat.com
 */
public class InvalidCommandArgumentException extends Exception {

	private static final long serialVersionUID = 1720878965735759391L;

	private int argumentIndex;

	/**
	 * Constructor.
	 */
	public InvalidCommandArgumentException(int argumentIndex, String message) {
		super(message);
		this.setArgumentIndex(argumentIndex);
	}

	/**
	 * @return the argumentIndex
	 */
	public int getArgumentIndex() {
		return argumentIndex;
	}

	/**
	 * @param argumentIndex the argumentIndex to set
	 */
	public void setArgumentIndex(int argumentIndex) {
		this.argumentIndex = argumentIndex;
	}

}
