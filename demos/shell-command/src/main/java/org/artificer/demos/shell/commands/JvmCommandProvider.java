/*
 * Copyright 2013 JBoss Inc
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
package org.artificer.demos.shell.commands;

import java.util.HashMap;
import java.util.Map;

import org.artificer.shell.api.ShellCommand;
import org.artificer.shell.api.ShellCommandProvider;

/**
 * Demonstrates how to contribute custom commands to the S-RAMP interactive shell.
 *
 * @author eric.wittmann@redhat.com
 */
public class JvmCommandProvider implements ShellCommandProvider {

    /**
     * Constructor.
     */
    public JvmCommandProvider() {
    }

    @Override
    public String getNamespace() {
        return "jvm";
    }

    @Override
    public Map<String, Class<? extends ShellCommand>> provideCommands() {
        Map<String, Class<? extends ShellCommand>> rval = new HashMap<String, Class<? extends ShellCommand>>();
        rval.put("status", JvmStatusCommand.class);
        return rval;
    }

}
