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
package org.artificer.shell.core;

import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * Disconnects from the current S-RAMP repository.
 *
 * @author Brett Meyer
 */
@CommandDefinition(name = "disconnect",
		description = "The \"disconnect\" command disconnects from the currently active Artificer repository.\n")
public class DisconnectCommand extends AbstractCommand {

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		context(commandInvocation).setClient(null);
		commandInvocation.getShell().out().println(Messages.i18n.format("Disconnect.Success"));
        return CommandResult.SUCCESS;
	}

	@Override
	protected String getName() {
		return "disconnect";
	}

}
