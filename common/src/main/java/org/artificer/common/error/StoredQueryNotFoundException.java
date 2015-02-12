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
 * Exception thrown when the user attempts to access or modify a stored query that
 * does not exist in the repository.
 *
 * @author Brett Meyer
 */
public class StoredQueryNotFoundException extends ArtificerNotFoundException {

    private static final long serialVersionUID = 6962141373474710343L;

    public StoredQueryNotFoundException() {
    }

    public StoredQueryNotFoundException(String queryName) {
        super(Messages.i18n.format("STOREDQUERY_NOT_FOUND", queryName)); //$NON-NLS-1$
    }

}
