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
import org.overlord.sramp.integration.teiid.model.VdbManifest.VdbManifestRelationshipType;

/**
 * The Teiid VDB translator model object.
 */
public final class VdbTranslator {

    /**
     * The VDB manifest (<code>vdb.xml</code>) identifiers related to translator elements.
     */
    public interface ManifestId extends Describable.XmlId, Propertied.XmlId {

        /**
         * The translator name attribute identifier.
         */
        String NAME = "name"; //$NON-NLS-1$

        /**
         * The translator type attribute identifier.
         */
        String TYPE = "type"; //$NON-NLS-1$

    }

    /**
     * Translator artifact property names.
     */
    public interface PropertyId extends Describable.PropertyId, Propertied.XmlId {

        /**
         * The translator type.
         */
        String TYPE = "translatorType"; //$NON-NLS-1$

    }

    /**
     * The artifact type of a Teiid VDB translator.
     */
    public static final TeiidExtendedType ARTIFACT_TYPE = VdbManifestExtendedType.TRANSLATOR;

    /**
     * A relationship between a translator artifact and the source artifacts that reference it.
     */
    public static final TeiidRelationshipType SOURCES_RELATIONSHIP = VdbManifestRelationshipType.TRANSLATOR_TO_SOURCES;

}
