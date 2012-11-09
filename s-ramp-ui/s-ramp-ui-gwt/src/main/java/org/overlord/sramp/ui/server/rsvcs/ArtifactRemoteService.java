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
package org.overlord.sramp.ui.server.rsvcs;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.ui.server.api.SrampAtomApiClient;
import org.overlord.sramp.ui.server.util.ExceptionUtils;
import org.overlord.sramp.ui.shared.beans.ArtifactDetails;
import org.overlord.sramp.ui.shared.rsvcs.IArtifactRemoteService;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of the artifact remote service.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactRemoteService extends RemoteServiceServlet implements IArtifactRemoteService {

	private static final long serialVersionUID = ArtifactRemoteService.class.hashCode();

	/**
	 * Constructor.
	 */
	public ArtifactRemoteService() {
	}

	/**
	 * @see org.overlord.sramp.ui.shared.rsvcs.IArtifactRemoteService#getArtifactDetails(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ArtifactDetails getArtifactDetails(String model, String type, String artifactUUID)
			throws RemoteServiceException {
		try {
			ArtifactType artyType = ArtifactType.valueOf(type);
			BaseArtifactType artifact = SrampAtomApiClient.getInstance().getArtifactMetaData(artyType, artifactUUID);

			ArtifactDetails details = new ArtifactDetails();
			details.setModel(artyType.getArtifactType().getModel());
			details.setType(artyType.getArtifactType().getType());
			details.setUuid(artifact.getUuid());
			details.setName(artifact.getName());
			details.setDescription(artifact.getDescription());
			details.setCreatedBy(artifact.getCreatedBy());
			details.setCreatedOn(artifact.getCreatedTimestamp().toGregorianCalendar().getTime());
			details.setUpdatedOn(artifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
			details.setDerived(artyType.getArtifactType().isDerived());

			for (Property property : artifact.getProperty()) {
				details.setProperty(property.getPropertyName(), property.getPropertyValue());
			}
			details.getClassifiedBy().addAll(artifact.getClassifiedBy());
			details.setUpdatedBy(artifact.getLastModifiedBy());

			return details;
		} catch (Throwable t) {
			throw ExceptionUtils.createRemoteException(t);
		}
	}

}
