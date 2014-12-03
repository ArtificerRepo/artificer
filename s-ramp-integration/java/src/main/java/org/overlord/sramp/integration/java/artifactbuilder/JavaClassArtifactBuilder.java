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
package org.overlord.sramp.integration.java.artifactbuilder;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.artifactbuilder.AbstractArtifactBuilder;
import org.overlord.sramp.common.artifactbuilder.ArtifactBuilder;
import org.overlord.sramp.common.artifactbuilder.RelationshipContext;
import org.overlord.sramp.integration.java.model.JavaModel;

import java.io.IOException;

/**
 * This artifact builder operates on the switchyard.xml file found in a typical SwitchYard
 * application JAR/WAR.
 *
 * @author eric.wittmann@redhat.com
 */
public class JavaClassArtifactBuilder extends AbstractArtifactBuilder {

    @Override
    public ArtifactBuilder buildArtifacts(BaseArtifactType primaryArtifact, ArtifactContent artifactContent)
            throws IOException {
        super.buildArtifacts(primaryArtifact, artifactContent);
        
        ClassParser parser = new ClassParser(getContentStream(), primaryArtifact.getName());
        JavaClass javaClass = parser.parse();
        if (javaClass.isInterface()) {
            ((ExtendedDocument) primaryArtifact).setExtendedType(JavaModel.TYPE_JAVA_INTERFACE);
        } else if (javaClass.isClass()) {
            ((ExtendedDocument) primaryArtifact).setExtendedType(JavaModel.TYPE_JAVA_CLASS);
        } else if (javaClass.isEnum()) {
            ((ExtendedDocument) primaryArtifact).setExtendedType(JavaModel.TYPE_JAVA_ENUM);
        }
        String packageName = javaClass.getPackageName();
        String className = javaClass.getClassName();
        primaryArtifact.setName(className);
        String shortName = className;
        if (className.lastIndexOf('.') > 0) {
            shortName = className.substring(className.lastIndexOf('.') + 1);
        }
        SrampModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_PACKAGE_NAME, packageName);
        SrampModelUtils.setCustomProperty(primaryArtifact, JavaModel.PROP_CLASS_NAME, shortName);
        
        return this;
    }

    @Override
    public ArtifactBuilder buildRelationships(RelationshipContext context) throws IOException {
        return this;
    }

}
