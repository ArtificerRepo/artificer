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
package org.artificer.repository.test;

import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ActorEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ActorTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Choreography;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ChoreographyProcess;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Collaboration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.CollaborationProcess;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Composition;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Effect;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.EffectEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.EffectTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Event;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.EventEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.EventTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.InformationType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Orchestration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationProcess;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationProcessEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationProcessTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicySubject;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceComposition;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContractEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContractTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstanceEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstanceTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterfaceEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterfaceTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperationEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoaModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Task;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.TaskEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.TaskTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.artificer.common.ArtifactTypeEnum;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Brett Meyer
 */
public class LogicalModelTest extends AbstractNoAuditingPersistenceTest {
    
    @Test
    public void testServiceImplementationModel() throws Exception {
        ServiceEndpoint persistedServiceEndpoint = (ServiceEndpoint) getArtifactByUUID(serviceEndpoint.getUuid());
        assertBasic(persistedServiceEndpoint, serviceEndpoint);
        assertDocumentation(persistedServiceEndpoint.getDocumentation());
        assertEquals(serviceEndpoint.getUrl(), persistedServiceEndpoint.getUrl());
        Port portByTarget = (Port) getArtifactByTarget(persistedServiceEndpoint.getEndpointDefinedBy());
        assertBasic(portByTarget, port);
        
        ServiceInstance persistedServiceInstance = (ServiceInstance) getArtifactByUUID(serviceInstance.getUuid());
        assertBasic(persistedServiceInstance, serviceInstance);
        assertDocumentation(persistedServiceInstance.getDocumentation());
        assertTargets(persistedServiceInstance.getUses(), 1, port);
        assertTargets(persistedServiceInstance.getDescribedBy(), 1, wsdl);
        
        ServiceOperation persistedServiceOperation = (ServiceOperation) getArtifactByUUID(serviceOperation.getUuid());
        assertBasic(persistedServiceOperation, serviceOperation);
        assertDocumentation(persistedServiceOperation.getDocumentation());
        Operation operationByTarget = (Operation) getArtifactByTarget(persistedServiceOperation.getOperationDefinedBy());
        assertBasic(operationByTarget, operation);
    }
    
