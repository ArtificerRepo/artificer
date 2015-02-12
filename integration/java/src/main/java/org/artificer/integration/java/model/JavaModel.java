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
package org.artificer.integration.java.model;


/**
 * Information about the java model defined by the java artifact builder(s).
 *
 * @author eric.wittmann@redhat.com
 */
public class JavaModel {

    public static final String TYPE_ARCHIVE = "JavaArchive"; //$NON-NLS-1$
    public static final String TYPE_WEB_APPLICATION = "JavaWebApplication"; //$NON-NLS-1$
    public static final String TYPE_ENTERPRISE_APPLICATION = "JavaEnterpriseApplication"; //$NON-NLS-1$
    public static final String TYPE_BEANS_XML = "BeanArchiveDescriptor"; //$NON-NLS-1$

    public static final String TYPE_JAVA_CLASS = "JavaClass"; //$NON-NLS-1$
    public static final String TYPE_JAVA_INTERFACE = "JavaInterface"; //$NON-NLS-1$
    public static final String TYPE_JAVA_ENUM = "JavaEnum"; //$NON-NLS-1$

    public static final String PROP_PACKAGE_NAME = "packageName"; //$NON-NLS-1$
    public static final String PROP_CLASS_NAME = "className"; //$NON-NLS-1$

    //maven info
    public static final String TYPE_MAVEN_POM_XML = "MavenPom"; //$NON-NLS-1$
    public static final String PROP_MAVEN_PROPERTY = "maven.property."; //$NON-NLS-1$
    public static final String PROP_MAVEN_ARTIFACT_ID = "maven.artifactId"; //$NON-NLS-1$
    public static final String PROP_MAVEN_GROUP_ID = "maven.groupId"; //$NON-NLS-1$
    public static final String PROP_MAVEN_VERSION = "maven.version"; //$NON-NLS-1$
    public static final String PROP_MAVEN_TYPE = "maven.type"; //$NON-NLS-1$
    public static final String PROP_MAVEN_CLASSIFIER = "maven.classifier"; //$NON-NLS-1$
    public static final String PROP_MAVEN_PACKAGING = "maven.packaging"; //$NON-NLS-1$
    public static final String PROP_MAVEN_PARENT_ARTIFACT_ID = "maven.parent.artifactId"; //$NON-NLS-1$
    public static final String PROP_MAVEN_PARENT_GROUP_ID = "maven.parent.groupId"; //$NON-NLS-1$
    public static final String PROP_MAVEN_PARENT_VERSION = "maven.parent.version"; //$NON-NLS-1$
    public static final String PROP_MAVEN_HASH_MD5 = "maven.hash.md5"; //$NON-NLS-1$
    public static final String PROP_MAVEN_HASH_SHA1 = "maven.hash.sha1"; //$NON-NLS-1$
    public static final String PROP_MAVEN_SNAPSHOT_ID = "maven.snapshot.id"; //$NON-NLS-1$

}
