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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * An implementation of the {@link ShellCommandReader} that uses standard input
 * to read commands typed in by the user.
 *
 * @author eric.wittmann@redhat.com
 */
public class StdInShellCommandReader extends AbstractShellCommandReader {

	private BufferedReader stdinReader;

	    /**
     * Constructor.
     *
     * @param factory
     *            the factory
     * @param context
     *            the context
     * @param args
     *            the args
     */
	public StdInShellCommandReader(ShellCommandFactory factory, ShellContextImpl context, ShellArguments args) {
		super(factory, context, args);
	}

	    /**
     * Open.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#open()
     */
	@Override
	public void open() throws IOException {
		stdinReader = new BufferedReader(new InputStreamReader(System.in));
	}

	    /**
     * Read line.
     *
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#readLine()
     */
	@Override
	protected String readLine() throws IOException {
		if (!stdinReader.ready()) {
			System.out.print("sramp> "); //$NON-NLS-1$
		}
		return stdinReader.readLine();
	}

	    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see org.overlord.sramp.common.shell.ShellCommandReader#close()
     */
	@Override
	public void close() throws IOException {
	}

	    /**
     * Prompt for input.
     *
     * @param prompt
     *            the prompt
     * @return the string
     * @see org.overlord.sramp.shell.ShellCommandReader#promptForInput(java.lang.String)
     */
	@Override
	public String promptForInput(String prompt) {
        try {
            if (!stdinReader.ready()) {
                System.out.print(prompt);
            }
            return stdinReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	    /**
     * Prompt for password.
     *
     * @param prompt
     *            the prompt
     * @return the string
     * @see org.overlord.sramp.shell.ShellCommandReader#promptForPassword(java.lang.String)
     */
	@Override
	public String promptForPassword(String prompt) {
	    // stdin doesn't support reading passwords
	    return promptForInput(prompt);
	}

}
