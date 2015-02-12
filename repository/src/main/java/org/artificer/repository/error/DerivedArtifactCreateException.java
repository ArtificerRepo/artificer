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
package org.artificer.repository.error;

import org.artificer.common.ArtifactTypeEnum;
import org.artificer.common.error.ArtificerUserException;
import org.artificer.repository.i18n.Messages;

/**
 * Exception thrown the user attempts to create a derived artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class DerivedArtifactCreateException extends ArtificerUserException {

    private static final long serialVersionUID = -2247193241132739490L;

    /**
     * Constructor.
     */
    public DerivedArtifactCreateException() {
    }

    /**
     * Constructor.
     * @param artifactType
     */
    public DerivedArtifactCreateException(ArtifactTypeEnum artifactType) {
        super(Messages.i18n.format("CANNOT_CREATE_DERIVED_ARTY", artifactType)); //$NON-NLS-1$
    }

}
