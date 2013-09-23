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
package org.overlord.sramp.shell.api;

import javax.xml.namespace.QName;

/**
 * The context available to all shell commands.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ShellContext {

	/**
	 * Sets a variable in the context.
	 * @param name
	 * @param object
	 */
	public void setVariable(QName name, Object object);

	/**
	 * Sets a variable in the context.
	 * @param name
	 * @param object
	 * @param lifecycleHandler
	 */
	public void setVariable(QName name, Object object, ShellContextVariableLifecycleHandler lifecycleHandler);

	/**
	 * Adds an event handler to the shell context.
	 * @param handler
	 */
	public void addHandler(ShellContextEventHandler handler);

	/***
	 * Removes an event handler from the shell context.
	 * @param handler
	 */
	public void removeHandler(ShellContextEventHandler handler);

	/**
	 * Gets a variable from the context.
	 * @param name
	 */
	public Object getVariable(QName name);

	/**
	 * Removes a variable from the context.  Returns the object that was removed.
	 * @param name
	 */
	public Object removeVariable(QName name);

	/**
	 * Prompts the user for some input.  Returns the text entered by the user.
	 * @param prompt
	 */
	public String promptForInput(String prompt);

	/**
	 * Prompts the user for a password.  Returns the text entered by the user.
	 * @param prompt
	 */
	public String promptForPassword(String prompt);

}
