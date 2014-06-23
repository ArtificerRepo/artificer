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
 * Indicates the Teiid object may have generic properties.
 */
public interface Propertied {

    /**
     * The artifact property identifiers.
     */
    public interface PropertyId {

        /**
         * The artifact property identifier.
         */
        String PROPERTY = "property"; //$NON-NLS-1$

    }

    /**
     * The identifiers related to XML element descriptions.
     */
    public interface XmlId {

        /**
         * The property name attribute identifier.
         */
        String NAME = "name"; //$NON-NLS-1$

        /**
         * The property element identifier.
         */
        String PROPERTY = "property"; //$NON-NLS-1$

        /**
         * The property value attribute identifier.
         */
        String VALUE = "value"; //$NON-NLS-1$

    }

}
