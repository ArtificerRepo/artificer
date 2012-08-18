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
package org.overlord.sramp.client;

/**
 * Exception thrown by the S-RAMP client when an error is received from the 
 * s-ramp server (repository).
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampServerException extends RuntimeException {

	private static final long serialVersionUID = -40629222359166674L;
	
	private String remoteStackTrace;

	/**
	 * Constructor.
	 */
	public SrampServerException() {
	}

	/**
	 * Constructor.
	 * @param message
	 */
	public SrampServerException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message
	 * @param cause
	 */
	public SrampServerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * @param cause
	 */
	public SrampServerException(Throwable cause) {
		super(cause);
	}

	/**
	 * @return the remoteStackTrace
	 */
	public String getRemoteStackTrace() {
		return remoteStackTrace;
	}

	/**
	 * @param remoteStackTrace the remoteStackTrace to set
	 */
	public void setRemoteStackTrace(String remoteStackTrace) {
		this.remoteStackTrace = remoteStackTrace;
	}

}
