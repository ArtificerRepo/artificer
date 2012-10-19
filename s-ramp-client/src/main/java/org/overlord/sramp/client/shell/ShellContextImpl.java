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
package org.overlord.sramp.client.shell;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Implementation of the shell context.
 *
 * @author eric.wittmann@redhat.com
 */
public class ShellContextImpl implements ShellContext {

	private Map<QName, Object> variables = new HashMap<QName, Object>();
	private Map<QName, ShellContextVariableLifecycleHandler> variableLifecycleHandlers = new HashMap<QName, ShellContextVariableLifecycleHandler>();

	/**
	 * Constructor.
	 */
	public ShellContextImpl() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellContext#setVariable(javax.xml.namespace.QName, java.lang.Object)
	 */
	@Override
	public void setVariable(QName name, Object object) {
		this.variables.put(name, object);
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellContext#setVariable(javax.xml.namespace.QName, java.lang.Object, org.overlord.sramp.client.shell.ShellContextVariableLifecycleHandler)
	 */
	@Override
	public void setVariable(QName name, Object object, ShellContextVariableLifecycleHandler lifecycleHandler) {
		if (lifecycleHandler.onAdd(object)) {
			this.variables.put(name, object);
			this.variableLifecycleHandlers.put(name, lifecycleHandler);
		}
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellContext#getVariable(javax.xml.namespace.QName)
	 */
	@Override
	public Object getVariable(QName name) {
		return this.variables.get(name);
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellContext#removeVariable(javax.xml.namespace.QName)
	 */
	@Override
	public Object removeVariable(QName name) {
		if (!this.variables.containsKey(name)) {
			return null;
		}

		Object rval = this.variables.remove(name);
		ShellContextVariableLifecycleHandler handler = this.variableLifecycleHandlers.remove(name);
		if (handler != null) {
			handler.onRemove(rval);
		}
		return rval;
	}

	/**
	 * Called to destroy the context.
	 */
	public void destroy() {
		for (QName varName : this.variables.keySet()) {
			Object object = this.variables.get(varName);
			ShellContextVariableLifecycleHandler handler = this.variableLifecycleHandlers.get(varName);
			if (handler != null) {
				handler.onContextDestroyed(object);
			}
		}
	}

}
