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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.*;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Error;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Process;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.System;
import org.overlord.sramp.common.visitors.*;
import org.reflections.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * This visitor verifies numerous logical and spec-required constraints on artifact creations and updates.
 * 
 * @author Brett Meyer
 */
public class ArtifactVerifier extends HierarchicalArtifactVisitor {

    private static final Set<String> reservedNames = new HashSet<String>();
    private final ArtifactType artifactType;
    private BaseArtifactType oldArtifact = null;

    static {
        // TODO: This is not working reliably -- works well in EAP, but fails in Wildfly (different module system,
        // etc.).  For now, just hard-code the classes.
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setScanners(new SubTypesScanner(false))
//                .setUrls(ClasspathHelper.forClassLoader(ArtifactVerifier.class.getClassLoader()))
//                .filterInputsBy(new FilterBuilder().include(
//                        FilterBuilder.prefix("org.oasis_open.docs.s_ramp.ns.s_ramp_v1"))));
//        Set<Class<? extends Object>> classes = reflections.getSubTypesOf(Object.class);
        Class[] classes = new Class[] {
            Actor.class,
            ActorEnum.class,
            ActorTarget.class,
            Artifact.class,
            AttributeDeclaration.class,
            BaseArtifactEnum.class,
            BaseArtifactTarget.class,
            BaseArtifactType.class,
            Binding.class,
            BindingEnum.class,
            BindingOperation.class,
            BindingOperationEnum.class,
            BindingOperationFault.class,
            BindingOperationFaultEnum.class,
            BindingOperationFaultTarget.class,
            BindingOperationInput.class,
            BindingOperationInputEnum.class,
            BindingOperationInputTarget.class,
            BindingOperationOutput.class,
            BindingOperationOutputEnum.class,
            BindingOperationOutputTarget.class,
            BindingOperationTarget.class,
            BindingTarget.class,
            Choreography.class,
            ChoreographyProcess.class,
            ClassificationData.class,
            Collaboration.class,
            CollaborationProcess.class,
            ComplexTypeDeclaration.class,
            Composition.class,
            DerivedArtifactEnum.class,
            DerivedArtifactTarget.class,
            DerivedArtifactType.class,
            Document.class,
            DocumentArtifactEnum.class,
            DocumentArtifactTarget.class,
            DocumentArtifactType.class,
            Effect.class,
            EffectEnum.class,
            EffectTarget.class,
            Element.class,
            ElementDeclaration.class,
            ElementDeclarationEnum.class,
            ElementDeclarationTarget.class,
            ElementEnum.class,
            ElementTarget.class,
            Error.class,
            ErrorResponse.class,
            Event.class,
            EventEnum.class,
            EventTarget.class,
            ExtendedArtifactType.class,
            ExtendedDocument.class,
            ExtensionType.class,
            Fault.class,
            FaultEnum.class,
            FaultTarget.class,
            InformationType.class,
            InformationTypeEnum.class,
            InformationTypeTarget.class,
            Message.class,
            MessageEnum.class,
            MessageTarget.class,
            NamedWsdlDerivedArtifactType.class,
            ObjectFactory.class,
            Operation.class,
            OperationEnum.class,
            OperationInput.class,
            OperationInputEnum.class,
            OperationInputTarget.class,
            OperationOutput.class,
            OperationOutputEnum.class,
            OperationOutputTarget.class,
            OperationTarget.class,
            Orchestration.class,
            OrchestrationEnum.class,
            OrchestrationProcess.class,
            OrchestrationProcessEnum.class,
            OrchestrationProcessTarget.class,
            OrchestrationTarget.class,
            Organization.class,
            Part.class,
            PartEnum.class,
            PartTarget.class,
            Policy.class,
            PolicyAttachment.class,
            PolicyDocument.class,
            PolicyEnum.class,
            PolicyExpression.class,
            PolicyExpressionEnum.class,
            PolicyExpressionTarget.class,
            PolicySubject.class,
            PolicySubjectEnum.class,
            PolicySubjectTarget.class,
            PolicyTarget.class,
            Port.class,
            PortEnum.class,
            PortTarget.class,
            PortType.class,
            PortTypeEnum.class,
            PortTypeTarget.class,
            Process.class,
            Property.class,
            PropertyData.class,
            Relationship.class,
            RelationshipData.class,
            RelationshipTypeData.class,
            Service.class,
            ServiceComposition.class,
            ServiceContract.class,
            ServiceContractEnum.class,
            ServiceContractTarget.class,
            ServiceEndpoint.class,
            ServiceEnum.class,
            ServiceImplementationModelEnum.class,
            ServiceImplementationModelTarget.class,
            ServiceImplementationModelType.class,
            ServiceInstance.class,
            ServiceInstanceEnum.class,
            ServiceInstanceTarget.class,
            ServiceInterface.class,
            ServiceInterfaceEnum.class,
            ServiceInterfaceTarget.class,
            ServiceOperation.class,
            ServiceOperationEnum.class,
            ServiceOperationTarget.class,
            ServiceTarget.class,
            SimpleTypeDeclaration.class,
            SoaModelType.class,
            SoapAddress.class,
            SoapBinding.class,
            StoredQuery.class,
            StoredQueryData.class,
            System.class,
            Target.class,
            Task.class,
            TaskEnum.class,
            TaskTarget.class,
            WsdlDerivedArtifactType.class,
            WsdlDocument.class,
            WsdlDocumentEnum.class,
            WsdlDocumentTarget.class,
            WsdlExtension.class,
            WsdlExtensionEnum.class,
            WsdlExtensionTarget.class,
            WsdlService.class,
            XmlDocument.class,
            XsdDocument.class,
            XsdDocumentEnum.class,
            XsdDocumentTarget.class,
            XsdType.class,
            XsdTypeEnum.class,
            XsdTypeTarget.class
        };

        for (Class clazz : classes) {
            Set<Field> fields = ReflectionUtils.getAllFields(clazz);
            for (Field field : fields) {
                reservedNames.add(field.getName().toLowerCase());
            }
        }
    }

