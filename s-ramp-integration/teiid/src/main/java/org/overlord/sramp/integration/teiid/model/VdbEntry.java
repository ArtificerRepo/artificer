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

import org.overlord.sramp.integration.teiid.model.VdbManifest.VdbManifestExtendedType;

/**
 * The Teiid VDB entry model object.
 */
public final class VdbEntry {

    /**
     * The VDB manifest (<code>vdb.xml</code>) identifiers related to entry elements.
     */
    public interface ManifestId extends Describable.XmlId, Propertied.XmlId {

        /**
         * The resource path identifier.
         */
        String PATH = "path"; //$NON-NLS-1$

    }

    /**
     * Entry artifact property names.
     */
    public interface PropertyId extends Describable.PropertyId, Propertied.XmlId {
        // no additional properties
    }

    /**
     * The artifact type of a Teiid VDB entry.
     */
    public static final VdbManifestExtendedType ARTIFACT_TYPE = VdbManifestExtendedType.ENTRY;

}
