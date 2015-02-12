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
package org.artificer.demos.artifactbuilder;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.artificer.integration.artifactbuilder.IndexedArtifactCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link List} implementation that also indexes the web.xml related artifacts
 * added to it.  This index can be used for fast lookup of various types of
 * derived web.xml content.
 *
 * @author eric.wittmann@redhat.com
 */
public class WebXmlArtifactCollection extends IndexedArtifactCollection {

	private static final long serialVersionUID = WebXmlArtifactCollection.class.hashCode();

	private Map<String, ExtendedArtifactType> filterIndex = new HashMap<String, ExtendedArtifactType>();
    private Map<String, ExtendedArtifactType> servletIndex = new HashMap<String, ExtendedArtifactType>();

	/**
	 * Constructor.
	 */
	public WebXmlArtifactCollection() {
	}

	/**
	 * @see java.util.LinkedList#add(java.lang.Object)
	 */
	@Override
	public boolean add(BaseArtifactType artifact) {
		indexArtifact(artifact);
		return super.add(artifact);
	}

	/**
	 * Adds the given artifact to the appropriate index.
	 * @param artifact
	 */
	private void indexArtifact(BaseArtifactType artifact) {
		if (artifact instanceof ExtendedArtifactType) {
		    ExtendedArtifactType uda = (ExtendedArtifactType) artifact;
            String userType = uda.getExtendedType();
		    if ("FilterDeclaration".equals(userType)) {
		        String key = artifact.getName();
		        filterIndex.put(key, uda);
		    } else if ("ServletDeclaration".equals(userType)) {
		        String key = artifact.getName();
		        servletIndex.put(key, uda);
		    }
		}
	}

    /**
     * Find a filter declaration by name.
     * @param name
     */
    public ExtendedArtifactType lookupFilter(String name) {
        return filterIndex.get(name);
    }

	/**
	 * Find a servlet declaration by name.
	 * @param name
	 */
	public ExtendedArtifactType lookupServlet(String name) {
		return servletIndex.get(name);
	}

}
