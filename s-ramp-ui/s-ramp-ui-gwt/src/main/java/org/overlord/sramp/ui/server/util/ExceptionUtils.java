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
package org.overlord.sramp.ui.server.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.overlord.sramp.client.SrampServerException;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

/**
 * Provides some util methods for dealing with exceptions.
 *
 * @author eric.wittmann@redhat.com
 */
public class ExceptionUtils {
	
	/**
	 * Gets the root stack trace as a string.
	 * @param t
	 */
	public static String getRootStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		getRootCause(t).printStackTrace(writer);
		return sw.getBuffer().toString();
	}

	/**
	 * Gets the root exception from the given {@link Throwable}.
	 * @param t
	 */
	public static Throwable getRootCause(Throwable t) {
		Throwable root = t;
		while (root.getCause() != null && root.getCause() != root)
			root = root.getCause();
		return root;
	}
	
	/**
	 * Creates a remote exception that can be thrown to the client.
	 * @param message
	 * @param root
	 */
	public static RemoteServiceException createRemoteException(String message, Throwable root) {
		RemoteServiceException re = null;
		if (message != null && root != null) {
			re = new RemoteServiceException(message, root);
		} else if (root != null) {
			re = new RemoteServiceException(root);
		} else {
			re = new RemoteServiceException();
		}
		if (root != null)
			re.setRootStackTrace(getRootStackTrace(root));
		return re;
	}

	/**
	 * Creates a remote exception that can be thrown to the client.
	 * @param root
	 */
	public static RemoteServiceException createRemoteException(Throwable root) {
		return createRemoteException(null, root);
	}

	/**
	 * Creates a remote exception that can be thrown to the client.
	 * @param serverError
	 */
	public static RemoteServiceException createRemoteException(SrampServerException serverError) {
		RemoteServiceException re = new RemoteServiceException(serverError.getMessage());
		StringBuffer b = new StringBuffer();
		b.append(getRootStackTrace(serverError));
		if (serverError.getRemoteStackTrace() != null) {
			b.append("\nCaused By:\n");
			b.append(serverError.getRemoteStackTrace());
		}
		re.setRootStackTrace(b.toString());
		return re;
	}

}
