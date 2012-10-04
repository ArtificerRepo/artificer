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

import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
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
		} else {
			return new ArtifactType(ArtifactTypeEnum.Document, null);
		}
	}

	/**
	 * Figures out the artifact type (enum) from the given S-RAMP artifact type string.
	 * @param artifactType
	 */
	public static ArtifactType valueOf(String artifactType) {
		ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.valueOf(artifactType);
		return new ArtifactType(artifactTypeEnum, null);
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
		    if (artifactType.getArtifactType().equals(ArtifactTypeEnum.UserDefinedArtifactType)) {
                String userType = ((UserDefinedArtifactType) artifact).getUserType();
                artifactType.setUserType(userType);
            }
			return artifactType;
		}
		ArtifactTypeEnum[] values = ArtifactTypeEnum.values();
		for (ArtifactTypeEnum artifactTypeEnum : values) {
			if (artifactTypeEnum.getTypeClass().equals(artifact.getClass())) {
			    ArtifactType artifactType = new ArtifactType(artifactTypeEnum, null);
			    if (artifactTypeEnum.equals(ArtifactTypeEnum.UserDefinedArtifactType)) {
			        String userType = ((UserDefinedArtifactType) artifact).getUserType();
                    artifactType.setUserType(userType);
                }
				return artifactType;
			}
		}
		throw new RuntimeException("Could not determine Artifact Type from artifact class: " + artifact.getClass());
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

}
