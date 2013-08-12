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
public class SrampShell {

    private static final String LOCALE_PROPERTY = "s-ramp.shell.locale"; //$NON-NLS-1$

	/**
	 * Main entry point.
	 * @param args
	 */
	public static void main(String [] args) {
	    String locale_str = System.getProperty(LOCALE_PROPERTY);
	    if (locale_str != null) {
	        String lang = null;
	        String region = null;
	        String [] lsplit = locale_str.split("_"); //$NON-NLS-1$
	        if (lsplit.length > 0) {
	            lang = lsplit[0];
	        }
	        if (lsplit.length > 1) {
	            region = lsplit[1];
	        }
	        if (lang != null && region != null) {
	            Locale.setDefault(new Locale(lang, region));
	        } else if (lang != null) {
                Locale.setDefault(new Locale(lang));
	        }
	    }

		final SrampShell shell = new SrampShell();
		Thread shutdownHook = new Thread(new Runnable() {
			@Override
			public void run() {
				shell.shutdown();
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		try {
			shell.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.out.println(Messages.i18n.format("Shell.EXITING")); //$NON-NLS-1$
		}
	}

	private ShellCommandFactory factory = new ShellCommandFactory();
	private ShellContextImpl context = new ShellContextImpl();
	private ShellCommandReader reader;

	/**
	 * Constructor.
	 */
	public SrampShell() {
	}

	/**
	 * Runs the shell.
	 * @param args
	 * @throws Exception
	 */
	public void run(String[] args) throws Exception {
		reader = createCommandReader(args);
		displayWelcomeMessage();
		boolean done = false;
		while (!done) {
			ShellCommand command = null;
			try {
	            command = reader.read();
				if (command == null) {
					done = true;
				} else {
					command.execute();
				}
			} catch (InvalidCommandArgumentException e) {
				System.out.println(Messages.i18n.format("Shell.INVALID_ARG", e.getMessage())); //$NON-NLS-1$
				if (command != null) {
    				System.out.print(Messages.i18n.format("Shell.USAGE")); //$NON-NLS-1$
    				command.printUsage();
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	/**
	 * Creates an appropriate {@link ShellCommandReader} based on the command line
	 * arguments and the current runtime environment.
	 * @param args
	 * @throws IOException
	 */
	protected ShellCommandReader createCommandReader(String[] args) throws IOException {
		ShellCommandReader commandReader = null;
		if (args.length >= 2 && "-f".equals(args[0])) { //$NON-NLS-1$
			String filePath = args[1];
			commandReader = new FileShellCommandReader(factory, context, filePath);
		} else if (args.length == 1 && "-simple".equals(args[0])) { //$NON-NLS-1$
			if (System.console() != null) {
				commandReader = new ConsoleShellCommandReader(factory, context);
			} else {
				commandReader = new StdInShellCommandReader(factory, context);
			}
		} else {
			if (System.console() != null) {
				commandReader = new InteractiveShellCommandReader(factory, context);
			} else {
				commandReader = new StdInShellCommandReader(factory, context);
			}
		}
		commandReader.open();
		return commandReader;
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
				"  JBoss S-RAMP Kurt Stam and Eric Wittmann, Licensed under the\n" + //$NON-NLS-1$
				"  Apache License, V2.0, Copyright 2012\n" + //$NON-NLS-1$
				"  Locale: " + Locale.getDefault().toString().trim() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"**********************************************************************" //$NON-NLS-1$
				);
	}
}
