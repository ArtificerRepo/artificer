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
package org.overlord.sramp.repository.jcr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.overlord.sramp.visitors.ArtifactVisitorAdapter;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * An artifact visitor used to update a JCR node.  This class is responsible
 * for modifying a JCR node using information found in the supplied s-ramp
 * artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateJCRNodeFromArtifactVisitor extends ArtifactVisitorAdapter {

	private Node jcrNode;
	private Exception error;
	
	/**
	 * Constructor.
	 * @param jcrNode the JCR node this visitor will be updating
	 */
	public UpdateJCRNodeFromArtifactVisitor(Node jcrNode) {
		this.jcrNode = jcrNode;
	}
	
	/**
	 * Called to perform all common base artifact updates (e.g. classifiedBy and properties).
	 * @param artifact the artifact being visited
	 */
	protected void visitBaseArtifact(BaseArtifactType artifact) {
		try {
			updateArtifactMetaData(artifact);
			updateArtifactProperties(artifact);
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * Updates the basic s-ramp meta data.
	 * @param artifact
	 * @throws Exception
	 */
	private void updateArtifactMetaData(BaseArtifactType artifact) throws Exception {
		if (artifact.getName() != null)
			this.jcrNode.setProperty("sramp:name", artifact.getName());
		if (artifact.getDescription() != null)
			this.jcrNode.setProperty("sramp:description", artifact.getDescription());
		if (artifact.getVersion() != null)
			this.jcrNode.setProperty("version", artifact.getVersion());
	}

	/**
	 * Updates the custom s-ramp properties.
	 * @param artifact
	 * @throws Exception
	 */
	private void updateArtifactProperties(BaseArtifactType artifact) throws Exception {
		Map<String, String> artifactProps = getArtifactProperties(artifact);
		Set<String> nodeProps = getNodePropertyNames(jcrNode);

		Set<String> propsToRemove = nodeProps;
		propsToRemove.removeAll(artifactProps.keySet());

    	String srampPropsPrefix = JCRConstants.SRAMP_PROPERTIES + ":";

		// Remove all properties that have been earmarked for removal.
		for (String propToRemove : propsToRemove) {
			String qname = srampPropsPrefix + propToRemove;
			this.jcrNode.setProperty(qname, (Value) null);
		}

		// Set all new property values
		for (Entry<String, String> prop : artifactProps.entrySet()) {
			String name = prop.getKey();
			String qname = srampPropsPrefix + name;
			String val = prop.getValue();
			this.jcrNode.setProperty(qname, val);
		}
	}

	/**
	 * Gets all of the custom properties from the artifact and returns them as a map.
	 * @param artifact
	 */
	private static Map<String, String> getArtifactProperties(BaseArtifactType artifact) {
		Map<String, String> props = new HashMap<String, String>();
		for (org.s_ramp.xmlns._2010.s_ramp.Property property : artifact.getProperty())
			props.put(property.getPropertyName(), property.getPropertyValue());
		return props;
	}

	/**
	 * Gets all of the custom s-ramp property names currently stored on the given
	 * JCR node.
	 * @param jcrNode
	 * @throws RepositoryException 
	 */
	private static Set<String> getNodePropertyNames(Node jcrNode) throws RepositoryException {
    	String srampPropsPrefix = JCRConstants.SRAMP_PROPERTIES + ":";
    	int srampPropsPrefixLen = srampPropsPrefix.length();

    	Set<String> rval = new HashSet<String>();
		PropertyIterator properties = jcrNode.getProperties();
		while (properties.hasNext()) {
			Property prop = properties.nextProperty();
			String propName = prop.getName();
			if (propName.startsWith(srampPropsPrefix)) {
				propName = propName.substring(srampPropsPrefixLen);
				rval.add(propName);
			}
		}
		return rval;
	}

	/**
	 * Returns true if this visitor encountered an error during visitation.
	 */
	public boolean hasError() {
		return error != null;
	}
	
	/**
	 * Returns the error encountered during visitation.
	 */
	public Exception getError() {
		return error;
	}

	/**
	 * @see org.overlord.sramp.visitors.ArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
		visitBaseArtifact(artifact);
	}

}
