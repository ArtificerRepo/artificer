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
package org.overlord.sramp.integration.teiid.model;

/**
 * A Teiid model object.
 */
public interface TeiidModelObject {

    /**
     * Names of the default properties for a Teiid model object.
     */
    public interface PropertyId {

        /**
         * The Teiid model object identifier.
         */
        String MMUID = "mmuuid"; //$NON-NLS-1$

        /**
         * The Teiid model object name in source.
         */
        String NAME_IN_SOURCE = "nameInSource"; //$NON-NLS-1$

    }

}
