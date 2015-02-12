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
package org.artificer.shell;

import org.artificer.shell.api.ShellContextVariableLifecycleHandler;

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

	@Override
	public boolean onAdd(Object object) {
		return true;
	}

	@Override
	public void onRemove(Object object) {
	}

	@Override
	public void onContextDestroyed(Object object) {
	}

}
