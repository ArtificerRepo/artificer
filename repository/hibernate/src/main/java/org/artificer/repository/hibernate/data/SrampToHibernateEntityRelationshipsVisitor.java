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
package org.artificer.repository.hibernate.data;

import org.apache.commons.lang.StringUtils;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.common.visitors.HierarchicalArtifactVisitor;
import org.artificer.repository.hibernate.HibernateRelationshipFactory;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerRelationship;
import org.artificer.repository.hibernate.entity.ArtificerRelationshipType;
import org.artificer.repository.hibernate.entity.ArtificerTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ActorTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.EffectTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.EventTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.InformationTypeTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicySubjectTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContractTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterfaceTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoaModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.TaskTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Brett Meyer.
 */
public class SrampToHibernateEntityRelationshipsVisitor extends HierarchicalArtifactVisitor {

    private final ArtificerArtifact artificerArtifact;
    private final HibernateRelationshipFactory relationshipFactory;

    public static void visit(BaseArtifactType srampArtifact, ArtificerArtifact artificerArtifact,
            HibernateRelationshipFactory relationshipFactory) throws Exception {
        SrampToHibernateEntityRelationshipsVisitor visitor = new SrampToHibernateEntityRelationshipsVisitor(
                artificerArtifact, relationshipFactory);
        ArtifactVisitorHelper.visitArtifact(visitor, srampArtifact);

        visitor.throwError();
    }

