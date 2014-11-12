/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.common;

import java.lang.reflect.Field;
import java.util.*;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.*;
import org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * This visitor verifies numerous logical and spec-required constraints on artifact creations and updates.
 * 
 * @author Brett Meyer
 */
public class ArtifactVerifier extends HierarchicalArtifactVisitorAdapter {
    
    private static Set<String> reservedNames = new HashSet<String>();
    static {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false))
                .setUrls(ClasspathHelper.forClassLoader(BaseArtifactType.class.getClassLoader()))
                .filterInputsBy(new FilterBuilder().include(
                        FilterBuilder.prefix("org.oasis_open.docs.s_ramp.ns.s_ramp_v1"))));
        Set<Class<? extends Object>> classes = reflections.getSubTypesOf(Object.class);
        
        for (Class<? extends Object> clazz : classes) {
            Set<Field> fields = ReflectionUtils.getAllFields(clazz);
            for (Field field : fields) {
                reservedNames.add(field.getName().toLowerCase());
            }
        }
    }

    @Override
    protected void visitBase(BaseArtifactType artifact) {
        super.visitBase(artifact);
        verifyNames(artifact);
    }

    @Override
    public void visit(XsdDocument artifact) {
        super.visit(artifact);
        verifyEmptyDerivedRelationships("importedXsds", artifact.getImportedXsds());
        verifyEmptyDerivedRelationships("includedXsds", artifact.getIncludedXsds());
        verifyEmptyDerivedRelationships("redefinedXsds", artifact.getRedefinedXsds());
    }

    @Override
    public void visit(WsdlDocument artifact) {
        super.visit(artifact);
        verifyEmptyDerivedRelationships("importedXsds", artifact.getImportedXsds());
        verifyEmptyDerivedRelationships("includedXsds", artifact.getIncludedXsds());
        verifyEmptyDerivedRelationships("redefinedXsds", artifact.getRedefinedXsds());
        verifyEmptyDerivedRelationships("importedWsdls", artifact.getImportedWsdls());
    }

    /**
     * The S-RAMP spec states that a custom property or generic relationship name cannot duplicate *any* built-in
     * property/relationship name from *any* S-RAMP type.  To conform to that requirement, we'll automate the process
     * by building a list of all field names in the API.  This will define our "reserved keyword" list, even if it is
     * somewhat more restrictive than the spec requires.
     *
     * The spec also requires that, within an artifact, a custom property name cannot duplicate a generic relationship
     * name (and vice versa).
     *
     * @param artifact
     */
    private void verifyNames(BaseArtifactType artifact) {
        // First, build a list of all the names within this artifact.
        List<String> propertyNames = new ArrayList<String>();
        List<String> relationshipNames = new ArrayList<String>();
        for (Property property : artifact.getProperty()) {
            propertyNames.add(property.getPropertyName());
        }
        for (Relationship relationship : artifact.getRelationship()) {
            relationshipNames.add(relationship.getRelationshipType());
        }
        
        // Then, compare against both reserved and local names.
        for (String propertyName : propertyNames) {
            if (isReserved(propertyName)) {
                error = new ReservedNameException(propertyName);
            }
            if (relationshipNames.contains(propertyName)) {
                error = new DuplicateNameException(propertyName);
            }
            if (Collections.frequency(propertyNames, propertyName) > 1) {
                error = new DuplicateNameException(propertyName);
            }
        }
        for (String relationshipName : relationshipNames) {
            if (isReserved(relationshipName)) {
                error = new ReservedNameException(relationshipName);
            }
            if (propertyNames.contains(relationshipName)) {
                error = new DuplicateNameException(relationshipName);
            }
        }
    }
    
    private boolean isReserved(String s) {
        return reservedNames.contains(s.toLowerCase());
    }

    private void verifyEmptyDerivedRelationships(String relationshipType, Collection<?> relationships) {
        if (!relationships.isEmpty()) {
            error = new DerivedRelationshipCreationException(relationshipType);
        }
    }
}
