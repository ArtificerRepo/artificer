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

import org.jboss.aesh.cl.exception.CommandLineParserException;
import org.jboss.aesh.cl.parser.CommandLineParser;
import org.jboss.aesh.cl.parser.ParserGenerator;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.console.AeshInvocationProviders;
import org.jboss.aesh.console.InvocationProviders;
import org.jboss.aesh.console.command.completer.AeshCompleterInvocationProvider;
import org.jboss.aesh.console.command.converter.AeshConverterInvocationProvider;
import org.jboss.aesh.console.command.validator.AeshValidatorInvocationProvider;
import org.overlord.sramp.shell.api.ShellCommand;

/**
 * Class that populates an Aesh Command from the String pass as a param. It
 * parses the line and store the result into the command using aesh command line
 * parser.
 *
 * @author David Virgil Naranjo
 */
public class AeshPopulator {

    private final InvocationProviders invocationProviders = new AeshInvocationProviders(
            new AeshConverterInvocationProvider(), new AeshCompleterInvocationProvider(),
            new AeshValidatorInvocationProvider());

    /**
     * Populate command with Aesh Command Populator.
     * 
     * @param command
     *            the command
     * @param line
     *            the line
     * @throws CommandLineParserException
     *             the command line parser exception
     * @throws OptionValidatorException
     *             the option validator exception
     */
    public void populateCommand(ShellCommand command, String line) throws CommandLineParserException,
            OptionValidatorException {
        CommandLineParser parser = ParserGenerator.generateCommandLineParser(command.getClass());
        parser.getCommandPopulator().populateObject(command, parser.parse(line), invocationProviders, true);
    }
}
