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
package org.artificer.common.error;

import org.artificer.common.i18n.Messages;

/**
 * Thrown when an artifact is published to the incorrect model.
 * 
 * @author Brett Meyer
 */
public class WrongModelException extends ArtificerUserException {

    private static final long serialVersionUID = -1006199656016992864L;

    public WrongModelException(String expectedType, String receivedType) {
        super(Messages.i18n.format("WRONG_MODEL", expectedType, receivedType)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
