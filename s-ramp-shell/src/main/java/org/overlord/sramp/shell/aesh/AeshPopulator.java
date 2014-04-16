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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.aesh.cl.exception.CommandLineParserException;
import org.jboss.aesh.cl.parser.CommandLineParser;
import org.jboss.aesh.cl.parser.ParserGenerator;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.console.AeshInvocationProviders;
import org.jboss.aesh.console.InvocationProviders;
import org.jboss.aesh.console.command.completer.AeshCompleterInvocationProvider;
import org.jboss.aesh.console.command.converter.AeshConverterInvocationProvider;
import org.jboss.aesh.console.command.validator.AeshValidatorInvocationProvider;
import org.overlord.sramp.shell.InteractiveShellCommandReader;
import org.overlord.sramp.shell.api.ShellCommand;

// TODO: Auto-generated Javadoc
/**
 * Class that populates an Aesh Command from the String pass as a param. It
 * parses the line and store the result into the command using aesh command line
 * parser.
 *
 * @author David Virgil Naranjo
 */
public class AeshPopulator {

    private final static String BEGINNNING_ALIAS_LINE = "alias ";

    private final InvocationProviders invocationProviders = new AeshInvocationProviders(
            new AeshConverterInvocationProvider(), new AeshCompleterInvocationProvider(),
            new AeshValidatorInvocationProvider());

    private final Map<String, String> aliases;

    /**
     * Instantiates a new aesh populator.
     *
     * @param aliasFile
     *            the alias file
     */
    public AeshPopulator(String aliasFile) {
        this.aliases = getAliases(aliasFile);
    }
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
        line = checkAlias(line);

        CommandLineParser parser = ParserGenerator.generateCommandLineParser(command.getClass());
        parser.getCommandPopulator().populateObject(command, parser.parse(line), invocationProviders, true);
    }

    /**
     * Gets the aliases.
     *
     * @param file
     *            the file
     * @return the aliases
     */
    private Map<String, String> getAliases(String file) {
        Map<String, String> aliases = new HashMap<String, String>();
        File aliasFile = null;

        try {
            aliasFile = new File(InteractiveShellCommandReader.class.getResource(file).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(aliasFile));
            String line = "";
            while ((line = br.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    if (line.startsWith(BEGINNNING_ALIAS_LINE)) {
                        String alias = line.substring(BEGINNNING_ALIAS_LINE.length());
                        if (alias.contains("=")) {
                            String[] splitted_alias = alias.split("=");
                            if (splitted_alias != null && splitted_alias.length == 2) {
                                aliases.put(splitted_alias[0], splitted_alias[1]);
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return aliases;
    }

    /**
     * Check alias.
     *
     * @param line
     *            the line
     * @return the string
     */
    private String checkAlias(String line) {
        String commandName = line.substring(0, line.indexOf(" ") + 1).trim();
        String rest = "";
        if (StringUtils.isEmpty(commandName)) {
            commandName = line;
        } else {
            rest = line.substring(line.indexOf(" ") + 1);
        }
        if (aliases.containsKey(commandName)) {
            return aliases.get(commandName) + " " + rest;
        } else {
            return line;
        }
    }

}