    public SrampToHibernateEntityRelationshipsVisitor(ArtificerArtifact artificerArtifact,
            HibernateRelationshipFactory relationshipFactory) {
        this.artificerArtifact = artificerArtifact;
        this.relationshipFactory = relationshipFactory;

        // To make this simple, first bulk-delete all relationships under this artifact.
        artificerArtifact.getRelationships().clear();
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
     */
    @Override
    protected void visitBase(BaseArtifactType artifact) {
        super.visitBase(artifact);
        try {
            updateGenericRelationships(artifact);
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
     */
    @Override
    protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
        super.visitWsdlDerived(artifact);
        try {
            setRelationships("extension", ArtificerRelationshipType.DERIVED, artifact.getExtension());
        } catch (Exception e) {
            error = e;
        }
    }

    @Override
    protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
        super.visitServiceImplementation(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (DocumentArtifactTarget documentation : artifact.getDocumentation()) {
                targetTypes.add(documentation.getArtifactType().toString());
            }
            setRelationships("documentation", ArtificerRelationshipType.MODELED, artifact.getDocumentation());
        } catch (Exception e) {
            error = e;
        }
    }

    @Override
    protected void visitSoa(SoaModelType artifact) {
        super.visitSoa(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (DocumentArtifactTarget documentation : artifact.getDocumentation()) {
                targetTypes.add(documentation.getArtifactType().toString());
            }
            setRelationships("documentation", ArtificerRelationshipType.MODELED, artifact.getDocumentation());
        } catch (Exception e) {
            error = e;
        }
    }

    @Override
    protected void visitElement(Element artifact) {
        super.visitElement(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (ElementTarget target : artifact.getRepresents()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("represents", ArtificerRelationshipType.MODELED, artifact.getRepresents());

            targetTypes = new ArrayList<String>();
            for (ElementTarget target : artifact.getUses()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("uses", ArtificerRelationshipType.MODELED, artifact.getUses());

            targetTypes = new ArrayList<String>();
            for (ServiceTarget target : artifact.getPerforms()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("performs", ArtificerRelationshipType.MODELED, artifact.getPerforms());

            if (artifact.getDirectsOrchestration() != null) {
                setRelationship("directsOrchestration", ArtificerRelationshipType.MODELED,
                        artifact.getDirectsOrchestration());
            }

            if (artifact.getDirectsOrchestrationProcess() != null) {
                setRelationship("directsOrchestrationProcess", ArtificerRelationshipType.MODELED,
                        artifact.getDirectsOrchestrationProcess());
            }

            targetTypes = new ArrayList<String>();
            for (EventTarget target : artifact.getGenerates()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("generates", ArtificerRelationshipType.MODELED, artifact.getGenerates());

            targetTypes = new ArrayList<String>();
            for (EventTarget target : artifact.getRespondsTo()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("respondsTo", ArtificerRelationshipType.MODELED, artifact.getRespondsTo());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void visitActor(Actor artifact) {
        super.visitActor(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (TaskTarget target : artifact.getDoes()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("does", ArtificerRelationshipType.MODELED, artifact.getDoes());

            targetTypes = new ArrayList<String>();
            for (PolicyTarget target : artifact.getSetsPolicy()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("setsPolicy", ArtificerRelationshipType.MODELED, artifact.getSetsPolicy());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the generic artifact relationships.
     * @param artifact
     * @throws Exception
     */
    private void updateGenericRelationships(BaseArtifactType artifact) throws Exception {
        for (Relationship relationship : artifact.getRelationship()) {
            if (relationship.getRelationshipTarget().size() > 0) {
                setRelationships(relationship.getRelationshipType(), ArtificerRelationshipType.GENERIC,
                        relationship.getRelationshipTarget(), relationship.getOtherAttributes());
            }
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
     */
    @Override
    public void visit(XsdDocument artifact) {
        super.visit(artifact);
        try {
            setRelationships("importedXsds", ArtificerRelationshipType.MODELED, artifact.getImportedXsds());
            setRelationships("includedXsds", ArtificerRelationshipType.MODELED, artifact.getIncludedXsds());
            setRelationships("redefinedXsds", ArtificerRelationshipType.MODELED, artifact.getRedefinedXsds());
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
     */
    @Override
    public void visit(WsdlDocument artifact) {
        super.visit(artifact);
        try {
            setRelationships("importedXsds", ArtificerRelationshipType.MODELED, artifact.getImportedXsds());
            setRelationships("includedXsds", ArtificerRelationshipType.MODELED, artifact.getIncludedXsds());
            setRelationships("redefinedXsds", ArtificerRelationshipType.MODELED, artifact.getRedefinedXsds());
            setRelationships("importedWsdls", ArtificerRelationshipType.MODELED, artifact.getImportedWsdls());
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * Message has references to all its {@link org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part}s.
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
     */
    @Override
    public void visit(Message artifact) {
        super.visit(artifact);
        try {
            setRelationships("part", ArtificerRelationshipType.DERIVED, artifact.getPart());
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
     */
    @Override
    public void visit(Part artifact) {
        super.visit(artifact);
        try {
            if (artifact.getElement() != null) {
                setRelationship("element", ArtificerRelationshipType.DERIVED, artifact.getElement());
            } else if (artifact.getType() != null) {
                setRelationship("type", ArtificerRelationshipType.DERIVED, artifact.getType());
            }
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
     */
    @Override
    public void visit(PortType artifact) {
        super.visit(artifact);
        try {
            setRelationships("operation", ArtificerRelationshipType.DERIVED, artifact.getOperation());
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
     */
    @Override
    public void visit(Operation artifact) {
        super.visit(artifact);
        try {
            setRelationship("input", ArtificerRelationshipType.DERIVED, artifact.getInput());
            setRelationship("output", ArtificerRelationshipType.DERIVED, artifact.getOutput());
            setRelationships("fault", ArtificerRelationshipType.DERIVED, artifact.getFault());
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
     */
    @Override
    public void visit(OperationInput artifact) {
        super.visit(artifact);
        try {
            setRelationship("message", ArtificerRelationshipType.DERIVED, artifact.getMessage());
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
     */
    @Override
    public void visit(OperationOutput artifact) {
        super.visit(artifact);
        try {
            setRelationship("message", ArtificerRelationshipType.DERIVED, artifact.getMessage());
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
     */
    @Override
    public void visit(Fault artifact) {
        super.visit(artifact);
        try {
            setRelationship("message", ArtificerRelationshipType.DERIVED, artifact.getMessage());
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
     */
    @Override
    public void visit(Binding artifact) {
        super.visit(artifact);
        try {
            setRelationships("bindingOperation", ArtificerRelationshipType.DERIVED, artifact.getBindingOperation());
            setRelationship("portType", ArtificerRelationshipType.DERIVED, artifact.getPortType());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
     */
    @Override
    public void visit(BindingOperation artifact) {
        super.visit(artifact);
        try {
            setRelationship("input", ArtificerRelationshipType.DERIVED, artifact.getInput());
            setRelationship("output", ArtificerRelationshipType.DERIVED, artifact.getOutput());
            setRelationships("fault", ArtificerRelationshipType.DERIVED, artifact.getFault());
            setRelationship("operation", ArtificerRelationshipType.DERIVED, artifact.getOperation());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService)
     */
    @Override
    public void visit(WsdlService artifact) {
        super.visit(artifact);
        try {
            setRelationships("port", ArtificerRelationshipType.DERIVED, artifact.getPort());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
     */
    @Override
    public void visit(Port artifact) {
        super.visit(artifact);
        try {
            setRelationship("binding", ArtificerRelationshipType.DERIVED, artifact.getBinding());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceEndpoint artifact) {
        super.visit(artifact);
        try {
            if (artifact.getEndpointDefinedBy() != null) {
                setRelationship("endpointDefinedBy", ArtificerRelationshipType.MODELED, artifact.getEndpointDefinedBy());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceInstance artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (BaseArtifactTarget target : artifact.getUses()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("uses", ArtificerRelationshipType.MODELED, artifact.getUses());

            targetTypes = new ArrayList<String>();
            for (BaseArtifactTarget target : artifact.getDescribedBy()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("describedBy", ArtificerRelationshipType.MODELED, artifact.getDescribedBy());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceOperation artifact) {
        super.visit(artifact);
        try {
            if (artifact.getOperationDefinedBy() != null) {
                setRelationship("operationDefinedBy", ArtificerRelationshipType.MODELED, artifact.getOperationDefinedBy());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Policy artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (PolicySubjectTarget target : artifact.getAppliesTo()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("appliesTo", ArtificerRelationshipType.MODELED, artifact.getAppliesTo());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceInterface artifact) {
        super.visit(artifact);
        try {
            if (artifact.getInterfaceDefinedBy() != null) {
                setRelationship("interfaceDefinedBy", ArtificerRelationshipType.MODELED, artifact.getInterfaceDefinedBy());
            }

            if (artifact.getHasOperation() != null) {
                setRelationship("hasOperation", ArtificerRelationshipType.MODELED, artifact.getHasOperation());
            }

            List<String> targetTypes = new ArrayList<String>();
            for (InformationTypeTarget target : artifact.getHasOutput()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("hasOutput", ArtificerRelationshipType.MODELED, artifact.getHasOutput());

            targetTypes = new ArrayList<String>();
            for (InformationTypeTarget target : artifact.getHasInput()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("hasInput", ArtificerRelationshipType.MODELED, artifact.getHasInput());

            targetTypes = new ArrayList<String>();
            for (ServiceTarget target : artifact.getIsInterfaceOf()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("isInterfaceOf", ArtificerRelationshipType.MODELED, artifact.getIsInterfaceOf());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceContract artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (ActorTarget target : artifact.getInvolvesParty()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("involvesParty", ArtificerRelationshipType.MODELED, artifact.getInvolvesParty());

            targetTypes = new ArrayList<String>();
            for (EffectTarget target : artifact.getSpecifies()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("specifies", ArtificerRelationshipType.MODELED, artifact.getSpecifies());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Organization artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (ServiceImplementationModelTarget target : artifact.getProvides()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("provides", ArtificerRelationshipType.MODELED, artifact.getProvides());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Service artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (ServiceContractTarget target : artifact.getHasContract()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("hasContract", ArtificerRelationshipType.MODELED, artifact.getHasContract());

            targetTypes = new ArrayList<String>();
            for (ServiceInterfaceTarget target : artifact.getHasInterface()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("hasInterface", ArtificerRelationshipType.MODELED, artifact.getHasInterface());

            if (artifact.getHasInstance() != null) {
                setRelationship("hasInstance", ArtificerRelationshipType.MODELED, artifact.getHasInstance());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // generic, derived
    private void setRelationship(String relationshipName, ArtificerRelationshipType relationshipType,
            Target target) throws Exception {
        if (target != null && StringUtils.isNotBlank(target.getValue())) {
            ArtificerRelationship artificerRelationship = createRelationship(relationshipName, relationshipType,
                    Collections.EMPTY_MAP);

            createTarget(artificerRelationship, target);

            artificerArtifact.getRelationships().add(artificerRelationship);
        }
    }

    private void setRelationships(String relationshipName, ArtificerRelationshipType relationshipType,
            List<? extends Target> targets, Map<QName, String> relationshipOtherAttributes) throws Exception {
        if (targets.size() > 0) {
            ArtificerRelationship artificerRelationship = createRelationship(relationshipName, relationshipType,
                    relationshipOtherAttributes);

            for (Target target : targets) {
                if (StringUtils.isNotBlank(target.getValue())) {
                    createTarget(artificerRelationship, target);
                }
            }

            if (!artificerRelationship.getTargets().isEmpty()) {
                artificerArtifact.getRelationships().add(artificerRelationship);
            }
        }
    }

    private void setRelationships(String relationshipName, ArtificerRelationshipType relationshipType,
            List<? extends Target> targets) throws Exception {
        setRelationships(relationshipName, relationshipType, targets, Collections.EMPTY_MAP);
    }

    private ArtificerRelationship createRelationship(String relationshipName,
            ArtificerRelationshipType relationshipType, Map<QName, String> relationshipOtherAttributes)
            throws Exception {
        ArtificerRelationship artificerRelationship = new ArtificerRelationship();
        artificerRelationship.setName(relationshipName);
        artificerRelationship.setType(relationshipType);
        artificerRelationship.setOwner(artificerArtifact);
        for (QName key : relationshipOtherAttributes.keySet()) {
            String value = relationshipOtherAttributes.get(key);
            artificerRelationship.getOtherAttributes().put(key.toString(), value);
        }
        return artificerRelationship;
    }

    private void createTarget(ArtificerRelationship artificerRelationship, Target target) throws Exception {
        ArtificerTarget artificerTarget = new ArtificerTarget();
        artificerTarget.setTarget(relationshipFactory.createRelationship(target.getValue()));

        // Use reflection to get the 'artifact type' enum attribute found on
        // most (all?) targets.  Unfortunately, the method and field are
        // redefined in each subclass of Target.
        // Get ^^^ changed in the spec!
        try {
            Method m = target.getClass().getMethod("getArtifactType");
            Object o = m.invoke(target); // the enum itself
            m = o.getClass().getMethod("name");
            String targetType = (String) m.invoke(o);
            artificerTarget.setTargetType(targetType);
        } catch (Exception e) {
            // eat it
        }

        artificerTarget.setRelationship(artificerRelationship);
        for (QName key : target.getOtherAttributes().keySet()) {
            String value = target.getOtherAttributes().get(key);
            artificerTarget.getOtherAttributes().put(key.toString(), value);
        }
        artificerRelationship.getTargets().add(artificerTarget);
    }
}
