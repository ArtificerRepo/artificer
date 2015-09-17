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
package org.artificer.integration.java.artifactbuilder;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.integration.artifactbuilder.AbstractArtifactBuilder;
import org.artificer.integration.artifactbuilder.ArtifactBuilder;
import org.artificer.integration.artifactbuilder.RelationshipContext;
import org.artificer.integration.java.model.JavaModel;

import java.io.IOException;

/**
 * This artifact builder operates on a Maven pom.
 *
 * @author kurt.stam@redhat.com
 */
public class MavenPomArtifactBuilder extends AbstractArtifactBuilder {

    @Override
    public ArtifactBuilder buildArtifacts(BaseArtifactType primaryArtifact, ArtifactContent artifactContent)
            throws Exception {
        super.buildArtifacts(primaryArtifact, artifactContent);
        
        try {
	        Model model = new MavenXpp3Reader().read(getContentStream());
	        MavenProject project = new MavenProject(model);
	        ((ExtendedDocument) primaryArtifact).setExtendedType(JavaModel.TYPE_MAVEN_POM_XML);
	        for (String key :project.getProperties().stringPropertyNames()) {
	        	String value = project.getProperties().getProperty(key);
	        	ArtificerModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_MAVEN_PROPERTY + key, value);
	        }
	        //set core properties when not specified on the request
	        if (primaryArtifact.getDescription()==null) primaryArtifact.setDescription(project.getDescription());
	        if (primaryArtifact.getName()==null) primaryArtifact.setName(project.getName());
	        if (primaryArtifact.getVersion()==null) primaryArtifact.setVersion(project.getVersion());
	        //set the GAV and packaging info
	        ArtificerModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_MAVEN_ARTIFACT_ID, model.getArtifactId());
	        ArtificerModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_MAVEN_GROUP_ID, model.getGroupId());
	        ArtificerModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_MAVEN_VERSION, model.getVersion());
	        ArtificerModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_MAVEN_PACKAGING, model.getPackaging());
	        //set the parent GAV info
	        if (model.getParent()!=null) {
		        ArtificerModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_MAVEN_PARENT_ARTIFACT_ID, model.getParent().getArtifactId());
		        ArtificerModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_MAVEN_PARENT_GROUP_ID, model.getParent().getGroupId());
		        ArtificerModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_MAVEN_PARENT_VERSION, model.getParent().getVersion());
	        }
	        
	        return this;
		} catch (XmlPullParserException e) {
			throw new IOException(e.getMessage(),e);
		}
    }

    @Override
    public ArtifactBuilder buildRelationships(RelationshipContext context) throws IOException {
        return this;
    }

}
