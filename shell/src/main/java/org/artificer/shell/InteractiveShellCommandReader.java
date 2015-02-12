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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.namespace.QName;

import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleOutput;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.settings.Settings;
import org.artificer.shell.api.ShellContextEventHandler;

/**
 * An implementation of the {@link ShellCommandReader} that uses aesh to provide
 * a rich console experience to the user, complete with history, tab completion,
 * and ansi output.
 *
 * @author eric.wittmann@redhat.com
 */
public class InteractiveShellCommandReader extends AbstractShellCommandReader implements ShellContextEventHandler {

	private static final QName CLIENT_NAME = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$

    private Console consoleReader;

    private Prompt prompt;
	/**
	 * Constructor.
	 * @param factory
	 * @param context
     * @param args
	 */
	public InteractiveShellCommandReader(ShellCommandFactory factory, ShellContextImpl context, ShellArguments args) {
		super(factory, context, args);
		context.addHandler(this);
	}

	@Override
	public void open() throws IOException {
        Settings settings = Settings.getInstance();
        settings.setAliasEnabled(false);
        // settings.setAliasFile(new File("al"));
        settings.setEnablePipelineAndRedirectionParser(false);
        settings.setLogging(true);
        consoleReader = new Console(settings);

        String promptChar = defaultAnsiPrompt();
        prompt = new Prompt(promptChar);
        consoleReader.addCompletion(new TabCompleter(getFactory(), getContext()));
	}

	/**
	 * Creates the ANSI compatible prompt.
	 */
	private String defaultAnsiPrompt() {
		return "\033[1m\033[31martificer>\033[0m "; //$NON-NLS-1$
	}

	/**
	 * Creates the ANSI compatible prompt.
	 */
	private String connectedAnsiPrompt() {
		return "\033[1m\033[32martificer>\033[0m "; //$NON-NLS-1$
	}

	@Override
	protected String readLine() throws IOException {
	    // The #isRunning and output != null check are necessary to prevent exceptions when killing the shell w/o
	    // using exit (ctrl+c, etc.)
	    if (consoleReader.isRunning()) {
            ConsoleOutput output = consoleReader.read(prompt, null);
            if (output != null) {
                return output.getBuffer();
            }
	    }
	    
	    return ""; //$NON-NLS-1$
	}

	@Override
	protected Writer getCommandOutput() {
        return new OutputStreamWriter(Settings.getInstance().getStdOut());
	}

	@Override
	public void close() throws IOException {
        consoleReader.stop();
	}

	@Override
	public void onVariableAdded(QName variableName, Object value) {
		if (CLIENT_NAME.equals(variableName)) {
            prompt = new Prompt(connectedAnsiPrompt());
		}
	}

	@Override
	public void onVariableChanged(QName variableName, Object value) {
		// Nothing to do here
	}

	@Override
	public void onVariableRemoved(QName variableName) {
		if (CLIENT_NAME.equals(variableName)) {
            prompt = new Prompt(defaultAnsiPrompt());
		}
	}

	/**
	 * @see ShellCommandReader#promptForInput(java.lang.String)
	 */
	@Override
    public String promptForInput(String promptString) {
        String oldprompt = prompt.getPromptAsString();
	    try {
            return this.consoleReader.read(promptString).getBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            prompt = new Prompt(oldprompt);
        }
	}

	/**
	 * @see ShellCommandReader#promptForPassword(java.lang.String)
	 */
	@Override
	public String promptForPassword(String promptString) {
        String oldprompt = prompt.getPromptAsString();
        try {
            Prompt newPrompt = new Prompt(promptString);
            return this.consoleReader.read(newPrompt,Character.valueOf((char) 0)).getBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            prompt=new Prompt(oldprompt);
        }
	}

}
