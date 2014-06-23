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
package org.overlord.sramp.integration.java.deriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.derived.ArtifactDeriver;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.integration.java.model.JavaModel;

/**
 * This deriver operates on a Maven pom.
 *
 * @author kurt.stam@redhat.com
 */
public class MavenPomDeriver implements ArtifactDeriver {

    /**
     * Constructor.
     */
    public MavenPomDeriver() {
    }

    /**
     * @see org.overlord.sramp.common.derived.ArtifactDeriver#derive(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.io.InputStream)
     */
    @Override
    public Collection<BaseArtifactType> derive(BaseArtifactType artifact, InputStream contentStream)
            throws IOException {
        List<BaseArtifactType> derivedArtifacts = new ArrayList<BaseArtifactType>();
        try {
	        Model model = new MavenXpp3Reader().read(contentStream);
	        MavenProject project = new MavenProject(model);
	        ((ExtendedDocument) artifact).setExtendedType(JavaModel.TYPE_MAVEN_POM_XML);
	        for (String key :project.getProperties().stringPropertyNames()) {
	        	String value = project.getProperties().getProperty(key);
	        	SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_PROPERTY + key, value);
	        }
	        //set core properties when not specified on the request
	        if (artifact.getDescription()==null) artifact.setDescription(project.getDescription());
	        if (artifact.getName()==null) artifact.setName(project.getName());
	        if (artifact.getVersion()==null) artifact.setVersion(project.getVersion());
	        //set the GAV and packaging info
	        SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_ARTIFACT_ID, model.getArtifactId());
	        SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_GROUP_ID, model.getGroupId());
	        SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_VERSION, model.getVersion());
	        SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_PACKAGING, model.getPackaging());
	        //set the parent GAV info
	        if (model.getParent()!=null) {
		        SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_PARENT_ARTIFACT_ID, model.getParent().getArtifactId());
		        SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_PARENT_GROUP_ID, model.getParent().getGroupId());
		        SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_MAVEN_PARENT_VERSION, model.getParent().getVersion());
	        }
		} catch (XmlPullParserException e) {
			throw new IOException(e.getMessage(),e);
		}
        return derivedArtifacts;
    }

    /**
     * @see org.overlord.sramp.common.derived.ArtifactDeriver#link(org.overlord.sramp.common.derived.LinkerContext, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.util.Collection)
     */
    @Override
    public void link(LinkerContext context, BaseArtifactType sourceArtifact,
            Collection<BaseArtifactType> derivedArtifacts) {
    }

}
