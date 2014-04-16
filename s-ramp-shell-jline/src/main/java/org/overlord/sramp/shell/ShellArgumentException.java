/*
 * Copyright 2014 JBoss Inc
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

/**
 * Exception that is thrown when the arguments passed to the shell main are not
 * the correct ones.
 * 
 * @author David Virgil Naranjo
 */
public class ShellArgumentException extends Exception{


    /**
     *
     */
    private static final long serialVersionUID = 5579278117792194519L;
    private int argumentIndex;

    /**
     * Constructor.
     *
     * @param argumentIndex
     *            the argument index
     * @param message
     *            the message
     */
    public ShellArgumentException(int argumentIndex, String message) {
        super(message);
        this.setArgumentIndex(argumentIndex);
    }

    /**
     * Gets the argument index.
     *
     * @return the argumentIndex
     */
    public int getArgumentIndex() {
        return argumentIndex;
    }

    /**
     * Sets the argument index.
     *
     * @param argumentIndex
     *            the argumentIndex to set
     */
    public void setArgumentIndex(int argumentIndex) {
        this.argumentIndex = argumentIndex;
    }
}
