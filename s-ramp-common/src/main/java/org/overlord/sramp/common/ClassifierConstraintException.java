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
package org.overlord.sramp.common;

import org.overlord.sramp.common.i18n.Messages;

/**
 * @author Brett Meyer.
 */
// TODO: SrampAlreadyExistsException needs renamed to a more general conflict-related exception.
public class ClassifierConstraintException extends SrampAlreadyExistsException {

    public ClassifierConstraintException(String uuid) {
        super(Messages.i18n.format("CLASSIFIER_CONSTRAINT", uuid));
    }
}