    @Test
    public void testSOAModel() throws Exception {
        insertSoaArtifacts();
        
        Effect effect = new Effect();
        effect.setArtifactType(BaseArtifactEnum.EFFECT);
        persistAndAssert(effect);
        
        EffectTarget effectTarget = new EffectTarget();
        effectTarget.setArtifactType(EffectEnum.EFFECT);
        effectTarget.setValue(effect.getUuid());

        Event event = new Event();
        event.setArtifactType(BaseArtifactEnum.EVENT);
        persistAndAssert(event);

        InformationType informationType = new InformationType();
        informationType.setArtifactType(BaseArtifactEnum.INFORMATION_TYPE);
        persistAndAssert(informationType);

        Policy policy = new Policy();
        policy.setArtifactType(BaseArtifactEnum.POLICY);
        persistAndAssert(policy);
        // TODO: Test w/ a policy attachment, once the deriver actually works.

        PolicySubject policySubject = new PolicySubject();
        policySubject.setArtifactType(BaseArtifactEnum.POLICY_SUBJECT);
        persistAndAssert(policySubject);
        
        Element element = new Element();
        element.setArtifactType(BaseArtifactEnum.ELEMENT);
        persistAndAssertElement(element);
        
        Actor actor = new Actor();
        actor.setArtifactType(BaseArtifactEnum.ACTOR);
        persistAndAssertActor(actor);
        
        ActorTarget actorTarget = new ActorTarget();
        actorTarget.setArtifactType(ActorEnum.ACTOR);
        actorTarget.setValue(actor.getUuid());
        
        Organization organization = new Organization();
        organization.setArtifactType(BaseArtifactEnum.ORGANIZATION);
        ServiceImplementationModelTarget serviceImplementationModelTarget = new ServiceImplementationModelTarget();
        serviceImplementationModelTarget.setArtifactType(ServiceImplementationModelEnum.SERVICE_ENDPOINT);
        serviceImplementationModelTarget.setValue(serviceEndpoint.getUuid());
        organization.getProvides().add(serviceImplementationModelTarget);
        organization = (Organization) persistAndAssertActor(organization);
        assertTargets(organization.getProvides(), 1, serviceEndpoint);

        ServiceContract serviceContract = new ServiceContract();
        serviceContract.setArtifactType(BaseArtifactEnum.SERVICE_CONTRACT);
        serviceContract.getInvolvesParty().add(actorTarget);
        serviceContract.getSpecifies().add(effectTarget);
        serviceContract = persistAndAssert(serviceContract);
        assertTargets(serviceContract.getInvolvesParty(), 1, actor);
        assertTargets(serviceContract.getSpecifies(), 1, effect);
        
        ServiceContractTarget serviceContractTarget = new ServiceContractTarget();
        serviceContractTarget.setArtifactType(ServiceContractEnum.SERVICE_CONTRACT);
        serviceContractTarget.setValue(serviceContract.getUuid());

        ServiceInterface serviceInterface = new ServiceInterface();
        serviceInterface.setArtifactType(BaseArtifactEnum.SERVICE_INTERFACE);
        serviceInterface.setInterfaceDefinedBy(derivedPortTypeTarget);
        // TODO: Also can't test hasOutput, hasInput, and isInterfaceOf until the target enums are corrected
        ServiceOperationTarget serviceOperationTarget = new ServiceOperationTarget();
        serviceOperationTarget.setArtifactType(ServiceOperationEnum.SERVICE_OPERATION);
        serviceOperationTarget.setValue(serviceOperation.getUuid());
        serviceInterface.setHasOperation(serviceOperationTarget);
        serviceInterface = persistAndAssert(serviceInterface);
        assertTarget(serviceInterface.getHasOperation(), serviceOperation);
        // TODO: check #interfaceDefinedBy after the above is corrected
        
        ServiceInterfaceTarget serviceInterfaceTarget = new ServiceInterfaceTarget();
        serviceInterfaceTarget.setArtifactType(ServiceInterfaceEnum.SERVICE_INTERFACE);
        serviceInterfaceTarget.setValue(serviceInterface.getUuid());
        
        Service service = new Service();
        service.setArtifactType(BaseArtifactEnum.SERVICE);
        service.getHasContract().add(serviceContractTarget);
        service.getHasInterface().add(serviceInterfaceTarget);
        ServiceInstanceTarget serviceInstanceTarget = new ServiceInstanceTarget();
        serviceInstanceTarget.setArtifactType(ServiceInstanceEnum.SERVICE_INSTANCE);
        serviceInstanceTarget.setValue(serviceInstance.getUuid());
        service.setHasInstance(serviceInstanceTarget);
        service = (Service) persistAndAssertElement(service);
        assertTargets(service.getHasContract(), 1, serviceContract);
        assertTargets(service.getHasInterface(), 1, serviceInterface);
        assertTarget(service.getHasInstance(), serviceInstance);
        
        org.oasis_open.docs.s_ramp.ns.s_ramp_v1.System system = new org.oasis_open.docs.s_ramp.ns.s_ramp_v1.System();
        system.setArtifactType(BaseArtifactEnum.SYSTEM);
        persistAndAssertElement(system);
        
        Composition composition = new Composition();
        composition.setArtifactType(BaseArtifactEnum.COMPOSITION);
        persistAndAssertElement(composition);
        
        Choreography choreography = new Choreography();
        choreography.setArtifactType(BaseArtifactEnum.CHOREOGRAPHY);
        persistAndAssertElement(choreography);
        
        Collaboration collaboration = new Collaboration();
        collaboration.setArtifactType(BaseArtifactEnum.COLLABORATION);
        persistAndAssertElement(collaboration);
        
        Orchestration orchestration = new Orchestration();
        orchestration.setArtifactType(BaseArtifactEnum.ORCHESTRATION);
        persistAndAssertElement(orchestration);
        
        org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Process process = new org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Process();
        process.setArtifactType(BaseArtifactEnum.PROCESS);
        persistAndAssertElement(process);
        
        ChoreographyProcess choreographyProcess = new ChoreographyProcess();
        choreographyProcess.setArtifactType(BaseArtifactEnum.CHOREOGRAPHY_PROCESS);
        persistAndAssertElement(choreographyProcess);
        
        CollaborationProcess collaborationProcess = new CollaborationProcess();
        collaborationProcess.setArtifactType(BaseArtifactEnum.COLLABORATION_PROCESS);
        persistAndAssertElement(collaborationProcess);
        
        OrchestrationProcess orchestrationProcess = new OrchestrationProcess();
        orchestrationProcess.setArtifactType(BaseArtifactEnum.ORCHESTRATION_PROCESS);
        persistAndAssertElement(orchestrationProcess);
        
        ServiceComposition serviceComposition = new ServiceComposition();
        serviceComposition.setArtifactType(BaseArtifactEnum.SERVICE_COMPOSITION);
        persistAndAssertElement(serviceComposition);
        
        Task task = new Task();
        task.setArtifactType(BaseArtifactEnum.TASK);
        persistAndAssertElement(task);
    }
    
