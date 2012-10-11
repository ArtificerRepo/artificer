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
package org.overlord.sramp;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;

/**
 * An enum representing all of the Artifact Types defined by S-RAMP.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactType {

    public static final ArtifactType Document = new ArtifactType(ArtifactTypeEnum.Document, "application/octet-stream");
    public static final ArtifactType XmlDocument = new ArtifactType(ArtifactTypeEnum.XmlDocument, "application/xml");
    public static final ArtifactType XsdDocument = new ArtifactType(ArtifactTypeEnum.XsdDocument, "application/xml");
    public static final ArtifactType WsdlDocument = new ArtifactType(ArtifactTypeEnum.WsdlDocument, "application/xml");
    public static final ArtifactType PolicyDocument = new ArtifactType(ArtifactTypeEnum.PolicyDocument, "application/xml");

	private ArtifactTypeEnum artifactType;
	private String mimeType;
	/** for a UserDefined Type, the type should be stored here */
	private String userType;
	private static Map<String, ModelMime> userDefinedArtifactTypes;

	/**
	 * Constructor.
	 * @param artifactType
	 * @param mimeType
	 */
	private ArtifactType(ArtifactTypeEnum artifactType, String mimeType) {
	    init();
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
	
	private void init() {
	    if (userDefinedArtifactTypes==null) {
    	    userDefinedArtifactTypes = new ConcurrentHashMap<String, ModelMime>();
    	    //TODO use SRAMP documents to store SRAMP internal information? We would put this one in here as
    	    // an hard coded UserDefinedArtifactType (UserDefined by us)
    	    userDefinedArtifactTypes.put("sramp",      new ModelMime("SRAMPDocument",   "application/xml"));
    	    //TODO read this from the repo instead (store as an Artifact, can we do TextDocument?)
    	    userDefinedArtifactTypes.put("pkg",        new ModelMime("BrmsPkgDocument", "application/octet-stream"));
    	    userDefinedArtifactTypes.put("package",    new ModelMime("BrmsPkgDocument", "application/octet-stream"));
    	    userDefinedArtifactTypes.put("bpmn",       new ModelMime("BpmnDocument",    "application/xml"));
    	    userDefinedArtifactTypes.put("bpmn2",      new ModelMime("BpmnDocument",    "application/xml"));
    	    userDefinedArtifactTypes.put("txt",        new ModelMime("TextDocument",    "text/plain"));
    	    userDefinedArtifactTypes.put("properties", new ModelMime("TextDocument",    "text/plain"));
    	    userDefinedArtifactTypes.put("css",        new ModelMime("CssDocument",     "text/css"));
    	    userDefinedArtifactTypes.put("html",       new ModelMime("HtmlDocument",    "text/html"));
    	    userDefinedArtifactTypes.put("ftl",        new ModelMime("FtlDocument",     "text/html"));
    	    userDefinedArtifactTypes.put("wid",        new ModelMime("TextDocument",    "text/plain"));
    	    userDefinedArtifactTypes.put("gif",        new ModelMime("ImageDocument",   "application/octet-stream"));
    	    userDefinedArtifactTypes.put("png",        new ModelMime("ImageDocument",   "application/octet-stream"));
	    }
	}

	/**
	 * Called to unwrap the S-RAMP artifact from its wrapper.
	 * @param artifactWrapper the S-RAMP artifact wrapper
	 * @return the specific artifact based on type
	 */
	public BaseArtifactType unwrap(Artifact artifactWrapper) {
		try {
			Method method = Artifact.class.getMethod("get" + getArtifactType().getType());
			return (BaseArtifactType) method.invoke(artifactWrapper);
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
		} else if (userDefinedArtifactTypes.containsKey(ext)){
		    ModelMime modelMime = userDefinedArtifactTypes.get(ext);
		    ArtifactType artifactType = new ArtifactType(ArtifactTypeEnum.UserDefinedArtifactType, modelMime.mimeType);
		    artifactType.setUserType(modelMime.userDefinedModel);
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
			ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.UserDefinedArtifactType;
			ArtifactType rval = new ArtifactType(artifactTypeEnum, null);
			rval.setUserType(artifactType);
			return rval;
		}
	}

	/**
     * Figures out the artifact type (enum) from the given S-RAMP artifact type string.
     * @param artifactType
     */
    public static ArtifactType valueOf(String model, String type) {
        ArtifactType artifactType = null;
        if ("user".equals(model)) {
            ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.UserDefinedArtifactType;
            artifactType = new ArtifactType(artifactTypeEnum, null);
            artifactType.setUserType(type);
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
		    if (artifactType.getArtifactType() == ArtifactTypeEnum.UserDefinedArtifactType) {
		        if ((artifact.getOtherAttributes().keySet().contains(new QName(SrampConstants.SRAMP_CONTENT_TYPE)))) {
		            String contentTypeStr = artifact.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_TYPE));
		            artifactType.setMimeType(contentTypeStr);
		        }
                String userType = ((UserDefinedArtifactType) artifact).getUserType();
                artifactType.setUserType(userType);
            }
			return artifactType;
		}
		ArtifactTypeEnum[] values = ArtifactTypeEnum.values();
		for (ArtifactTypeEnum artifactTypeEnum : values) {
			if (artifactTypeEnum.getTypeClass().equals(artifact.getClass())) {
			    ArtifactType artifactType = new ArtifactType(artifactTypeEnum, null);
			    if (artifactTypeEnum == ArtifactTypeEnum.UserDefinedArtifactType) {
			        String userType = ((UserDefinedArtifactType) artifact).getUserType();
                    artifactType.setUserType(userType);
                }
				return artifactType;
			}
		}
		throw new RuntimeException("Could not determine Artifact Type from artifact class: " + artifact.getClass());
	}
	/**
	 * Instantiates an S-RAMP artifact of the correct type given the ArtifactType, and populates the artifactType and 
	 * the contentType.
	 * @param artifactType
	 * @return
	 */
	public static BaseArtifactType getArtifactInstance(ArtifactType artifactType) {
	    ArtifactTypeEnum[] values = ArtifactTypeEnum.values();
        for (ArtifactTypeEnum artifactTypeEnum : values) {
            if (artifactTypeEnum.equals(artifactType.getArtifactType())) {
                try {
                    BaseArtifactType baseArtifactType = (BaseArtifactType) artifactTypeEnum.getTypeClass().newInstance();
                    baseArtifactType.setArtifactType(artifactTypeEnum.getApiType());
                    if (DocumentArtifactType.class.isAssignableFrom(baseArtifactType.getClass())) {
                        ((DocumentArtifactType) baseArtifactType).setContentType(artifactType.getMimeType());
                    }
                    if (artifactType.getArtifactType() == ArtifactTypeEnum.UserDefinedArtifactType) {
                        baseArtifactType.getOtherAttributes().put(new QName(SrampConstants.SRAMP_CONTENT_TYPE), artifactType.getMimeType());
                        ((UserDefinedArtifactType) baseArtifactType).setUserType(artifactType.getUserType());
                    }
                    return baseArtifactType;
                } catch (Exception e) {
                    throw new RuntimeException("Could not instantiate Artifact " + artifactTypeEnum.getTypeClass(),e);
                }
            }
        }
        throw new RuntimeException("Could not instantiate Artifact from artifactTpe class: " + artifactType);
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
	 * @param artifactType the artifactType to set
	 */
	public void setArtifactType(ArtifactTypeEnum artifactType) {
		this.artifactType = artifactType;
	}

	public String getModel() {
	    return getArtifactType().getModel();
	}

	public String getType() {
	    if (getArtifactType().equals(ArtifactTypeEnum.UserDefinedArtifactType)) {
            return getUserType();
        } else {
            return getArtifactType().getType();
        }
	}

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

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }
    
    private class ModelMime {
        
        public ModelMime(String userDefinedModel, String mimeType) {
            super();
            this.userDefinedModel = userDefinedModel;
            this.mimeType = mimeType;
        }
        public String userDefinedModel;
        public String mimeType;
        
    }

}
