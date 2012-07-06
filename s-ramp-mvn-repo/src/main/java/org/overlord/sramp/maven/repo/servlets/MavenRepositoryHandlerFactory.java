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
package org.overlord.sramp.maven.repo.servlets;

import org.overlord.sramp.maven.repo.MavenRepositoryPath;
import org.overlord.sramp.maven.repo.handlers.ArtifactModelHandler;
import org.overlord.sramp.maven.repo.handlers.ArtifactTypeHandler;
import org.overlord.sramp.maven.repo.handlers.Error404Handler;
import org.overlord.sramp.maven.repo.handlers.RootHandler;

/**
 * This class maps an inbound request path to a handler capable of properly handling it.  The
 * inbound request path represents some part of the virtual Maven Repository.  Examples include:
 * 
 * <ul>
 *  <li>/</li>
 *  <li>/core/XmlDocument</li>
 *  <li>/xsd/XsdDocument/{uuid}/1.0.0/{uuid}.xsd</li>
 *  <li>/xsd/XsdDocument/{uuid}/1.0.0/{uuid}.pom</li>
 *  <li>/wsdl/WsdlDocument/{uuid}/2.3.7/{uuid}.wsdl</li>
 *  <li>/wsdl/WsdlDocument/{uuid}/2.3.7/{uuid}.pom</li>
 * </ul>
 * 
 * The following types of handlers are needed:
 * 
 * <ul>
 *  <li>Root handler (directory listing) - displays the S-RAMP Artifact Models</li>
 *  <li>Artifact Model handler (directory listing) - displays the Artifact Types for a given Artifact Model</li>
 *  <li>Artifact Type handler (directory listing) - displays all UUIDs for a given Artifact Type</li>
 *  <li>Artifact UUID handler (directory listing) - displays the list of versions for a given Artifact by UUID</li>
 *  <li>Artifact Version handler (directory listing) - displays a directory listing of the artifact and its POM</li>
 *  <li>Artifact handler (content) - returns the content/media of the artifact</li>
 *  <li>Artifact POM handler (content) - generates an returns a POM for the artifact</li>
 * </ul>
 * 
 * Maven dependencies on a given artifact in the S-RAMP repository can be specified as follows:
 * 
 * <pre>
 *   &lt;dependency&gt;
 *     &lt;groupId&gt;{ArtifactModel}.{ArtifactType}&lt;/groupId&gt;
 *     &lt;artifactId&gt;{ArtifactUUID}&lt;/artifactId&gt;
 *     &lt;version&gt;{ArtifactVersion}&lt;/version&gt;
 *     &lt;type&gt;{ArtifactType}&lt;/type&gt;
 *   &lt;/dependency&gt;
 * </pre>
 * 
 * An example of this might be:
 * 
 * <pre>
 *   &lt;dependency&gt;
 *     &lt;groupId&gt;xsd.XsdDocument&lt;/groupId&gt;
 *     &lt;artifactId&gt;3706cf40-c50f-11e1-9b21-0800200c9a66&lt;/artifactId&gt;
 *     &lt;version&gt;1.3&lt;/version&gt;
 *     &lt;type&gt;xsd&lt;/type&gt;
 *   &lt;/dependency&gt;
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public class MavenRepositoryHandlerFactory {

	private static final MavenRepositoryRequestHandler ROOT_HANDLER = new RootHandler();
	private static final MavenRepositoryRequestHandler ERROR_404_HANDLER = new Error404Handler();
	
	/**
	 * Default constructor.
	 */
	public MavenRepositoryHandlerFactory() {
	}

	/**
	 * Creates a handler for the given maven path.
	 * @param repositoryPath the maven repository path
	 * @return a maven request handler
	 */
	public MavenRepositoryRequestHandler createHandler(MavenRepositoryPath repositoryPath) {
		if (repositoryPath.getArtifactModel() == null) {
			return ROOT_HANDLER;
		} else if (repositoryPath.getArtifactType() == null) {
			return new ArtifactModelHandler(repositoryPath.getArtifactModel());
		} else if (repositoryPath.getArtifactUuid() == null) {
			return new ArtifactTypeHandler(repositoryPath);
		}
		return ERROR_404_HANDLER;
	}
	
}
