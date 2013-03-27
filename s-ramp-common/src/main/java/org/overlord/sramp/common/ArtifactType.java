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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;

/**
 * A class representing all of the Artifact Types defined by S-RAMP.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactType {

    public static final ArtifactType Document() {
        return new ArtifactType(ArtifactTypeEnum.Document, "application/octet-stream");
    }
    public static final ArtifactType XmlDocument() {
        return new ArtifactType(ArtifactTypeEnum.XmlDocument, "application/xml");
    }
    public static final ArtifactType XsdDocument() {
        return new ArtifactType(ArtifactTypeEnum.XsdDocument, "application/xml");
    }
    public static final ArtifactType WsdlDocument() {
        return new ArtifactType(ArtifactTypeEnum.WsdlDocument, "application/xml");
    }
    public static final ArtifactType PolicyDocument() {
        return new ArtifactType(ArtifactTypeEnum.PolicyDocument, "application/xml");
    }
    public static final ArtifactType Extended(String extendedType, boolean derived) {
        ArtifactType at = new ArtifactType(ArtifactTypeEnum.ExtendedArtifactType, null);
        at.setExtendedType(extendedType);
        at.setExtendedDerivedType(derived);
        return at;
    }

	private ArtifactTypeEnum artifactType;
	private String mimeType;
	/** for a Extended Type, the type should be stored here */
	private String extendedType;
	private boolean extendedDerivedType;
	private static Map<String, ModelMime> extendedArtifactTypes;
	static {
	    extendedArtifactTypes = new ConcurrentHashMap<String, ModelMime>();
        //TODO use SRAMP documents to store SRAMP internal information? We would put this one in here as
        // an hard coded ExtendedArtifactType (Extended by us)
	    extendedArtifactTypes.put("sramp",      new ModelMime("SRAMPDocument",   "application/xml"));
        //TODO read this from the repo instead (store as an Artifact, can we do TextDocument?)
	    extendedArtifactTypes.put("pkg",        new ModelMime("BrmsPkgDocument", "application/octet-stream"));
	    extendedArtifactTypes.put("package",    new ModelMime("BrmsPkgDocument", "application/octet-stream"));
	    extendedArtifactTypes.put("bpmn",       new ModelMime("BpmnDocument",    "application/xml"));
	    extendedArtifactTypes.put("bpmn2",      new ModelMime("BpmnDocument",    "application/xml"));
	    extendedArtifactTypes.put("txt",        new ModelMime("TextDocument",    "text/plain"));
	    extendedArtifactTypes.put("properties", new ModelMime("TextDocument",    "text/plain"));
	    extendedArtifactTypes.put("css",        new ModelMime("CssDocument",     "text/css"));
	    extendedArtifactTypes.put("html",       new ModelMime("HtmlDocument",    "text/html"));
	    extendedArtifactTypes.put("ftl",        new ModelMime("FtlDocument",     "text/html"));
	    extendedArtifactTypes.put("wid",        new ModelMime("TextDocument",    "text/plain"));
	    extendedArtifactTypes.put("gif",        new ModelMime("ImageDocument",   "application/octet-stream"));
	    extendedArtifactTypes.put("png",        new ModelMime("ImageDocument",   "application/octet-stream"));
	}

	/**
	 * Constructor.
	 * @param artifactType
	 * @param mimeType
	 */
	private ArtifactType(ArtifactTypeEnum artifactType, String mimeType) {
		setArtifactType(artifactType);
		// Might need something more interesting than this in the future.
		if (mimeType == null) {
			if (artifactType == ArtifactTypeEnum.Document) {
				mimeType = "application/octet-stream";
			} else {
				mimeType = "application/xml";
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
			Method method = Artifact.class.getMethod("get" + getArtifactType().getType());
			BaseArtifactType artifact = (BaseArtifactType) method.invoke(artifactWrapper);
			artifact.setArtifactType(this.getArtifactType().getApiType());
			return artifact;
		} catch (Exception e) {
			throw new RuntimeException("Failed to unwrap artifact for type: " + getArtifactType().getType(), e);
		}
	}

	/**
	 * Returns an {@link ArtifactType} given a common file extension.
	 * @param extension a file extension
	 * @return an s-ramp artifact type
	 */
	public static ArtifactType fromFileExtension(String extension) {
		String ext = extension.toLowerCase();
		if (ext.equals("xml")) {
			return new ArtifactType(ArtifactTypeEnum.XmlDocument, "application/xml");
		} else if (ext.equals("xsd")) {
			return new ArtifactType(ArtifactTypeEnum.XsdDocument, "application/xml");
		} else if (ext.equals("wsdl")) {
			return new ArtifactType(ArtifactTypeEnum.WsdlDocument, "application/xml");
		} else if (ext.equals("wspolicy")) {
			return new ArtifactType(ArtifactTypeEnum.PolicyDocument, "application/xml");
		} else if (extendedArtifactTypes.containsKey(ext)){
		    ModelMime modelMime = extendedArtifactTypes.get(ext);
		    ArtifactType artifactType = ArtifactType.Extended(modelMime.extendedModel, false);
		    artifactType.setMimeType(modelMime.mimeType);
		    return artifactType;
		} else {
			return new ArtifactType(ArtifactTypeEnum.Document, null);
		}
	}

	/**
	 * Figures out the artifact type (enum) from the given S-RAMP artifact type string.
	 * @param artifactType
	 */
	public static ArtifactType valueOf(String artifactType) {
		if (ArtifactTypeEnum.hasEnum(artifactType)) {
			ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.valueOf(artifactType);
			return new ArtifactType(artifactTypeEnum, null);
		} else {
			ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.ExtendedArtifactType;
			ArtifactType rval = new ArtifactType(artifactTypeEnum, null);
			rval.setExtendedType(artifactType);
			rval.setMimeType("application/octet-stream");
			return rval;
		}
	}

	/**
     * Figures out the artifact type (enum) from the given S-RAMP artifact type string.
     * @param artifactType
     */
    public static ArtifactType valueOf(String model, String type) {
        ArtifactType artifactType = null;
        if ("ext".equals(model)) {
            ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.ExtendedArtifactType;
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
		if (apiType != null) {
		    ArtifactType artifactType = valueOf(apiType);
		    if (DocumentArtifactType.class.isAssignableFrom(artifact.getClass())) {
		        artifactType.setMimeType(((DocumentArtifactType)artifact).getContentType());
		    }
		    if (artifactType.getArtifactType() == ArtifactTypeEnum.ExtendedArtifactType) {
		        if ((artifact.getOtherAttributes().keySet().contains(SrampConstants.SRAMP_CONTENT_TYPE_QNAME))) {
		            String contentTypeStr = artifact.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME);
		            artifactType.setMimeType(contentTypeStr);
		        }
                String extendedType = ((ExtendedArtifactType) artifact).getExtendedType();
                String extendedDerived = artifact.getOtherAttributes().get(SrampConstants.SRAMP_DERIVED_QNAME);
                artifactType.setExtendedType(extendedType);
                artifactType.setExtendedDerivedType("true".equals(extendedDerived));
            }
			return artifactType;
		}
		ArtifactTypeEnum[] values = ArtifactTypeEnum.values();
		for (ArtifactTypeEnum artifactTypeEnum : values) {
			if (artifactTypeEnum.getTypeClass().equals(artifact.getClass())) {
			    ArtifactType artifactType = new ArtifactType(artifactTypeEnum, null);
			    if (artifactTypeEnum == ArtifactTypeEnum.ExtendedArtifactType) {
	                if ((artifact.getOtherAttributes().keySet().contains(SrampConstants.SRAMP_CONTENT_TYPE_QNAME))) {
	                    String contentTypeStr = artifact.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME);
	                    artifactType.setMimeType(contentTypeStr);
	                }
			        String extendedType = ((ExtendedArtifactType) artifact).getExtendedType();
	                String extendedDerived = artifact.getOtherAttributes().get(SrampConstants.SRAMP_DERIVED_QNAME);
                    artifactType.setExtendedType(extendedType);
                    artifactType.setExtendedDerivedType("true".equals(extendedDerived));
                }
				return artifactType;
			}
		}
		throw new RuntimeException("Could not determine Artifact Type from artifact class: " + artifact.getClass());
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
            return baseArtifactType;
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate Artifact " + getArtifactType().getTypeClass(),e);
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
		throw new RuntimeException("Could not determine Artifact Type from S-RAMP API type: " + apiType.value());
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
	    return getArtifactType() == ArtifactTypeEnum.ExtendedArtifactType;
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
	    if (getArtifactType().equals(ArtifactTypeEnum.ExtendedArtifactType)) {
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
		return String.format("/s-ramp/%1$s/%2$s (%3$s)", getArtifactType().getModel(), getArtifactType()
				.getType(), getMimeType());
	}

    public void setExtendedType(String extendedType) {
        this.extendedType = extendedType;
    }

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
	public static ArtifactType valueOf(Artifact artifactWrapper) {
		ArtifactType type = null;
		Method[] methods = artifactWrapper.getClass().getMethods();
		try {
			for (Method method : methods) {
				if (method.getName().startsWith("get")) {
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