    private WsdlDocument wsdl;    
    private Operation operation;    
    private Port port;    
    private ServiceEndpoint serviceEndpoint;    
    private ServiceInstance serviceInstance;    
    private ServiceOperation serviceOperation;    
    private DerivedArtifactTarget derivedOperationTarget;    
    private DerivedArtifactTarget derivedPortTarget;    
    private DerivedArtifactTarget derivedPortTypeTarget;
    private DocumentArtifactTarget documentationTarget;
    @Before
    public void insertArtifacts() throws Exception {
        wsdl = (WsdlDocument) addArtifact("/sample-files/wsdl/", "jcr-sample.wsdl", new WsdlDocument(),
                BaseArtifactEnum.WSDL_DOCUMENT);
        PortType portType = (PortType) assertSingleArtifact(ArtifactTypeEnum.PortType, "SamplePortType");
        operation = (Operation) assertSingleArtifact(ArtifactTypeEnum.Operation, "findSimple");
        port = (Port) assertSingleArtifact(ArtifactTypeEnum.Port, "SamplePort");
        SoapAddress soapAddress = (SoapAddress) getArtifactByTarget(port.getExtension().get(0));

        derivedPortTypeTarget = new DerivedArtifactTarget();
        derivedPortTypeTarget.setArtifactType(DerivedArtifactEnum.PORT_TYPE);
        derivedPortTypeTarget.setValue(portType.getUuid());
        
        documentationTarget = new DocumentArtifactTarget();
        documentationTarget.setArtifactType(DocumentArtifactEnum.WSDL_DOCUMENT);
        documentationTarget.setValue(wsdl.getUuid());
        
        serviceEndpoint = new ServiceEndpoint();
        serviceEndpoint.setArtifactType(BaseArtifactEnum.SERVICE_ENDPOINT);
        serviceEndpoint.setName("SampleServiceEndpoint");
        serviceEndpoint.setUrl(soapAddress.getSoapLocation());
        serviceEndpoint.getDocumentation().add(documentationTarget);
        derivedPortTarget = new DerivedArtifactTarget();
        derivedPortTarget.setArtifactType(DerivedArtifactEnum.PORT);
        derivedPortTarget.setValue(port.getUuid());
        serviceEndpoint.setEndpointDefinedBy(derivedPortTarget);
        serviceEndpoint = (ServiceEndpoint) persistenceManager.persistArtifact(serviceEndpoint, null);
        
        serviceInstance = new ServiceInstance();
        serviceInstance.setArtifactType(BaseArtifactEnum.SERVICE_INSTANCE);
        serviceInstance.setName("SampleServiceInstance");
        serviceInstance.getDocumentation().add(documentationTarget);
        BaseArtifactTarget portTarget = new BaseArtifactTarget();
        portTarget.setArtifactType(BaseArtifactEnum.PORT);
        portTarget.setValue(port.getUuid());
        serviceInstance.getUses().add(portTarget);
        BaseArtifactTarget wsdlTarget = new BaseArtifactTarget();
        wsdlTarget.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
        wsdlTarget.setValue(wsdl.getUuid());
        serviceInstance.getDescribedBy().add(wsdlTarget);
        serviceInstance = (ServiceInstance) persistenceManager.persistArtifact(serviceInstance, null);
        
        serviceOperation = new ServiceOperation();
        serviceOperation.setArtifactType(BaseArtifactEnum.SERVICE_OPERATION);
        serviceOperation.setName("SampleServiceOperation");
        serviceOperation.getDocumentation().add(documentationTarget);
        derivedOperationTarget = new DerivedArtifactTarget();
        derivedOperationTarget.setArtifactType(DerivedArtifactEnum.OPERATION);
        derivedOperationTarget.setValue(operation.getUuid());
        serviceOperation.setOperationDefinedBy(derivedOperationTarget);
        serviceOperation = (ServiceOperation) persistenceManager.persistArtifact(serviceOperation, null);
    }

