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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.rsvcs.IQueryRemoteService;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of the query remote service.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryRemoteService extends RemoteServiceServlet implements IQueryRemoteService {

	private static final long serialVersionUID = QueryRemoteService.class.hashCode();
	
	private static final List<ArtifactSummary> artifacts = new ArrayList<ArtifactSummary>();
	static {
		for (int i=0; i < 100; i++)
			artifacts.add(createArtifact("artifact-"+i+".xsd"));
	}

	/**
	 * Constructor.
	 */
	public QueryRemoteService() {
	}

	/**
	 * @see org.overlord.sramp.ui.shared.rsvcs.IQueryRemoteService#findArtifacts(int, int)
	 */
	@Override
	public List<ArtifactSummary> findArtifacts(int page, int pageSize) throws RemoteServiceException {
		List<ArtifactSummary> rval = new ArrayList<ArtifactSummary>();
		int startIdx = page * pageSize;
		int endIdx = startIdx + pageSize - 1;
		for (int idx = startIdx; idx <= endIdx; idx++) {
			if (idx < artifacts.size()) {
				rval.add(artifacts.get(idx));
			}
		}
		return rval;
	}

	/**
	 * TODO REMOVE THIS - SAMPLE DATA ONLY.
	 */
	private static ArtifactSummary createArtifact(String name) {
		ArtifactSummary arty = new ArtifactSummary();
		String uuid = UUID.randomUUID().toString();
		arty.setUuid(uuid);
		arty.setName(name);
		arty.setDescription("This is the description of the artifact currently know as: " + name + " and UUID: " + uuid);
		arty.setCreatedBy("anonymous");
		arty.setCreatedOn(new Date());
		arty.setUpdatedOn(new Date());
		return arty;
	}

}
