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

import org.overlord.sramp.ui.client.services.IService;
import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.beans.PageInfo;
import org.overlord.sramp.ui.shared.types.ArtifactTypeFilter;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A service that provides a way to query the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IQueryService extends IService {

	/**
	 * Finds artifacts using the remote query service.
	 * @param page
	 * @param typeFilter
	 * @param nameFilter
	 * @param callback
	 */
	public void findArtifactsAsync(PageInfo page, ArtifactTypeFilter typeFilter, String nameFilter, AsyncCallback<List<ArtifactSummary>> callback);

}