    private Element element1;
    private ElementTarget elementTarget1;
    private Element element2;
    private ElementTarget elementTarget2;
    private Event event1;
    private EventTarget eventTarget1;
    private Event event2;
    private EventTarget eventTarget2;
    private Event event3;
    private EventTarget eventTarget3;
    private Orchestration orchestration1;
    private OrchestrationTarget orchestrationTarget1;
    private OrchestrationProcess orchestrationProcess1;
    private OrchestrationProcessTarget orchestrationProcessTarget1;
    private Policy policy1;
    private PolicyTarget policyTarget1;
    private Service service1;
    private ServiceTarget serviceTarget1;
    private Task task1;
    private TaskTarget taskTarget1;
    private void insertSoaArtifacts() throws Exception {
        // Note: It is possible to persist a single set of Element artifacts for use in the test, assertions,
        // relationships, etc.  But, to keep things simple, just create an isolated set here, even if it duplicates
        // artifacts created by #testSOAModel.
        element1 = new Element();
        element1.setArtifactType(BaseArtifactEnum.ELEMENT);
        element1 = persistAndAssert(element1);
        elementTarget1 = new ElementTarget();
        elementTarget1.setArtifactType(ElementEnum.ELEMENT);
        elementTarget1.setValue(element1.getUuid());
        element2 = new Element();
        element2.setArtifactType(BaseArtifactEnum.ELEMENT);
        element2 = persistAndAssert(element2);
        elementTarget2 = new ElementTarget();
        elementTarget2.setArtifactType(ElementEnum.ELEMENT);
        elementTarget2.setValue(element2.getUuid());
        event1 = new Event();
        event1.setArtifactType(BaseArtifactEnum.EVENT);
        event1 = persistAndAssert(event1);
        eventTarget1 = new EventTarget();
        eventTarget1.setArtifactType(EventEnum.EVENT);
        eventTarget1.setValue(event1.getUuid());
        event2 = new Event();
        event2.setArtifactType(BaseArtifactEnum.EVENT);
        event2 = persistAndAssert(event2);
        eventTarget2 = new EventTarget();
        eventTarget2.setArtifactType(EventEnum.EVENT);
        eventTarget2.setValue(event2.getUuid());
        event3 = new Event();
        event3.setArtifactType(BaseArtifactEnum.EVENT);
        event3 = persistAndAssert(event3);
        eventTarget3 = new EventTarget();
        eventTarget3.setArtifactType(EventEnum.EVENT);
        eventTarget3.setValue(event3.getUuid());
        orchestration1 = new Orchestration();
        orchestration1.setArtifactType(BaseArtifactEnum.ORCHESTRATION);
        orchestration1 = persistAndAssert(orchestration1);
        orchestrationTarget1 = new OrchestrationTarget();
        orchestrationTarget1.setArtifactType(OrchestrationEnum.ORCHESTRATION);
        orchestrationTarget1.setValue(orchestration1.getUuid());
        orchestrationProcess1 = new OrchestrationProcess();
        orchestrationProcess1.setArtifactType(BaseArtifactEnum.ORCHESTRATION_PROCESS);
        orchestrationProcess1 = persistAndAssert(orchestrationProcess1);
        orchestrationProcessTarget1 = new OrchestrationProcessTarget();
        orchestrationProcessTarget1.setArtifactType(OrchestrationProcessEnum.ORCHESTRATION_PROCESS);
        orchestrationProcessTarget1.setValue(orchestrationProcess1.getUuid());
        policy1 = new Policy();
        policy1.setArtifactType(BaseArtifactEnum.POLICY);
        policy1 = persistAndAssert(policy1);
        policyTarget1 = new PolicyTarget();
        policyTarget1.setArtifactType(PolicyEnum.POLICY);
        policyTarget1.setValue(policy1.getUuid());
        service1 = new Service();
        service1.setArtifactType(BaseArtifactEnum.SERVICE);
        service1 = persistAndAssert(service1);
        serviceTarget1 = new ServiceTarget();
        serviceTarget1.setArtifactType(ServiceEnum.SERVICE);
        serviceTarget1.setValue(service1.getUuid());
        task1 = new Task();
        task1.setArtifactType(BaseArtifactEnum.TASK);
        task1 = persistAndAssert(task1);
        taskTarget1 = new TaskTarget();
        taskTarget1.setArtifactType(TaskEnum.TASK);
        taskTarget1.setValue(task1.getUuid());
    }
    
