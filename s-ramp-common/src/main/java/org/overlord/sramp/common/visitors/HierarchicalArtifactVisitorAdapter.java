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
package org.overlord.sramp.common.visitors;

import org.s_ramp.xmlns._2010.s_ramp.Actor;
import org.s_ramp.xmlns._2010.s_ramp.AttributeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Binding;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperation;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperationFault;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperationInput;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperationOutput;
import org.s_ramp.xmlns._2010.s_ramp.Choreography;
import org.s_ramp.xmlns._2010.s_ramp.ChoreographyProcess;
import org.s_ramp.xmlns._2010.s_ramp.Collaboration;
import org.s_ramp.xmlns._2010.s_ramp.CollaborationProcess;
import org.s_ramp.xmlns._2010.s_ramp.ComplexTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.Composition;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Document;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Effect;
import org.s_ramp.xmlns._2010.s_ramp.Element;
import org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.Event;
import org.s_ramp.xmlns._2010.s_ramp.Fault;
import org.s_ramp.xmlns._2010.s_ramp.InformationType;
import org.s_ramp.xmlns._2010.s_ramp.Message;
import org.s_ramp.xmlns._2010.s_ramp.NamedWsdlDerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Operation;
import org.s_ramp.xmlns._2010.s_ramp.OperationInput;
import org.s_ramp.xmlns._2010.s_ramp.OperationOutput;
import org.s_ramp.xmlns._2010.s_ramp.Orchestration;
import org.s_ramp.xmlns._2010.s_ramp.OrchestrationProcess;
import org.s_ramp.xmlns._2010.s_ramp.Organization;
import org.s_ramp.xmlns._2010.s_ramp.Part;
import org.s_ramp.xmlns._2010.s_ramp.Policy;
import org.s_ramp.xmlns._2010.s_ramp.PolicyAttachment;
import org.s_ramp.xmlns._2010.s_ramp.PolicyDocument;
import org.s_ramp.xmlns._2010.s_ramp.PolicyExpression;
import org.s_ramp.xmlns._2010.s_ramp.PolicySubject;
import org.s_ramp.xmlns._2010.s_ramp.Port;
import org.s_ramp.xmlns._2010.s_ramp.PortType;
import org.s_ramp.xmlns._2010.s_ramp.Process;
import org.s_ramp.xmlns._2010.s_ramp.Service;
import org.s_ramp.xmlns._2010.s_ramp.ServiceComposition;
import org.s_ramp.xmlns._2010.s_ramp.ServiceContract;
import org.s_ramp.xmlns._2010.s_ramp.ServiceEndpoint;
import org.s_ramp.xmlns._2010.s_ramp.ServiceImplementationModelType;
import org.s_ramp.xmlns._2010.s_ramp.ServiceInstance;
import org.s_ramp.xmlns._2010.s_ramp.ServiceInterface;
import org.s_ramp.xmlns._2010.s_ramp.ServiceOperation;
import org.s_ramp.xmlns._2010.s_ramp.SimpleTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.SoaModelType;
import org.s_ramp.xmlns._2010.s_ramp.SoapAddress;
import org.s_ramp.xmlns._2010.s_ramp.SoapBinding;
import org.s_ramp.xmlns._2010.s_ramp.System;
import org.s_ramp.xmlns._2010.s_ramp.Task;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDocument;
import org.s_ramp.xmlns._2010.s_ramp.WsdlExtension;
import org.s_ramp.xmlns._2010.s_ramp.WsdlService;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdType;

