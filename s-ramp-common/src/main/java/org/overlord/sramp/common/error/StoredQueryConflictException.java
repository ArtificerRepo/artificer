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
package org.overlord.sramp.common.error;

import org.overlord.sramp.common.error.SrampConflictException;
import org.overlord.sramp.common.i18n.Messages;

/**
 * Exception thrown when the user attempts to add a stored query that already exists.
 *
 * @author Brett Meyer
 */
public class StoredQueryConflictException extends SrampConflictException {

    private static final long serialVersionUID = 632263403445444191L;

    public StoredQueryConflictException(String queryName) {
        super(Messages.i18n.format("STOREDQUERY_ALREADY_EXISTS", queryName)); //$NON-NLS-1$
    }

}
