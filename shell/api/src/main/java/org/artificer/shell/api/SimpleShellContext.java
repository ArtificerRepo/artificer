/*
 * Copyright 2013 JBoss Inc
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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * A simple implementation of {@link ShellContext}.  This implementation does not
 * implement any of the event handling.  The purpose of this class is to provide
 * a very simple implementation of the shell context in case users wish to execute
 * shell commands from outside the interactive shell runtime environment.  In other
 * words, it's useful when utilizing the shell command as a standalone Java app.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimpleShellContext implements ShellContext {

    private Map<QName, Object> variables = new HashMap<QName, Object>();

    /**
     * Constructor.
     */
    public SimpleShellContext() {
    }

    /**
     * @see ShellContext#setVariable(QName, Object)
     */
    @Override
    public void setVariable(QName name, Object object) {
        this.variables.put(name, object);
    }

    /**
     * @see ShellContext#setVariable(QName, Object, ShellContextVariableLifecycleHandler)
     */
    @Override
    public void setVariable(QName name, Object object, ShellContextVariableLifecycleHandler lifecycleHandler) {
        if (lifecycleHandler.onAdd(object)) {
            setVariable(name, object);
        }
    }

    /**
     * @see ShellContext#getVariable(QName)
     */
    @Override
    public Object getVariable(QName name) {
        return this.variables.get(name);
    }

    /**
     * @see ShellContext#removeVariable(QName)
     */
    @Override
    public Object removeVariable(QName name) {
        if (!this.variables.containsKey(name)) {
            return null;
        }
        return this.variables.remove(name);
    }

    /**
     * @see ShellContext#addHandler(ShellContextEventHandler)
     */
    @Override
    public void addHandler(ShellContextEventHandler handler) {
    }

    /**
     * @see ShellContext#removeHandler(ShellContextEventHandler)
     */
    @Override
    public void removeHandler(ShellContextEventHandler handler) {
    }

    /**
     * @see ShellContext#promptForInput(String)
     */
    @Override
    public String promptForInput(String prompt) {
        throw new RuntimeException("Not implemented."); //$NON-NLS-1$
    }

    /**
     * @see ShellContext#promptForPassword(String)
     */
    @Override
    public String promptForPassword(String prompt) {
        throw new RuntimeException("Not implemented."); //$NON-NLS-1$
    }
}