    public ArtifactVerifier(ArtifactType artifactType) {
        this.artifactType = artifactType;
    }

    public ArtifactVerifier(BaseArtifactType oldArtifact, ArtifactType artifactType) {
        this(artifactType);
        this.oldArtifact = oldArtifact;
    }

    @Override
    protected void visitBase(BaseArtifactType artifact) {
        super.visitBase(artifact);
        verifyModel(artifact);
        verifyNames(artifact);
    }

    @Override
    public void visit(XsdDocument artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("importedXsds", artifact.getImportedXsds());
            verifyEmptyDerivedRelationships("includedXsds", artifact.getIncludedXsds());
            verifyEmptyDerivedRelationships("redefinedXsds", artifact.getRedefinedXsds());
        } else {
            XsdDocument castOld = (XsdDocument) oldArtifact;
            verifyUnchangedDerivedRelationships("importedXsds", artifact.getImportedXsds(), castOld.getImportedXsds());
            verifyUnchangedDerivedRelationships("includedXsds", artifact.getIncludedXsds(), castOld.getIncludedXsds());
            verifyUnchangedDerivedRelationships("redefinedXsds", artifact.getRedefinedXsds(), castOld.getRedefinedXsds());
        }
    }

    @Override
    public void visit(WsdlDocument artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("importedXsds", artifact.getImportedXsds());
            verifyEmptyDerivedRelationships("includedXsds", artifact.getIncludedXsds());
            verifyEmptyDerivedRelationships("redefinedXsds", artifact.getRedefinedXsds());
            verifyEmptyDerivedRelationships("importedWsdls", artifact.getImportedWsdls());
        } else {
            WsdlDocument castOld = (WsdlDocument) oldArtifact;
            verifyUnchangedDerivedRelationships("importedXsds", artifact.getImportedXsds(), castOld.getImportedXsds());
            verifyUnchangedDerivedRelationships("includedXsds", artifact.getIncludedXsds(), castOld.getIncludedXsds());
            verifyUnchangedDerivedRelationships("redefinedXsds", artifact.getRedefinedXsds(), castOld.getRedefinedXsds());
            verifyUnchangedDerivedRelationships("importedWsdls", artifact.getImportedWsdls(), castOld.getImportedWsdls());
        }
    }

    @Override
    public void visit(Message artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("part", artifact.getPart());
        } else {
            Message castOld = (Message) oldArtifact;
            verifyUnchangedDerivedRelationships("part", artifact.getPart(), castOld.getPart());
        }
    }

    @Override
    public void visit(Part artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("element", artifact.getElement());
            verifyEmptyDerivedRelationships("type", artifact.getType());
        } else {
            Part castOld = (Part) oldArtifact;
            verifyUnchangedDerivedRelationships("element", artifact.getElement(), castOld.getElement());
            verifyUnchangedDerivedRelationships("type", artifact.getType(), castOld.getType());
        }
    }

    @Override
    public void visit(PortType artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("operation", artifact.getOperation());
        } else {
            PortType castOld = (PortType) oldArtifact;
            verifyUnchangedDerivedRelationships("operation", artifact.getOperation(), castOld.getOperation());
        }
    }

    @Override
    public void visit(Operation artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("input", artifact.getInput());
            verifyEmptyDerivedRelationships("output", artifact.getOutput());
            verifyEmptyDerivedRelationships("fault", artifact.getFault());
        } else {
            Operation castOld = (Operation) oldArtifact;
            verifyUnchangedDerivedRelationships("input", artifact.getInput(), castOld.getInput());
            verifyUnchangedDerivedRelationships("output", artifact.getOutput(), castOld.getOutput());
            verifyUnchangedDerivedRelationships("fault", artifact.getFault(), castOld.getFault());
        }
    }

    @Override
    public void visit(OperationInput artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("message", artifact.getMessage());
        } else {
            OperationInput castOld = (OperationInput) oldArtifact;
            verifyUnchangedDerivedRelationships("message", artifact.getMessage(), castOld.getMessage());
        }
    }

    @Override
    public void visit(OperationOutput artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("message", artifact.getMessage());
        } else {
            OperationOutput castOld = (OperationOutput) oldArtifact;
            verifyUnchangedDerivedRelationships("message", artifact.getMessage(), castOld.getMessage());
        }
    }

    @Override
    public void visit(Fault artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("message", artifact.getMessage());
        } else {
            Fault castOld = (Fault) oldArtifact;
            verifyUnchangedDerivedRelationships("message", artifact.getMessage(), castOld.getMessage());
        }
    }

    @Override
    public void visit(Binding artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("bindingOperation", artifact.getBindingOperation());
            verifyEmptyDerivedRelationships("portType", artifact.getPortType());
        } else {
            Binding castOld = (Binding) oldArtifact;
            verifyUnchangedDerivedRelationships("bindingOperation", artifact.getBindingOperation(), castOld.getBindingOperation());
            verifyUnchangedDerivedRelationships("portType", artifact.getPortType(), castOld.getPortType());
        }
    }

    @Override
    public void visit(BindingOperation artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("input", artifact.getInput());
            verifyEmptyDerivedRelationships("output", artifact.getOutput());
            verifyEmptyDerivedRelationships("fault", artifact.getFault());
            verifyEmptyDerivedRelationships("operation", artifact.getOperation());
        } else {
            BindingOperation castOld = (BindingOperation) oldArtifact;
            verifyUnchangedDerivedRelationships("input", artifact.getInput(), castOld.getInput());
            verifyUnchangedDerivedRelationships("output", artifact.getOutput(), castOld.getOutput());
            verifyUnchangedDerivedRelationships("fault", artifact.getFault(), castOld.getFault());
            verifyUnchangedDerivedRelationships("operation", artifact.getOperation(), castOld.getOperation());
        }
    }

    @Override
    public void visit(WsdlService artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("port", artifact.getPort());
        } else {
            WsdlService castOld = (WsdlService) oldArtifact;
            verifyUnchangedDerivedRelationships("port", artifact.getPort(), castOld.getPort());
        }
    }

    @Override
    public void visit(Port artifact) {
        super.visit(artifact);
        if (oldArtifact == null) {
            verifyEmptyDerivedRelationships("binding", artifact.getBinding());
        } else {
            Port castOld = (Port) oldArtifact;
            verifyUnchangedDerivedRelationships("binding", artifact.getBinding(), castOld.getBinding());
        }
    }

    private void verifyModel(BaseArtifactType artifact) {
        if (! artifactType.getArtifactType().getApiType().equals(artifact.getArtifactType())) {
            error = new WrongModelException(artifactType.getArtifactType().getApiType().value(),
                    artifact.getArtifactType().value());
        }
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

    private void verifyEmptyDerivedRelationships(String relationshipType, Object relationship) {
        if (relationship != null) {
            error = new DerivedRelationshipCreationException(relationshipType);
        }
    }

    private void verifyUnchangedDerivedRelationships(String relationshipType, Collection<?> relationships,
            Collection<?> oldRelationships) {
        // TODO: We'll eventually be introducing artifact deep comparisons.  But until then, just keep this simple...
        if (relationships.size() != oldRelationships.size()) {
            error = new DerivedRelationshipCreationException(relationshipType);
        }
    }

    private void verifyUnchangedDerivedRelationships(String relationshipType, Object relationship, Object oldRelationship) {
        // TODO: We'll eventually be introducing artifact deep comparisons.  But until then, just keep this simple...
        if ((oldRelationship != null && relationship == null) || (oldRelationship == null && relationship != null)) {
            error = new DerivedRelationshipCreationException(relationshipType);
        }
    }
}
