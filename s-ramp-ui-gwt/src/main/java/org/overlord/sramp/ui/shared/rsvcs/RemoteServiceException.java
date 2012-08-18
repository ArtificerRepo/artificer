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
package org.overlord.sramp.ui.shared.rsvcs;



/**
 * Remote services can throw this exception and it will be caught on the client.
 *
 * @author eric.wittmann@redhat.com
 */
public class RemoteServiceException extends Exception {

	private static final long serialVersionUID = -5870562184243839633L;
	
	private String rootStackTrace;

	/**
	 * Constructor.
	 */
	public RemoteServiceException() {
		super();
	}
	
	/**
	 * Constructor.
	 * @param message
	 * @param root
	 */
	public RemoteServiceException(String message, Throwable root) {
		super(message, root);
	}
	
	/**
	 * Constructor.
	 * @param root
	 */
	public RemoteServiceException(Throwable root) {
		super(root);
	}

	/**
	 * Constructor.
	 * @param message
	 */
	public RemoteServiceException(String message) {
		super(message);
	}

	/**
	 * @return the rootStackTrace
	 */
	public String getRootStackTrace() {
		return rootStackTrace;
	}

	/**
	 * @param rootStackTrace the rootStackTrace to set
	 */
	public void setRootStackTrace(String rootStackTrace) {
		this.rootStackTrace = rootStackTrace;
	}
	
}