    private int counter = 0;
    private <T extends SoaModelType> T persistAndAssert(T artifact) throws Exception {
        artifact.setName("Sample" + artifact.getClass().getSimpleName() + counter++);
        artifact.getDocumentation().add(documentationTarget);
        
        artifact = (T) persistenceManager.persistArtifact(artifact, null);
        T persistedArtifact = (T) getArtifactByUUID(artifact.getUuid());
        
        assertBasic(persistedArtifact, artifact);
        assertDocumentation(persistedArtifact.getDocumentation());
        
        return persistedArtifact;
    }
    
    private Element persistAndAssertElement(Element artifact) throws Exception {
        artifact.getRepresents().add(elementTarget1);
        artifact.getUses().add(elementTarget2);
        artifact.getPerforms().add(serviceTarget1);
        artifact.setDirectsOrchestration(orchestrationTarget1);
        artifact.setDirectsOrchestrationProcess(orchestrationProcessTarget1);
        artifact.getGenerates().add(eventTarget1);
        artifact.getRespondsTo().add(eventTarget2);
        // Just to make sure we can add more than one, make 'er so.
        artifact.getRespondsTo().add(eventTarget3);
        
        artifact = persistAndAssert(artifact);

        assertTargets(artifact.getRepresents(), 1, element1);
        assertTargets(artifact.getUses(), 1, element2);
        assertTargets(artifact.getPerforms(), 1, service1);
        assertTarget(artifact.getDirectsOrchestration(), orchestration1);
        assertTarget(artifact.getDirectsOrchestrationProcess(), orchestrationProcess1);
        assertTargets(artifact.getGenerates(), 1, event1);
        assertTargets(artifact.getRespondsTo(), 2, event2, event3);
        
        return artifact;
    }
    
    private Actor persistAndAssertActor(Actor artifact) throws Exception {
        artifact.getDoes().add(taskTarget1);
        artifact.getSetsPolicy().add(policyTarget1);
        
        artifact = (Actor) persistAndAssertElement(artifact);

        assertTargets(artifact.getDoes(), 1, task1);
        assertTargets(artifact.getSetsPolicy(), 1, policy1);
        
        return artifact;
    }
    
    private void assertDocumentation(List<DocumentArtifactTarget> documentation) throws Exception {
        assertNotNull(documentation);
        assertTrue(documentation.size() > 0);
        for (DocumentArtifactTarget target : documentation) {
            WsdlDocument wsdlByTarget = (WsdlDocument) getArtifactByTarget(target);
            assertEquals(wsdl.getUuid(), wsdlByTarget.getUuid());
        }
    }
    
    private <T extends Target> void assertTargets(List<T> elementTargets, int expectedSize,
            BaseArtifactType... expectedArtifacts) throws Exception {
        assertNotNull(elementTargets);
        assertEquals(expectedSize, elementTargets.size());
        for (int i = 0; i < elementTargets.size(); i++) {
            assertTarget(elementTargets.get(i), expectedArtifacts[i]);
        }
    }
    
    private <T extends Target> void assertTarget(T elementTarget, BaseArtifactType expectedArtifact)
            throws Exception {
        BaseArtifactType artifactByTarget = (BaseArtifactType) getArtifactByTarget(elementTarget);
        assertBasic(artifactByTarget, expectedArtifact);
    }
}
