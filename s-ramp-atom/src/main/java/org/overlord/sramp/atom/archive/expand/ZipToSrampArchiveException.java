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
package org.overlord.sramp.atom.archive.expand;


/**
 * Exception thrown when a problem with the {@link ZipToSrampArchive} class encounters an
 * error.
 *
 * @author eric.wittmann@redhat.com
 */
public class ZipToSrampArchiveException extends Exception {

	private static final long serialVersionUID = -5084985969972561772L;

	/**
	 * Constructor.
	 */
	public ZipToSrampArchiveException() {
	}

	/**
	 * Constructor.
	 * @param message
	 */
	public ZipToSrampArchiveException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message
	 * @param cause
	 */
	public ZipToSrampArchiveException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * @param cause
	 */
	public ZipToSrampArchiveException(Throwable cause) {
		super(cause);
	}

}
