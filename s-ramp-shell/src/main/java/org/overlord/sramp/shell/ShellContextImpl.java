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
package org.overlord.sramp.shell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.overlord.sramp.shell.api.ShellContext;
import org.overlord.sramp.shell.api.ShellContextEventHandler;
import org.overlord.sramp.shell.api.ShellContextVariableLifecycleHandler;

/**
 * Implementation of the shell context.
 *
 * @author eric.wittmann@redhat.com
 */
public class ShellContextImpl implements ShellContext {

	private Map<QName, Object> variables = new HashMap<QName, Object>();
	private Map<QName, ShellContextVariableLifecycleHandler> variableLifecycleHandlers = new HashMap<QName, ShellContextVariableLifecycleHandler>();
	private Set<ShellContextEventHandler> eventHandlers = new HashSet<ShellContextEventHandler>();
	private ShellCommandReader reader;

	/**
	 * Constructor.
	 */
	public ShellContextImpl() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContext#setVariable(javax.xml.namespace.QName, java.lang.Object)
	 */
	@Override
	public void setVariable(QName name, Object object) {
		boolean isUpdate = this.variables.containsKey(name);
		this.variables.put(name, object);
		if (isUpdate) {
			fireChangeEvent(name, object);
		} else {
			fireAddEvent(name, object);
		}
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContext#setVariable(javax.xml.namespace.QName, java.lang.Object, org.overlord.sramp.shell.api.shell.ShellContextVariableLifecycleHandler)
	 */
	@Override
	public void setVariable(QName name, Object object, ShellContextVariableLifecycleHandler lifecycleHandler) {
		if (lifecycleHandler.onAdd(object)) {
			setVariable(name, object);
			this.variableLifecycleHandlers.put(name, lifecycleHandler);
		}
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContext#getVariable(javax.xml.namespace.QName)
	 */
	@Override
	public Object getVariable(QName name) {
		return this.variables.get(name);
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContext#removeVariable(javax.xml.namespace.QName)
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
		fireRemoveEvent(name);
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

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContext#addHandler(org.overlord.sramp.shell.api.shell.ShellContextEventHandler)
	 */
	@Override
	public void addHandler(ShellContextEventHandler handler) {
		this.eventHandlers.add(handler);
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContext#removeHandler(org.overlord.sramp.shell.api.shell.ShellContextEventHandler)
	 */
	@Override
	public void removeHandler(ShellContextEventHandler handler) {
		this.eventHandlers.remove(handler);
	}

	/**
	 * Fires the add event.
	 * @param name
	 * @param object
	 */
	private void fireAddEvent(QName name, Object object) {
		for (ShellContextEventHandler handler : eventHandlers) {
			handler.onVariableAdded(name, object);
		}
	}

	/**
	 * Fires the change event.
	 * @param name
	 * @param object
	 */
	private void fireChangeEvent(QName name, Object object) {
		for (ShellContextEventHandler handler : eventHandlers) {
			handler.onVariableChanged(name, object);
		}
	}

	/**
	 * Fires the remove event.
	 * @param name
	 */
	private void fireRemoveEvent(QName name) {
		for (ShellContextEventHandler handler : eventHandlers) {
			handler.onVariableRemoved(name);
		}
	}

	/**
	 * @see org.overlord.sramp.shell.api.ShellContext#promptForInput(java.lang.String)
	 */
	@Override
	public String promptForInput(String prompt) {
	    return getReader().promptForInput(prompt);
	}

	/**
	 * @see org.overlord.sramp.shell.api.ShellContext#promptForPassword(java.lang.String)
	 */
	@Override
	public String promptForPassword(String prompt) {
	    return getReader().promptForPassword(prompt);
	}

    /**
     * @return the reader
     */
    protected ShellCommandReader getReader() {
        return reader;
    }

    /**
     * @param reader the reader to set
     */
    public void setReader(ShellCommandReader reader) {
        this.reader = reader;
    }

}
