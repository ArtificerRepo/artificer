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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Choreography;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ChoreographyProcess;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Collaboration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.CollaborationProcess;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Composition;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Effect;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Event;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.InformationType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Orchestration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationProcess;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyAttachment;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyExpression;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicySubject;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Process;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceComposition;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoaModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.System;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Task;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtension;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdType;

/**
 * A base class for visitors that are interested in specific sections of the
 * S-RAMP class hierarchy.  This class provides methods for the various
 * shared (abstract) base classes within the hierachy.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class HierarchicalArtifactVisitor extends AbstractArtifactVisitor {

	protected Exception error;

	/**
	 * Default constructor.
	 */
	public HierarchicalArtifactVisitor() {
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
     * Common visit method for Extended artifacts.
     * @param artifact
     */
    protected void visitExtended(ExtendedArtifactType artifact) {
        // Subclasses can do common visit logic here
    }

    /**
     * Common visit method for Extended document artifacts.
     * @param artifact
     */
    protected void visitExtendedDocument(ExtendedDocument artifact) {
        // Subclasses can do common visit logic here
    }

    /**
     * Common visit method for Element artifacts.
     * @param artifact
     */
    protected void visitElement(Element artifact) {
        // Subclasses can do common visit logic here
    }

    /**
     * Common visit method for Actor artifacts.
     * @param artifact
     */
    protected void visitActor(Actor artifact) {
        visitElement(artifact);
    }

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document)
	 */
	@Override
	public void visit(Document artifact) {
		super.visit(artifact);
		visitDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument)
	 */
	@Override
	public void visit(XmlDocument artifact) {
		super.visit(artifact);
		visitDocument(artifact);
		visitXmlDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
		super.visit(artifact);
		visitDocument(artifact);
		visitXmlDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration)
	 */
	@Override
	public void visit(AttributeDeclaration artifact) {
		super.visit(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration)
	 */
	@Override
	public void visit(ElementDeclaration artifact) {
		super.visit(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration)
	 */
	@Override
	public void visit(SimpleTypeDeclaration artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitXsdDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration)
	 */
	@Override
	public void visit(ComplexTypeDeclaration artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitXsdDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyDocument)
	 */
	@Override
	public void visit(PolicyDocument artifact) {
		super.visit(artifact);
		visitDocument(artifact);
		visitXmlDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyExpression)
	 */
	@Override
	public void visit(PolicyExpression artifact) {
		super.visit(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyAttachment)
	 */
	@Override
	public void visit(PolicyAttachment artifact) {
		super.visit(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress)
	 */
	@Override
	public void visit(SoapAddress artifact) {
		super.visit(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding)
	 */
	@Override
	public void visit(SoapBinding artifact) {
		super.visit(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
	 */
	@Override
	public void visit(WsdlDocument artifact) {
		super.visit(artifact);
		visitDocument(artifact);
		visitXmlDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService)
	 */
	@Override
	public void visit(WsdlService artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
	 */
	@Override
	public void visit(Port artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtension)
	 */
	@Override
	public void visit(WsdlExtension artifact) {
		super.visit(artifact);
		visitDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
	 */
	@Override
	public void visit(Part artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
	 */
	@Override
	public void visit(Message artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
	 */
	@Override
	public void visit(Fault artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
	 */
	@Override
	public void visit(PortType artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
	 */
	@Override
	public void visit(Operation artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
	 */
	@Override
	public void visit(OperationInput artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
	 */
	@Override
	public void visit(OperationOutput artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
	 */
	@Override
	public void visit(Binding artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
	 */
	@Override
	public void visit(BindingOperation artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInput)
	 */
	@Override
	public void visit(BindingOperationInput artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutput)
	 */
	@Override
	public void visit(BindingOperationOutput artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFault)
	 */
	@Override
	public void visit(BindingOperationFault artifact) {
		super.visit(artifact);
		visitDerived(artifact);
		visitWsdlDerived(artifact);
		visitNamedWsdlDerived(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization)
	 */
	@Override
	public void visit(Organization artifact) {
		super.visit(artifact);
		visitSoa(artifact);
		visitActor(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint)
	 */
	@Override
	public void visit(ServiceEndpoint artifact) {
		super.visit(artifact);
		visitServiceImplementation(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance)
	 */
	@Override
	public void visit(ServiceInstance artifact) {
		super.visit(artifact);
		visitServiceImplementation(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation)
	 */
	@Override
	public void visit(ServiceOperation artifact) {
		super.visit(artifact);
		visitServiceImplementation(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
	 */
	@Override
	public void visit(ExtendedArtifactType artifact) {
		super.visit(artifact);
		visitExtended(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument)
	 */
	@Override
	public void visit(ExtendedDocument artifact) {
        super.visit(artifact);
        visitDocument(artifact);
        visitExtendedDocument(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor)
	 */
	@Override
	public void visit(Actor artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitActor(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Choreography)
	 */
	@Override
	public void visit(Choreography artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ChoreographyProcess)
	 */
	@Override
	public void visit(ChoreographyProcess artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Collaboration)
	 */
	@Override
	public void visit(Collaboration artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.CollaborationProcess)
	 */
	@Override
	public void visit(CollaborationProcess artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Composition)
	 */
	@Override
	public void visit(Composition artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Effect)
	 */
	@Override
	public void visit(Effect artifact) {
		super.visit(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element)
	 */
	@Override
	public void visit(Element artifact) {
		super.visit(artifact);
		visitSoa(artifact);
		visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Event)
	 */
	@Override
	public void visit(Event artifact) {
		super.visit(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.InformationType)
	 */
	@Override
	public void visit(InformationType artifact) {
		super.visit(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Orchestration)
	 */
	@Override
	public void visit(Orchestration artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationProcess)
	 */
	@Override
	public void visit(OrchestrationProcess artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy)
	 */
	@Override
	public void visit(Policy artifact) {
		super.visit(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicySubject)
	 */
	@Override
	public void visit(PolicySubject artifact) {
		super.visit(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Process)
	 */
	@Override
	public void visit(Process artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service)
	 */
	@Override
	public void visit(Service artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract)
	 */
	@Override
	public void visit(ServiceContract artifact) {
		super.visit(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceComposition)
	 */
	@Override
	public void visit(ServiceComposition artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface)
	 */
	@Override
	public void visit(ServiceInterface artifact) {
		super.visit(artifact);
		visitSoa(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.System)
	 */
	@Override
	public void visit(System artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Task)
	 */
	@Override
	public void visit(Task artifact) {
		super.visit(artifact);
		visitSoa(artifact);
        visitElement(artifact);
	}

	public void throwError() throws Exception {
		if (error != null) {
			throw error;
		}
	}

}
