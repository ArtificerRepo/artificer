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
package org.overlord.sramp.server.atom.workspaces;

import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.common.ArtifactTypeEnum;

/**
 * Models the S-RAMP SOA workspace.
 *
 * @author Brett Meyer
 */
public class SoaWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = 1498525113317933041L;

	/**
	 * Constructor.
	 * @param hrefBase
	 */
	public SoaWorkspace(String hrefBase) {
		super(hrefBase, "SOA Model"); //$NON-NLS-1$
	}

	/**
	 * @see org.overlord.sramp.common.server.atom.workspaces.AbstractWorkspace#configureWorkspace()
	 */
	@Override
	protected void configureWorkspace() {
        AppCollection soaCollection = addCollection("/s-ramp/soa", "SOA Model Objects", MediaType.APPLICATION_ZIP); //$NON-NLS-1$ //$NON-NLS-2$
        AppCollection effectCollection = addCollection("/s-ramp/soa/Effect", "Effect", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection eventCollection = addCollection("/s-ramp/soa/Event", "Event", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection informationTypeCollection = addCollection("/s-ramp/soa/InformationType", "InformationType", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection policyCollection = addCollection("/s-ramp/soa/Policy", "Policy", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection policySubjectCollection = addCollection("/s-ramp/soa/PolicySubject", "PolicySubject", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection elementCollection = addCollection("/s-ramp/soa/Element", "Element", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection actorCollection = addCollection("/s-ramp/soa/Actor", "Actor", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection organizationCollection = addCollection("/s-ramp/soa/Organization", "Organization", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection serviceCollection = addCollection("/s-ramp/soa/Service", "Service", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection systemCollection = addCollection("/s-ramp/soa/System", "System", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection compositionCollection = addCollection("/s-ramp/soa/Composition", "Composition", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection choreographyCollection = addCollection("/s-ramp/soa/Choreography", "Choreography", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection collaborationCollection = addCollection("/s-ramp/soa/Collaboration", "Collaboration", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection orchestrationCollection = addCollection("/s-ramp/soa/Orchestration", "Orchestration", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection processCollection = addCollection("/s-ramp/soa/Process", "Process", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection choreographyProcessCollection = addCollection("/s-ramp/soa/ChoreographyProcess", "ChoreographyProcess", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection collaborationProcessCollection = addCollection("/s-ramp/soa/CollaborationProcess", "CollaborationProcess", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection orchestrationProcessCollection = addCollection("/s-ramp/soa/OrchestrationProcess", "OrchestrationProcess", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection serviceCompositionCollection = addCollection("/s-ramp/soa/ServiceComposition", "ServiceComposition", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection taskCollection = addCollection("/s-ramp/soa/Task", "Task", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection serviceContractCollection = addCollection("/s-ramp/soa/ServiceContract", "ServiceContract", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection serviceInterfaceCollection = addCollection("/s-ramp/soa/ServiceInterface", "ServiceInterface", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        addTypeCategory(soaCollection, ArtifactTypeEnum.Effect);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Event);
        addTypeCategory(soaCollection, ArtifactTypeEnum.InformationType);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Policy);
        addTypeCategory(soaCollection, ArtifactTypeEnum.PolicySubject);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Element);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Actor);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Organization);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Service);
        addTypeCategory(soaCollection, ArtifactTypeEnum.System);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Composition);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Choreography);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Collaboration);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Orchestration);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Process);
        addTypeCategory(soaCollection, ArtifactTypeEnum.ChoreographyProcess);
        addTypeCategory(soaCollection, ArtifactTypeEnum.CollaborationProcess);
        addTypeCategory(soaCollection, ArtifactTypeEnum.OrchestrationProcess);
        addTypeCategory(soaCollection, ArtifactTypeEnum.ServiceComposition);
        addTypeCategory(soaCollection, ArtifactTypeEnum.Task);
        addTypeCategory(soaCollection, ArtifactTypeEnum.ServiceContract);
        addTypeCategory(soaCollection, ArtifactTypeEnum.ServiceInterface);

        addTypeCategory(effectCollection, ArtifactTypeEnum.Effect);
        addTypeCategory(eventCollection, ArtifactTypeEnum.Event);
        addTypeCategory(informationTypeCollection, ArtifactTypeEnum.InformationType);
        addTypeCategory(policyCollection, ArtifactTypeEnum.Policy);
        addTypeCategory(policySubjectCollection, ArtifactTypeEnum.PolicySubject);
        addTypeCategory(elementCollection, ArtifactTypeEnum.Element);
        addTypeCategory(actorCollection, ArtifactTypeEnum.Actor);
        addTypeCategory(organizationCollection, ArtifactTypeEnum.Organization);
        addTypeCategory(serviceCollection, ArtifactTypeEnum.Service);
        addTypeCategory(systemCollection, ArtifactTypeEnum.System);
        addTypeCategory(compositionCollection, ArtifactTypeEnum.Composition);
        addTypeCategory(choreographyCollection, ArtifactTypeEnum.Choreography);
        addTypeCategory(collaborationCollection, ArtifactTypeEnum.Collaboration);
        addTypeCategory(orchestrationCollection, ArtifactTypeEnum.Orchestration);
        addTypeCategory(processCollection, ArtifactTypeEnum.Process);
        addTypeCategory(choreographyProcessCollection, ArtifactTypeEnum.ChoreographyProcess);
        addTypeCategory(collaborationProcessCollection, ArtifactTypeEnum.CollaborationProcess);
        addTypeCategory(orchestrationProcessCollection, ArtifactTypeEnum.OrchestrationProcess);
        addTypeCategory(serviceCompositionCollection, ArtifactTypeEnum.ServiceComposition);
        addTypeCategory(taskCollection, ArtifactTypeEnum.Task);
        addTypeCategory(serviceContractCollection, ArtifactTypeEnum.ServiceContract);
        addTypeCategory(serviceInterfaceCollection, ArtifactTypeEnum.ServiceInterface);
    }
}
