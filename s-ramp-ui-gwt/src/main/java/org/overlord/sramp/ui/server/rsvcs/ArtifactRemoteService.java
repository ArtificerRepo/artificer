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

import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.ui.server.api.SrampAtomApiClient;
import org.overlord.sramp.ui.server.util.ExceptionUtils;
import org.overlord.sramp.ui.server.visitors.RelationshipVisitor;
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
			ArtifactType artyType = null;
			if (type == null) {
				artyType = resolveArtifactType(artifactUUID);
			} else {
				artyType = ArtifactType.valueOf(type);
			}
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
			details.setUpdatedBy(artifact.getLastModifiedBy());
			details.setDerived(artyType.getArtifactType().isDerived());
			// Properties
			for (Property property : artifact.getProperty()) {
				details.setProperty(property.getPropertyName(), property.getPropertyValue());
			}
			// Classifications
			details.getClassifiedBy().addAll(artifact.getClassifiedBy());
			// Relationships
			RelationshipVisitor visitor = new RelationshipVisitor(details);
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);

			return details;
		} catch (Throwable t) {
			throw ExceptionUtils.createRemoteException(t);
		}
	}

	/**
	 * Looks up the artifact type for a given artifact UUID.  This is unfortunately done by
	 * issueing a query to the S-RAMP repository.
	 * @param artifactUUID
	 */
	private ArtifactType resolveArtifactType(String artifactUUID) throws Exception {
		// Even though there really isn't much of an injection danger, we still make
		// sure to fail if someone tries it.
		if (artifactUUID.contains("'")) {
			throw new Exception("Invalid UUID: " + artifactUUID);
		}
		String q = String.format("/s-ramp[@uuid = '%1$s']", artifactUUID);
		QueryResultSet resultSet = SrampAtomApiClient.getInstance().query(q);
		if (resultSet.size() == 1) {
			ArtifactSummary summary = resultSet.iterator().next();
			return summary.getType();
		} else {
			throw new Exception("Failed to find artifact with UUID '" + artifactUUID + "'.");
		}

	}

}
