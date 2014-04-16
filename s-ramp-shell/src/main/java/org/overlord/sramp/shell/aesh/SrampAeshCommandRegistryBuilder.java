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
package org.overlord.sramp.shell.aesh;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.overlord.sramp.shell.ShellCommandFactory;
import org.overlord.sramp.shell.api.ShellCommand;

/**
 * Util class that returns an Aesh Command Registry Builder class that contains
 * all the commands that can be processed by the shell. It uses the S-ramp
 * ShellCommandFactory to read the commands and store into aesh
 *
 * @author David Virgil Naranjo
 */
public class SrampAeshCommandRegistryBuilder {

    /**
     * Gets the commmand registy builder.
     *
     * @param commandFactory
     *            the command factory
     * @return the commmand registy builder
     */
    public static AeshCommandRegistryBuilder getCommmandRegistyBuilder(ShellCommandFactory commandFactory) {
        // Object to be returned
        AeshCommandRegistryBuilder builder = new AeshCommandRegistryBuilder();

        // Gets the commands from the s-ramp shell commands factory
        Map<QName, Class<? extends ShellCommand>> commands = commandFactory.getCommands();

        // Store the commands into the object to be returned
        Set<Class<? extends ShellCommand>> commandsAdded=new HashSet<Class<? extends ShellCommand>>();
        for (QName key : commands.keySet()) {
            if (!commandsAdded.contains(commands.get(key))) {
                builder.command(commands.get(key));
                commandsAdded.add(commands.get(key));
            }
        }
        // Store the standard commands (help, quit...)
        Map<QName, Class<? extends ShellCommand>> standardCommands = commandFactory.getStandardCommands();
        for (QName key : standardCommands.keySet()) {
            if (!commandsAdded.contains(standardCommands.get(key))) {
                builder.command(standardCommands.get(key));
                commandsAdded.add(standardCommands.get(key));
            }
        }

        return builder;
    }
}
