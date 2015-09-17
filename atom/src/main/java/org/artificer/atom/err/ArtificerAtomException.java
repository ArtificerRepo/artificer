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
package org.artificer.atom.err;

import org.artificer.common.error.ArtificerServerException;

/**
 * The exception thrown by the Atom layer whenever something goes horribly, horribly wrong.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerAtomException extends ArtificerServerException {

	private static final long serialVersionUID = -4954468657023096910L;

	/**
	 * Constructor.
	 */
	public ArtificerAtomException() {
	}

	/**
	 * Constructor.
	 * @param message
	 */
	public ArtificerAtomException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message
	 * @param cause
	 */
	public ArtificerAtomException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * @param cause
	 */
	public ArtificerAtomException(Throwable cause) {
		super(cause);
	}

}
