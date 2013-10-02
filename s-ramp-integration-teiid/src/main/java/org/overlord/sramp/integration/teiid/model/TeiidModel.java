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
 * The Teiid model file artifact. Also called a schema, the model usually has a <code>*.xmi</code> file extension.
 */
public final class TeiidModel implements TeiidModelObject {

    /**
     * The Teiid model artifact property names.
     */
    public interface PropertyId extends Describable.PropertyId, TeiidModelObject.PropertyId {

        /**
         * The max set size property name of a model artifact.
         */
        String MAX_SET_SIZE = "maxSetSize"; //$NON-NLS-1$

        /**
         * The model type property name of the model artifact.
         */
        String MODEL_TYPE = "modelType"; //$NON-NLS-1$

        /**
         * The primary metamodel URI property name of the model artifact.
         */
        String PRIMARY_METAMODEL_URI = "primaryMetamodelUri"; //$NON-NLS-1$

        /**
         * The producer name property name of the model artifact.
         */
        String PRODUCER_NAME = "producerName"; //$NON-NLS-1$

        /**
         * The producer version property name of the model artifact.
         */
        String PRODUCER_VERSION = "producerVersion"; //$NON-NLS-1$

        /**
         * The model visibility flag property name of the model artifact.
         */
        String VISIBLE = "visible"; //$NON-NLS-1$

    }

    /**
     * The model (<code>*.xmi</code>) related identifiers.
     */
    public interface XmiId extends Describable.XmlId {

        /**
         * The max set size attribute of a model annotation.
         */
        String MAX_SET_SIZE = "maxSetSize"; //$NON-NLS-1$

        /**
         * The model annotation element identifier. One and only one is required.
         */
        String MODEL_ANNOTATION = "mmcore:ModelAnnotation"; //$NON-NLS-1$

        /**
         * The model type attribute of a model annotation.
         */
        String MODEL_TYPE = "modelType"; //$NON-NLS-1$

        /**
         * The name in source of a model annotation.
         */
        String NAME_IN_SOURCE = "nameInSource"; //$NON-NLS-1$

        /**
         * The primary metamodel URI attribute of a model annotation.
         */
        String PRIMARY_METAMODEL_URI = "primaryMetamodelUri"; //$NON-NLS-1$

        /**
         * The producer name attribute of a model annotation.
         */
        String PRODUCER_NAME = "ProducerName"; //$NON-NLS-1$

        /**
         * The producer version attribute of a model annotation.
         */
        String PRODUCER_VERSION = "ProducerVersion"; //$NON-NLS-1$

        /**
         * The XMI root element of a model.
         */
        String ROOT_ELEMENT = "XMI"; //$NON-NLS-1$

        /**
         * The UUID attribute of a model annotation.
         */
        String UUID = "xmi:uuid"; //$NON-NLS-1$

        /**
         * The visibility flag attribute.
         */
        String VISIBLE = "visible"; //$NON-NLS-1$

        /**
         * The XML namespace attribute.
         */
        String XML_NAMESPACE = "xmlns"; //$NON-NLS-1$

    }

    /**
     * The artifact type of a Teiid model.
     */
    public static final TeiidExtendedType ARTIFACT_TYPE = TeiidArtifactType.MODEL;

    /**
     * The file extension, including the '.' prefix, of a Teiid model. Value is {@value} .
     */
    public static final String FILE_EXT = ".xmi"; //$NON-NLS-1$

}
