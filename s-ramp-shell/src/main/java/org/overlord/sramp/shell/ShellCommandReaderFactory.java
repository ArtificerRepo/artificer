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

import java.io.IOException;

/**
 * Class that acts as factory method class. It includes the method that depends
 * the kind of arguments passed generates the specific implementation of the
 * ShellCommandReader
 *
 * @author David Virgil Naranjo
 */
public class ShellCommandReaderFactory {

    /**
     * Creates an appropriate {@link ShellCommandReader} based on the command
     * line arguments and the current runtime environment.
     *
     * @param args
     *            the args
     * @param factory
     *            the factory
     * @param context
     *            the context
     * @return the shell command reader
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static ShellCommandReader createCommandReader(ShellArguments args, ShellCommandFactory factory,
            ShellContextImpl context) throws IOException {
        ShellCommandReader commandReader = null;
        if (args.isSimple()) { //$NON-NLS-1$
            if (System.console() != null) {
                commandReader = new ConsoleShellCommandReader(factory, context, args);
            } else {
                commandReader = new StdInShellCommandReader(factory, context, args);
            }

        } else if (args.hasBatchFile()) { //$NON-NLS-1$
            commandReader = new FileShellCommandReader(factory, context, args);

        } else {
            if (System.console() != null) {
                commandReader = new InteractiveShellCommandReader(factory, context, args);
            } else {
                commandReader = new StdInShellCommandReader(factory, context, args);
            }
        }
        return commandReader;
    }

}
