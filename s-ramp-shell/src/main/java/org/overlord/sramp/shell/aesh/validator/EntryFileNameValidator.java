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
package org.overlord.sramp.shell.aesh.validator;

import java.io.File;

import org.jboss.aesh.cl.validator.OptionValidator;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.console.command.validator.ValidatorInvocation;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Validates that the file name input String has the correct format without Path
 * Separators.
 * 
 * @author David Virgil Naranjo
 */
public class EntryFileNameValidator implements OptionValidator<ValidatorInvocation<String>> {

    /**
     * Instantiates a new entry file name validator.
     */
    public EntryFileNameValidator() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.aesh.cl.validator.OptionValidator#validate(org.jboss.aesh.console
     * .command.validator.ValidatorInvocation)
     */
    @Override
    public void validate(ValidatorInvocation<String> validatorInvocation) throws OptionValidatorException {
        if (validatorInvocation.getValue().contains(File.separatorChar + "")) {
            throw new OptionValidatorException(Messages.i18n.format(
                    "Archive.Entry.FileName.Format.Not.Correct",
                    validatorInvocation.getValue()));
        }
    }
}