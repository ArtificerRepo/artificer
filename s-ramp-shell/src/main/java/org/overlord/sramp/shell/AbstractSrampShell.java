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

import java.io.IOException;
import java.util.Locale;

import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.api.ShellCommand;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * An interactive shell for working with an s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractSrampShell {

    protected static final String LOCALE_PROPERTY = "s-ramp.shell.locale"; //$NON-NLS-1$



	private final ShellCommandFactory factory = new ShellCommandFactory();
	private final ShellContextImpl context = new ShellContextImpl();
	private ShellCommandReader reader;

	/**
	 * Constructor.
	 */
	public AbstractSrampShell() {
	}

	/**
	 * Runs the shell.
	 * @param args
	 * @throws Exception
	 */
	public void run(String[] args) throws Exception {
	    ShellArguments shellArgs = new ShellArguments(args);
        reader = ShellCommandReaderFactory.createCommandReader(shellArgs, factory, context);
        context.setReader(reader);
        reader.open();
		displayWelcomeMessage();
		boolean done = false;
		while (!done) {
			ShellCommand command = null;
			try {
	            command = reader.read();
				if (command == null) {
					done = true;
				} else {
					boolean success = command.execute();
					if (!success && reader.isBatch()) {
                        exit();
					}
				}
			} catch (InvalidCommandArgumentException e) {
				System.out.println(Messages.i18n.format("Shell.INVALID_ARG", e.getMessage())); //$NON-NLS-1$
				if (command != null) {
    				System.out.println(Messages.i18n.format("Shell.USAGE")); //$NON-NLS-1$
    				command.printUsage();
				}
				if (reader.isBatch())
                    exit();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				if (reader.isBatch())
                    exit();
			}
		}
	}

	/**
	 * Shuts down the shell.
	 */
	public void shutdown() {
		System.out.print(Messages.i18n.format("Shell.SHUTTING_DOWN")); //$NON-NLS-1$
		try { this.reader.close(); } catch (IOException e) { }
		this.context.destroy();
		System.out.println(Messages.i18n.format("Shell.DONE")); //$NON-NLS-1$
	}

	/**
	 * Displays a welcome message to the user.
	 */
	private void displayWelcomeMessage() {
		System.out.println(
				"**********************************************************************\n" + //$NON-NLS-1$
				"           _____       ______  ___ ___  ________  \n" + //$NON-NLS-1$
				"          /  ___|      | ___ \\/ _ \\|  \\/  | ___ \\ \n" + //$NON-NLS-1$
				"          \\ `--. ______| |_/ / /_\\ \\ .  . | |_/ / \n" + //$NON-NLS-1$
				"           `--. \\______|    /|  _  | |\\/| |  __/  \n" + //$NON-NLS-1$
				"          /\\__/ /      | |\\ \\| | | | |  | | |     \n" + //$NON-NLS-1$
				"          \\____/       \\_| \\_\\_| |_|_|  |_|_|     \n" + //$NON-NLS-1$
				"                                                  \n" + //$NON-NLS-1$
				"  Overlord S-RAMP, Licensed under Apache License V2.0, Copyright 2014\n" + //$NON-NLS-1$
				"  Locale: " + Locale.getDefault().toString().trim() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"**********************************************************************" //$NON-NLS-1$
				);
	}

    protected abstract void exit() throws Exception;
}
