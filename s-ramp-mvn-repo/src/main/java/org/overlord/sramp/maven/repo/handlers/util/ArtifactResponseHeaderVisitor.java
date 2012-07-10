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
package org.overlord.sramp.maven.repo.handlers.util;

import javax.servlet.http.HttpServletResponse;

import org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;

/**
 * An artifact visitor that 
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactResponseHeaderVisitor extends HierarchicalArtifactVisitorAdapter {

	private HttpServletResponse response;
	
	/**
	 * Constructor.
	 * @param response
	 */
	public ArtifactResponseHeaderVisitor(HttpServletResponse response) {
		this.response = response;
	}
	
	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitBase(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
	}
	
	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitXmlDocument(org.s_ramp.xmlns._2010.s_ramp.XmlDocument)
	 */
	@Override
	protected void visitXmlDocument(XmlDocument artifact) {
		String ct = artifact.getContentType();
		String ce = artifact.getContentEncoding();
		Long cs = artifact.getContentSize();
		
		if (ct == null || ct.trim().length() == 0)
			ct = "text/xml";
		if (ce != null && ce.trim().length() > 0)
			ct += ";" + ce;
		
		this.response.setContentType(ct);
		if (cs != null && cs.intValue() > 0)
			this.response.setContentLength(cs.intValue());
	}

}
