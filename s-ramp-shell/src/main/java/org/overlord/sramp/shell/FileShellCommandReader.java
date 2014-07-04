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
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * An implementation of the {@link ShellCommandReader} that reads data from a
 * file.
 * 
 * @author eric.wittmann@redhat.com
 */
public class FileShellCommandReader extends AbstractShellCommandReader {

    private final String filePath;
    private BufferedReader fileReader;

    /**
     * Constructor.
     * 
     * @param factory the factory
     * @param context the context
     * @param args the args
     */
    public FileShellCommandReader(ShellCommandFactory factory, ShellContextImpl context, ShellArguments args) {
        super(factory, context, args);
        this.filePath = args.getBatchFilePath();
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
        File file = new File(filePath);
        if (!file.isFile()) {
            throw new FileNotFoundException(filePath);
        }
        this.fileReader = new BufferedReader(new InputStreamReader(FileUtils.openInputStream(file)));
    }

    /**
     * Read line.
     * 
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#readLine()
     */
    @Override
    protected String readLine() throws IOException {
        String line = null;
        do {
            line = fileReader.readLine();
            if (line == null)
                break;
        } while (line.startsWith("#") || line.trim().length() == 0); //$NON-NLS-1$
        return line;
    }

    /**
     * Close.
     * 
     * @throws IOException
     * @see org.overlord.sramp.common.shell.ShellCommandReader#close()
     */
    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(fileReader);
    }

    /**
     * Checks if is batch.
     * 
     * @return true, if is batch
     * @see org.overlord.sramp.shell.ShellCommandReader#isBatch()
     */
    @Override
    public boolean isBatch() {
        return true;
    }

    /**
     * Prompt for input.
     * 
     * @param prompt the prompt
     * @return the string
     * @see org.overlord.sramp.shell.ShellCommandReader#promptForInput(java.lang.String)
     */
    @Override
    public String promptForInput(String prompt) {
        Console console = System.console();
        if (console != null) {
            return console.readLine(prompt);
        } else {
            throw new RuntimeException(Messages.i18n.format("FileShellCommandReader.NoConsole")); //$NON-NLS-1$
        }
    }

    /**
     * Prompt for password.
     * 
     * @param prompt the prompt
     * @return the password
     * @see org.overlord.sramp.shell.ShellCommandReader#promptForPassword(java.lang.String)
     */
    @Override
    public String promptForPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            return new String(console.readPassword(prompt));
        } else {
            throw new RuntimeException(Messages.i18n.format("FileShellCommandReader.NoConsole")); //$NON-NLS-1$
        }
    }

}
