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
import java.io.IOException;

import org.jboss.aesh.cl.validator.OptionValidator;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.console.command.validator.ValidatorInvocation;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Validates that the File input is a file and not a directory.
 * 
 * @author David Virgil Naranjo
 */
public class FileValidator implements OptionValidator<ValidatorInvocation<File>> {

    /**
     * Instantiates a new file validator.
     */
    public FileValidator() {

    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.cl.validator.OptionValidator#validate(org.jboss.aesh.console.command.validator.ValidatorInvocation)
     */
    @Override
    public void validate(ValidatorInvocation<File> validatorInvocation) throws OptionValidatorException {
        try {
            checkIsFile(validatorInvocation.getValue());
        } catch (InvalidCommandArgumentException e) {
            throw new OptionValidatorException(e.getMessage());
        }
    }

    /**
     * Check is file.
     *
     * @param path
     *            the path
     * @throws InvalidCommandArgumentException
     *             the invalid command argument exception
     */
    public static void checkIsFile(File path) throws InvalidCommandArgumentException {
        if (!path.isFile()) {
            String fileName = "";

            try {
                fileName = path.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }

            throw new InvalidCommandArgumentException(Messages.i18n.format(
                    "Validator.OutputFileDoesNotExist", fileName)); //$NON-NLS-1$
        }
    }
}