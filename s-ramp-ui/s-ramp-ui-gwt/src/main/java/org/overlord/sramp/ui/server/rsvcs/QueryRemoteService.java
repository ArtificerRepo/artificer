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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.beans.PageInfo;
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
	 * @see org.overlord.sramp.ui.shared.rsvcs.IQueryRemoteService#findArtifacts(org.overlord.sramp.ui.shared.beans.PageInfo)
	 */
	@Override
	public List<ArtifactSummary> findArtifacts(final PageInfo page) throws RemoteServiceException {
		Collections.sort(artifacts, new Comparator<ArtifactSummary>() {
			@Override
			public int compare(ArtifactSummary as1, ArtifactSummary as2) {
				int rval = as1.getName().compareTo(as2.getName());
				if (!page.isAscending())
					rval *= -1;
				return rval;
			}
		});
		List<ArtifactSummary> rval = new ArrayList<ArtifactSummary>();
		int startIdx = page.getPage() * page.getPageSize();
		int endIdx = startIdx + page.getPageSize() - 1;
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