/**
 * A base class for visitors that are interested in specific sections of the
 * S-RAMP class hierarchy.  This class provides methods for the various
 * shared (abstract) base classes within the hierachy.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class HierarchicalArtifactVisitorAdapter implements ArtifactVisitor {

	/**
	 * Default constructor.
	 */
	public HierarchicalArtifactVisitorAdapter() {
	}

	/**
	 * Common visit method for all artifacts.
	 * @param artifact
	 */
	protected void visitBase(BaseArtifactType artifact) {
		// Subclasses can do common visit logic here
	}

	/**
	 * Common visit method for derived artifacts.
	 * @param artifact
	 */
	protected void visitDerived(DerivedArtifactType artifact) {
		// Subclasses can do common visit logic here
	}

	/**
	 * Common visit method for WSDL derived artifacts.
	 * @param artifact
	 */
	protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
		// Subclasses can do common visit logic here
	}

	/**
	 * Common visit method for named WSDL derived artifacts.
	 * @param artifact
	 */
	protected void visitNamedWsdlDerived(NamedWsdlDerivedArtifactType artifact) {
		// Subclasses can do common visit logic here
	}

	/**
	 * Common visit method for XSD derived artifacts.
	 * @param artifact
	 */
	protected void visitXsdDerived(XsdType artifact) {
		// Subclasses can do common visit logic here
	}

	/**
	 * Common visit method for document artifacts.
	 * @param artifact
	 */
	protected void visitDocument(DocumentArtifactType artifact) {
		// Subclasses can do common visit logic here
	}

	/**
	 * Common visit method for XML document artifacts.
	 * @param artifact
	 */
	protected void visitXmlDocument(XmlDocument artifact) {
		// Subclasses can do common visit logic here
	}

	/**
	 * Common visit method for service implementation artifacts.
	 * @param artifact
	 */
	protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
		// Subclasses can do common visit logic here
	}

	/**
	 * Common visit method for SOA model artifacts.
	 * @param artifact
	 */
	protected void visitSoa(SoaModelType artifact) {
		// Subclasses can do common visit logic here
	}

	/**
     * Common visit method for UserDefined artifacts.
     * @param artifact
     */
    protected void visitUserDefined(UserDefinedArtifactType artifact) {
        // Subclasses can do common visit logic here
    }

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Document)
	 */
	@Override
	public void visit(Document artifact) {
		visitBase(artifact);
		visitDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.XmlDocument)
	 */
	@Override
	public void visit(XmlDocument artifact) {
		visitBase(artifact);
		visitDocument(artifact);
		visitXmlDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
		visitBase(artifact);
		visitDocument(artifact);
		visitXmlDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.AttributeDeclaration)
	 */
	@Override
	public void visit(AttributeDeclaration artifact) {
		visitBase(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration)
	 */
	@Override
	public void visit(ElementDeclaration artifact) {
		visitBase(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.SimpleTypeDeclaration)
	 */
	@Override
	public void visit(SimpleTypeDeclaration artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitXsdDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ComplexTypeDeclaration)
	 */
	@Override
	public void visit(ComplexTypeDeclaration artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitXsdDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PolicyDocument)
	 */
	@Override
	public void visit(PolicyDocument artifact) {
		visitBase(artifact);
		visitDocument(artifact);
		visitXmlDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PolicyExpression)
	 */
	@Override
	public void visit(PolicyExpression artifact) {
		visitBase(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PolicyAttachment)
	 */
	@Override
	public void visit(PolicyAttachment artifact) {
		visitBase(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.SoapAddress)
	 */
	@Override
	public void visit(SoapAddress artifact) {
		visitBase(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.SoapBinding)
	 */
	@Override
	public void visit(SoapBinding artifact) {
		visitBase(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.WsdlDocument)
	 */
	@Override
	public void visit(WsdlDocument artifact) {
		visitBase(artifact);
		visitDocument(artifact);
		visitXmlDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.WsdlService)
	 */
	@Override
	public void visit(WsdlService artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Port)
	 */
	@Override
	public void visit(Port artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.WsdlExtension)
	 */
	@Override
	public void visit(WsdlExtension artifact) {
		visitBase(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Part)
	 */
	@Override
	public void visit(Part artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Message)
	 */
	@Override
	public void visit(Message artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Fault)
	 */
	@Override
	public void visit(Fault artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PortType)
	 */
	@Override
	public void visit(PortType artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Operation)
	 */
	@Override
	public void visit(Operation artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.OperationInput)
	 */
	@Override
	public void visit(OperationInput artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.OperationOutput)
	 */
	@Override
	public void visit(OperationOutput artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Binding)
	 */
	@Override
	public void visit(Binding artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.BindingOperation)
	 */
	@Override
	public void visit(BindingOperation artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.BindingOperationInput)
	 */
	@Override
	public void visit(BindingOperationInput artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.BindingOperationOutput)
	 */
	@Override
	public void visit(BindingOperationOutput artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.BindingOperationFault)
	 */
	@Override
	public void visit(BindingOperationFault artifact) {
		visitBase(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Organization)
	 */
	@Override
	public void visit(Organization artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceEndpoint)
	 */
	@Override
	public void visit(ServiceEndpoint artifact) {
		visitBase(artifact);
		visitServiceImplementation(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceInstance)
	 */
	@Override
	public void visit(ServiceInstance artifact) {
		visitBase(artifact);
		visitServiceImplementation(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceOperation)
	 */
	@Override
	public void visit(ServiceOperation artifact) {
		visitBase(artifact);
		visitServiceImplementation(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType)
	 */
	@Override
	public void visit(UserDefinedArtifactType artifact) {
		visitBase(artifact);
		visitUserDefined(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Actor)
	 */
	@Override
	public void visit(Actor artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Choreography)
	 */
	@Override
	public void visit(Choreography artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ChoreographyProcess)
	 */
	@Override
	public void visit(ChoreographyProcess artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Collaboration)
	 */
	@Override
	public void visit(Collaboration artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.CollaborationProcess)
	 */
	@Override
	public void visit(CollaborationProcess artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Composition)
	 */
	@Override
	public void visit(Composition artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Effect)
	 */
	@Override
	public void visit(Effect artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Element)
	 */
	@Override
	public void visit(Element artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Event)
	 */
	@Override
	public void visit(Event artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.InformationType)
	 */
	@Override
	public void visit(InformationType artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Orchestration)
	 */
	@Override
	public void visit(Orchestration artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.OrchestrationProcess)
	 */
	@Override
	public void visit(OrchestrationProcess artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Policy)
	 */
	@Override
	public void visit(Policy artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PolicySubject)
	 */
	@Override
	public void visit(PolicySubject artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Process)
	 */
	@Override
	public void visit(Process artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Service)
	 */
	@Override
	public void visit(Service artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceContract)
	 */
	@Override
	public void visit(ServiceContract artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceComposition)
	 */
	@Override
	public void visit(ServiceComposition artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceInterface)
	 */
	@Override
	public void visit(ServiceInterface artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.System)
	 */
	@Override
	public void visit(System artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Task)
	 */
	@Override
	public void visit(Task artifact) {
		visitBase(artifact);
		visitSoa(artifact);
	}

}
