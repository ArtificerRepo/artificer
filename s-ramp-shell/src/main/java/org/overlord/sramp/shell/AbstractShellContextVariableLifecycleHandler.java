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

import org.overlord.sramp.shell.api.ShellContextVariableLifecycleHandler;

/**
 * Base class for shell context variable lifecycle handlers.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractShellContextVariableLifecycleHandler implements ShellContextVariableLifecycleHandler {

	/**
	 * Constructor.
	 */
	public AbstractShellContextVariableLifecycleHandler() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContextVariableLifecycleHandler#onAdd(java.lang.Object)
	 */
	@Override
	public boolean onAdd(Object object) {
		return true;
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContextVariableLifecycleHandler#onRemove(java.lang.Object)
	 */
	@Override
	public void onRemove(Object object) {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContextVariableLifecycleHandler#onContextDestroyed(java.lang.Object)
	 */
	@Override
	public void onContextDestroyed(Object object) {
	}

}
