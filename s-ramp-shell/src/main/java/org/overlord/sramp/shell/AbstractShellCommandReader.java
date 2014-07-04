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

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.overlord.sramp.shell.api.Arguments;
import org.overlord.sramp.shell.api.ShellCommand;
import org.overlord.sramp.shell.api.ShellContext;
import org.overlord.sramp.shell.commands.NoOpCommand;

/**
 * A base class for all shell command readers.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractShellCommandReader implements ShellCommandReader {

	private final ShellContext context;
	private final ShellCommandFactory factory;
    private final ShellArguments args;

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
	public AbstractShellCommandReader(ShellCommandFactory factory, ShellContext context, ShellArguments args) {
		this.factory = factory;
		this.context = context;
		this.args = args;
		
		if (args.hasLogFile()) {
		    try {
	            // truncate it in case it already exists
                new FileWriter(args.getLogFilePath(), false).write("");
            } catch (IOException e) {
                // TODO
            };
		}
	}

	/**
     * Open.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see org.overlord.sramp.common.shell.ShellCommandReader#open()
     */
	@Override
	public abstract void open() throws IOException;

	/**
     * Read.
     *
     * @return the shell command
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.common.shell.ShellCommandReader#read()
     */
	@Override
	public final ShellCommand read() throws Exception {
		String line = readLine();
		if (line == null) {
			return null;
		}
		
		if (args.hasLogFile()) {
		    FileWriter writer = new FileWriter(args.getLogFilePath(), true);
		    writer.append(line + "\n");
		    writer.close();
		}
		
        line = interpolate(line, args.getPropertiesFromFile());
		Arguments arguments = new Arguments(line);
		if (arguments.isEmpty()) {
			return new NoOpCommand();
		}

		// The first argument is the qualified command name.
		QName commandName = arguments.removeCommandName();

		// Create the command.
		ShellCommand command = factory.createCommand(commandName);
		command.setContext(this.context);
		command.setArguments(arguments);
		command.setOutput(getCommandOutput());
		return command;
	}

    /**
     * Interpolate any properties that might be present on the line, using values
     * from the given map.
     *
     * @param line the line
     * @return the string
     */
    protected static String interpolate(String line, Map<String, String> properties) {
        String interpolated = line;
        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String match = matcher.group();
            String keyWithOptionalDefault = match.substring(2, match.length() - 1);
            int idx = keyWithOptionalDefault.indexOf(':');
            String key = keyWithOptionalDefault;
            String defaultVal = null;
            if (idx > 0) {
                key = keyWithOptionalDefault.substring(0, idx);
                defaultVal = keyWithOptionalDefault.substring(idx + 1);
            }
            if (properties.containsKey(key)) {
                interpolated = interpolated.replace(match, properties.get(key));
            } else if (System.getProperties().containsKey(key)) {
                interpolated = interpolated.replace(match, System.getProperty(key));
            } else if (defaultVal != null) {
                interpolated = interpolated.replace(match, defaultVal);
            }
        }
        return interpolated;
    }

	/**
     * Gets the output stream that should be used by commands when they need to
     * print a message to the console.
     *
     * @return the command output
     */
	protected Writer getCommandOutput() {
		return new OutputStreamWriter(System.out);
	}

	/**
     * Reads a single line from the input source (e.g. user input) and returns
     * it.
     *
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
	protected abstract String readLine() throws IOException;

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
     * Gets the factory.
     *
     * @return the factory
     */
	public ShellCommandFactory getFactory() {
		return factory;
	}

    /**
     * Gets the context.
     *
     * @return the context
     */
    public ShellContext getContext() {
        return context;
    }

    /**
     * Gets the args.
     *
     * @return the args
     */
    public ShellArguments getArgs() {
        return args;
    }

	/**
     * Checks if is batch.
     *
     * @return true, if is batch
     * @see org.overlord.sramp.shell.ShellCommandReader#isBatch()
     */
	@Override
	public boolean isBatch() {
	    return false;
	}

}
