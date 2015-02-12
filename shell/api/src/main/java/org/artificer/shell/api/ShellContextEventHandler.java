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

import javax.xml.namespace.QName;

/**
 * Implement this interface to listen for shell context events.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ShellContextEventHandler {

	/**
	 * Called when a variable is added to the context.
	 * @param variableName
	 * @param value
	 */
	public void onVariableAdded(QName variableName, Object value);

	/**
	 * Called when a variable is changed.
	 * @param variableName
	 */
	public void onVariableChanged(QName variableName, Object value);

	/***
	 * Called when a variable is removed.
	 * @param variableName
	 */
	public void onVariableRemoved(QName variableName);

}
