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
package org.overlord.sramp.ui.client.services.query;

import java.util.List;

import org.overlord.sramp.ui.client.services.AbstractService;
import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.beans.PageInfo;
import org.overlord.sramp.ui.shared.rsvcs.IQueryRemoteService;
import org.overlord.sramp.ui.shared.rsvcs.IQueryRemoteServiceAsync;
import org.overlord.sramp.ui.shared.types.ArtifactFilter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Implementation of the {@link IQueryService}.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryService extends AbstractService implements IQueryService {
	
	private final IQueryRemoteServiceAsync queryRemoteService = GWT.create(IQueryRemoteService.class);

	/**
	 * Constructor.
	 */
	public QueryService() {
	}

	/**
	 * @see org.overlord.sramp.ui.client.services.query.IQueryService#findArtifactsAsync(org.overlord.sramp.ui.shared.beans.PageInfo, org.overlord.sramp.ui.shared.types.ArtifactFilter, com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	@Override
	public void findArtifactsAsync(PageInfo page, ArtifactFilter filter,
			AsyncCallback<List<ArtifactSummary>> callback) {
		queryRemoteService.findArtifacts(page, filter, callback);
	}
	
}
