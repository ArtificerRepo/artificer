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
package org.overlord.sramp.common;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.common.i18n.Messages;

/**
 * A class representing all of the Artifact Types defined by S-RAMP.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactType implements Serializable {

    /**
     * Returns true if the given artifact type is valid.  It must be alphanumeric only.
     * @param artifactType the artifact type
     * @return true if valid
     */
    public static final boolean isValid(String artifactType) {
        for (int i = 0; i < artifactType.length(); i++) {
            char c = artifactType.charAt(i);
            if (!(Character.isLetter(c) || Character.isDigit(c))) {
                return false;
            }
        }
        return true;
    }
    
    public static final ArtifactType Document() {
        return new ArtifactType(ArtifactTypeEnum.Document, "application/octet-stream"); //$NON-NLS-1$
    }
    public static final ArtifactType Document(String mimeType) {
        return new ArtifactType(ArtifactTypeEnum.Document, mimeType);
    }
    public static final ArtifactType XmlDocument() {
        return new ArtifactType(ArtifactTypeEnum.XmlDocument, "application/xml"); //$NON-NLS-1$
    }
    public static final ArtifactType XsdDocument() {
        return new ArtifactType(ArtifactTypeEnum.XsdDocument, "application/xml"); //$NON-NLS-1$
    }
    public static final ArtifactType WsdlDocument() {
        return new ArtifactType(ArtifactTypeEnum.WsdlDocument, "application/xml"); //$NON-NLS-1$
    }
    public static final ArtifactType PolicyDocument() {
        return new ArtifactType(ArtifactTypeEnum.PolicyDocument, "application/xml"); //$NON-NLS-1$
    }
    public static final ArtifactType ExtendedArtifactType(String extendedType, boolean derived) {
        ArtifactType at = new ArtifactType(ArtifactTypeEnum.ExtendedArtifactType, null);
        at.setExtendedType(extendedType);
        at.setExtendedDerivedType(derived);
        return at;
    }
    public static final ArtifactType ExtendedDocument(String extendedType) {
        ArtifactType at = new ArtifactType(ArtifactTypeEnum.ExtendedDocument, null);
        at.setExtendedType(extendedType);
        return at;
    }

    private ArtifactTypeEnum artifactType;
    private String mimeType;
    /** for a Extended Type, the type should be stored here */
    private String extendedType;
    private boolean extendedDerivedType;

    /**
     * Constructor.
     * @param artifactType
     * @param mimeType
     */
    private ArtifactType(ArtifactTypeEnum artifactType, String mimeType) {
        setArtifactType(artifactType);
        // TODO: This needs re-thought.  The mimeType should only be non-null when artifactType.isDocument().  But
        // if you let null happen, you'll eventually get NPEs within RESTEasy.
        if (mimeType == null) {
            if (artifactType == ArtifactTypeEnum.Document || artifactType == ArtifactTypeEnum.ExtendedDocument) {
                mimeType = "application/octet-stream"; //$NON-NLS-1$
            } else {
                mimeType = "application/xml"; //$NON-NLS-1$
            }
        }
        setMimeType(mimeType);
    }

    /**
     * Called to unwrap the S-RAMP artifact from its wrapper.
     * @param artifactWrapper the S-RAMP artifact wrapper
     * @return the specific artifact based on type
     */
    public BaseArtifactType unwrap(Artifact artifactWrapper) {
        try {
            Method method = Artifact.class.getMethod("get" + getArtifactType().getType()); //$NON-NLS-1$
            BaseArtifactType artifact = (BaseArtifactType) method.invoke(artifactWrapper);
            artifact.setArtifactType(this.getArtifactType().getApiType());
            return artifact;
        } catch (Exception e) {
            throw new RuntimeException(Messages.i18n.format("ARTIFACT_UNWRAP_ERROR", getArtifactType().getType()), e); //$NON-NLS-1$
        }
    }

    /**
     * Figures out the artifact type (enum) from the given S-RAMP artifact type string.
     * @param artifactType
     */
    public static ArtifactType valueOf(String artifactType) {
        return valueOf(artifactType, false);
    }

    public static ArtifactType valueOf(String artifactType, boolean isDocument) {
        if (StringUtils.isEmpty(artifactType)) {
            return null;
        } else if (ArtifactTypeEnum.hasEnum(artifactType)) {
            ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.valueOf(artifactType);
            return new ArtifactType(artifactTypeEnum, null);
        } else {
            ArtifactTypeEnum artifactTypeEnum;
            if (isDocument) {
                artifactTypeEnum = ArtifactTypeEnum.ExtendedDocument;
            } else {
                artifactTypeEnum = ArtifactTypeEnum.ExtendedArtifactType;
            }
            ArtifactType rval = new ArtifactType(artifactTypeEnum, null);
            rval.setExtendedType(artifactType);
            rval.setMimeType("application/octet-stream"); //$NON-NLS-1$
            return rval;
        }
    }

    /**
     * Determines the ArtifactType from the model and type. If the model is "ext"
     * (extended) then the artifactType can be either ExtendedArtifactType or
     * ExtendedDocument. All extended artifacts that have contend inherit from
     * ExtendedDocument. 
     * 
     * @param model - the model, for example core, xsd, wsdl, ext, etc.
     * @param type - the type, for example XmlDocument, WsdlDocument
     * @param isDocument - this flag is taken into account ONLY when the model is 'ext',
     * in this case the 'type' can be ExtendedDocument,
     * ExtendedArtifactType, or a user defined value (for example 'SwitchYardApplication').
     * The flag 'isDocument' is required only if the type is user defined so it can 
     * distinguish between an artifact with (true) or without (false) content. 
     * If this is not known at the time of calling a value of null can be given which will 
     * default the ArtifactType of ExtendedArtifactType.
     * 
     * @return ArtifactType
     */
    public static ArtifactType valueOf(String model, String type, Boolean isDocument) {
        ArtifactType artifactType = null;
        if ("ext".equals(model)) { //$NON-NLS-1$
        	ArtifactTypeEnum artifactTypeEnum = null;
            if  ( type.equals(ArtifactTypeEnum.ExtendedDocument.getType()) || 
            		( !type.equals(ArtifactTypeEnum.ExtendedArtifactType.getType()) && isDocument!=null && isDocument ) ) {
            	artifactTypeEnum = ArtifactTypeEnum.ExtendedDocument;
            } else {
        		artifactTypeEnum = ArtifactTypeEnum.ExtendedArtifactType;
            }
            artifactType = new ArtifactType(artifactTypeEnum, null);
            artifactType.setExtendedType(type);
        } else {
            ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.valueOf(type);
            artifactType = new ArtifactType(artifactTypeEnum, null);
        }
        return artifactType;
    }

    /**
     * Figures out the type from the artifact instance.
     * @param artifact
     */
    public static ArtifactType valueOf(BaseArtifactType artifact) {
        BaseArtifactEnum apiType = artifact.getArtifactType();
        // First, figure it out by the ArtifactType enum on the object
        if (apiType != null) {
            ArtifactType artifactType = valueOf(apiType);
            if (DocumentArtifactType.class.isAssignableFrom(artifact.getClass())) {
                artifactType.setMimeType(((DocumentArtifactType)artifact).getContentType());
            }
            if (artifactType.isExtendedType()) {
                if ((artifact.getOtherAttributes().keySet().contains(SrampConstants.SRAMP_CONTENT_TYPE_QNAME))) {
                    String contentTypeStr = artifact.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME);
                    artifactType.setMimeType(contentTypeStr);
                }
                String extendedDerived = artifact.getOtherAttributes().get(SrampConstants.SRAMP_DERIVED_QNAME);
                artifactType.setExtendedDerivedType("true".equals(extendedDerived)); //$NON-NLS-1$
            }
            if (artifactType.getArtifactType() == ArtifactTypeEnum.ExtendedArtifactType) {
                String extendedType = ((ExtendedArtifactType) artifact).getExtendedType();
                artifactType.setExtendedType(extendedType);
            } else if (artifactType.getArtifactType() == ArtifactTypeEnum.ExtendedDocument) {
                String extendedType = ((ExtendedDocument) artifact).getExtendedType();
                artifactType.setExtendedType(extendedType);
            }
            return artifactType;
        }
        // If that didn't work, then iterate through and test against all possible artifact types.
        ArtifactTypeEnum[] values = ArtifactTypeEnum.values();
        for (ArtifactTypeEnum artifactTypeEnum : values) {
            if (artifactTypeEnum.getTypeClass().equals(artifact.getClass())) {
                ArtifactType artifactType = new ArtifactType(artifactTypeEnum, null);
                if (artifactTypeEnum == ArtifactTypeEnum.ExtendedArtifactType || artifactTypeEnum == ArtifactTypeEnum.ExtendedDocument) {
                    if ((artifact.getOtherAttributes().keySet().contains(SrampConstants.SRAMP_CONTENT_TYPE_QNAME))) {
                        String contentTypeStr = artifact.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME);
                        artifactType.setMimeType(contentTypeStr);
                    }
                    String extendedType = (artifact instanceof ExtendedArtifactType) ? ((ExtendedArtifactType) artifact)
                            .getExtendedType() : ((ExtendedDocument) artifact).getExtendedType();
                    String extendedDerived = artifact.getOtherAttributes().get(SrampConstants.SRAMP_DERIVED_QNAME);
                    artifactType.setExtendedType(extendedType);
                    artifactType.setExtendedDerivedType("true".equals(extendedDerived)); //$NON-NLS-1$
                }
                return artifactType;
            }
        }
        throw new RuntimeException(Messages.i18n.format("ARTIFACT_TYPE_FROM_CLASS_ERROR", artifact.getClass())); //$NON-NLS-1$
    }

    /**
     * Instantiates an S-RAMP artifact of the correct type, and populates the artifactType and the
     * contentType.
     * @param artifactType
     */
    public BaseArtifactType newArtifactInstance() {
        try {
            BaseArtifactType baseArtifactType = getArtifactType().getTypeClass().newInstance();
            baseArtifactType.setArtifactType(getArtifactType().getApiType());
            if (DocumentArtifactType.class.isAssignableFrom(baseArtifactType.getClass())) {
                ((DocumentArtifactType) baseArtifactType).setContentType(getMimeType());
            }
            if (getArtifactType() == ArtifactTypeEnum.ExtendedArtifactType) {
                baseArtifactType.getOtherAttributes().put(SrampConstants.SRAMP_CONTENT_TYPE_QNAME, getMimeType());
                ((ExtendedArtifactType) baseArtifactType).setExtendedType(getExtendedType());
            }
            if (getArtifactType() == ArtifactTypeEnum.ExtendedDocument) {
                baseArtifactType.getOtherAttributes().put(SrampConstants.SRAMP_CONTENT_TYPE_QNAME, getMimeType());
                ((ExtendedDocument) baseArtifactType).setExtendedType(getExtendedType());
            }
            return baseArtifactType;
        } catch (Exception e) {
            throw new RuntimeException(Messages.i18n.format("ARTIFACT_INSTANTIATION_ERROR", getArtifactType().getTypeClass()), e); //$NON-NLS-1$
        }
    }

    /**
     * Figures out the type from the s-ramp API type.
     * @param apiType
     */
    public static ArtifactType valueOf(BaseArtifactEnum apiType) {
        ArtifactTypeEnum[] values = ArtifactTypeEnum.values();
        for (ArtifactTypeEnum artifactType : values) {
            if (artifactType.getApiType() == apiType) {
                return new ArtifactType(artifactType, null);
            }
        }
        throw new RuntimeException(Messages.i18n.format("ARTIFACT_TYPE_FROM_APITYPE_ERROR", apiType.value())); //$NON-NLS-1$
    }

    /**
     * @return the artifactType
     */
    public ArtifactTypeEnum getArtifactType() {
        return artifactType;
    }

    /**
     * @return true if the type is a {@link ExtendedArtifactType}.
     */
    public boolean isExtendedType() {
        return getArtifactType() == ArtifactTypeEnum.ExtendedArtifactType || getArtifactType() == ArtifactTypeEnum.ExtendedDocument;
    }

    /**
     * @return true if the artifact is a document artifact
     */
    public boolean isDocument() {
        if (isDerived()) {
            return false;
        } else {
            return getArtifactType().isDocument();
        }
    }

    /**
     * @return true if the artifact is derived
     */
    public boolean isDerived() {
        return getArtifactType().isDerived() || isExtendedDerivedType();
    }

    /**
     * @param artifactType the artifactType to set
     */
    public void setArtifactType(ArtifactTypeEnum artifactType) {
        this.artifactType = artifactType;
    }

    /**
     * @return the artifact model
     */
    public String getModel() {
        return getArtifactType().getModel();
    }

    /**
     * @return the artifact type
     */
    public String getType() {
        if (getArtifactType() == ArtifactTypeEnum.ExtendedArtifactType || getArtifactType() == ArtifactTypeEnum.ExtendedDocument) {
            return getExtendedType();
        } else {
            return getArtifactType().getType();
        }
    }

    /**
     * @return the artifact type label
     */
    public String getLabel() {
        return getArtifactType().getLabel();
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("/s-ramp/%1$s/%2$s (%3$s)", getArtifactType().getModel(), getArtifactType() //$NON-NLS-1$
                .getType(), getMimeType());
    }

    /**
     * Sets the extended type.
     * @param extendedType
     */
    public void setExtendedType(String extendedType) {
        if (extendedType != null && !isValid(extendedType)) {
            throw new RuntimeException(Messages.i18n.format("ArtifactType.InvalidExtendedType", extendedType)); //$NON-NLS-1$
        }
        this.extendedType = extendedType;
    }

    /**
     * Gets the extended artifact type.
     */
    public String getExtendedType() {
        return extendedType;
    }

    private static class ModelMime {
        public ModelMime(String extendedModel, String mimeType) {
            this.extendedModel = extendedModel;
            this.mimeType = mimeType;
        }
        public String extendedModel;
        public String mimeType;
    }

    /**
     * What kind of artifact is inside that wrapper?
     * @param artifactWrapper
     */
    public static ArtifactType valueOf(Artifact artifactWrapper, String hint) {
        ArtifactType type = null;
        // We were given a hint - try using that first.
        if (hint != null) {
            String methodName = "get" + hint; //$NON-NLS-1$
            try {
                Method method = artifactWrapper.getClass().getMethod(methodName);
                Object o = method.invoke(artifactWrapper);
                if (o != null && BaseArtifactType.class.isAssignableFrom(o.getClass())) {
                    Class<? extends BaseArtifactType> artyClass = ((BaseArtifactType) o).getClass();
                    return valueOf(artyClass);
                }
            } catch (Exception e) {
                // eat it
            }
        }

        // Didn't find it based on the hint - try them all!
        if (type == null) {
            Method[] methods = artifactWrapper.getClass().getMethods();
            try {
                for (Method method : methods) {
                    if (method.getName().startsWith("get")) { //$NON-NLS-1$
                        Object o = method.invoke(artifactWrapper);
                        if (o != null && BaseArtifactType.class.isAssignableFrom(o.getClass())) {
                            Class<? extends BaseArtifactType> artyClass = ((BaseArtifactType) o).getClass();
                            return valueOf(artyClass);
                        }
                    }
                }
            } catch (Exception e) {
                // eat it
            }
        }
        return type;
    }

    /**
     * Figures out the artifact type from the class.
     * @param artyClass
     */
    private static ArtifactType valueOf(Class<? extends BaseArtifactType> artyClass) {
        ArtifactType rval = null;
        for (ArtifactTypeEnum e : ArtifactTypeEnum.values()) {
            if (e.getTypeClass().equals(artyClass)) {
                rval = new ArtifactType(e, null);
            }
        }
        return rval;
    }

    /**
     * @return the extendedDerivedType
     */
    protected boolean isExtendedDerivedType() {
        return extendedDerivedType;
    }

    /**
     * @param extendedDerivedType the extendedDerivedType to set
     */
    public void setExtendedDerivedType(boolean extendedDerivedType) {
        this.extendedDerivedType = extendedDerivedType;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactType == null) ? 0 : artifactType.hashCode());
        result = prime * result + ((extendedType == null) ? 0 : extendedType.hashCode());
        return result;
    }
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ArtifactType other = (ArtifactType) obj;
        if (artifactType != other.artifactType)
            return false;
        if (extendedType == null) {
            if (other.extendedType != null)
                return false;
        } else if (!extendedType.equals(other.extendedType))
            return false;
        return true;
    }
}
