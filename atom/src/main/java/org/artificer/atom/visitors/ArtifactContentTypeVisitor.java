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
package org.artificer.atom.visitors;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.artificer.atom.MediaType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.visitors.HierarchicalArtifactVisitor;

/**
 * A simple artifact visitor that determines the content type of an S-RAMP
 * artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactContentTypeVisitor extends HierarchicalArtifactVisitor {

	private javax.ws.rs.core.MediaType contentType;

	/**
	 * Default constructor.
	 */
	public ArtifactContentTypeVisitor() {
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
		setContentType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType)
	 */
	@Override
	protected void visitDocument(DocumentArtifactType artifact) {
		if (artifact.getContentType() != null)
			setContentType(MediaType.valueOf(artifact.getContentType()));
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitXmlDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument)
	 */
	@Override
	protected void visitXmlDocument(XmlDocument artifact) {
		setContentType(MediaType.APPLICATION_XML_TYPE);
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitExtendedDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument)
	 */
	@Override
	protected void visitExtendedDocument(ExtendedDocument artifact) {
	    //grab the content type from an any-attribute
        setContentType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
	    if ((artifact.getOtherAttributes().keySet().contains(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME))) {
	        String contentTypeStr = artifact.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME);
	        if (contentTypeStr != null) {
	            setContentType(MediaType.valueOf(contentTypeStr));
	        }
	    }
	}

	/**
	 * @return the contentType
	 */
	public javax.ws.rs.core.MediaType getContentType() {
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(javax.ws.rs.core.MediaType contentType) {
		this.contentType = contentType;
	}

}
