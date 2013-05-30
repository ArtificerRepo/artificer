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
package org.overlord.sramp.atom.archive.jar;


/**
 * Exception thrown when a problem with the {@link JarToSrampArchive} class encounters an
 * error.
 *
 * @author eric.wittmann@redhat.com
 */
public class JarToSrampArchiveException extends Exception {

	private static final long serialVersionUID = -5084985969972561772L;

	/**
	 * Constructor.
	 */
	public JarToSrampArchiveException() {
	}

	/**
	 * Constructor.
	 * @param message
	 */
	public JarToSrampArchiveException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message
	 * @param cause
	 */
	public JarToSrampArchiveException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * @param cause
	 */
	public JarToSrampArchiveException(Throwable cause) {
		super(cause);
	}

}
