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

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.derived.ArtifactDeriver;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.integration.java.model.JavaModel;

/**
 * This deriver operates on the switchyard.xml file found in a typical SwitchYard
 * application JAR/WAR.
 *
 * @author eric.wittmann@redhat.com
 */
public class JavaClassDeriver implements ArtifactDeriver {

    /**
     * Constructor.
     */
    public JavaClassDeriver() {
    }

    /**
     * @see org.overlord.sramp.common.derived.ArtifactDeriver#derive(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.io.InputStream)
     */
    @Override
    public Collection<BaseArtifactType> derive(BaseArtifactType artifact, InputStream contentStream)
            throws IOException {
        List<BaseArtifactType> derivedArtifacts = new ArrayList<BaseArtifactType>();
        ClassParser parser = new ClassParser(contentStream, artifact.getName());
        JavaClass javaClass = parser.parse();
        if (javaClass.isInterface()) {
            ((ExtendedDocument) artifact).setExtendedType(JavaModel.TYPE_JAVA_INTERFACE);
        } else if (javaClass.isClass()) {
            ((ExtendedDocument) artifact).setExtendedType(JavaModel.TYPE_JAVA_CLASS);
        } else if (javaClass.isEnum()) {
            ((ExtendedDocument) artifact).setExtendedType(JavaModel.TYPE_JAVA_ENUM);
        }
        String packageName = javaClass.getPackageName();
        String className = javaClass.getClassName();
        artifact.setName(className);
        String shortName = className;
        if (className.lastIndexOf('.') > 0) {
            shortName = className.substring(className.lastIndexOf('.') + 1);
        }
        SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_PACKAGE_NAME, packageName);
        SrampModelUtils.setCustomProperty(artifact, JavaModel.PROP_CLASS_NAME, shortName);
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
