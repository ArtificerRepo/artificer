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
	 * @param factory
	 * @param context
	 */
	public StdInShellCommandReader(ShellCommandFactory factory, ShellContextImpl context) {
		super(factory, context);
	}

	/**
	 * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#open()
	 */
	@Override
	public void open() throws IOException {
		stdinReader = new BufferedReader(new InputStreamReader(System.in));
	}

	/**
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
	 * @see org.overlord.sramp.common.shell.ShellCommandReader#close()
	 */
	@Override
	public void close() throws IOException {
	}

}
