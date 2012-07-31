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
package org.overlord.sramp.ui.client.views;

import java.util.List;

import org.overlord.sramp.ui.client.activities.IBrowseActivity;
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

/**
 * Browse view interface.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IBrowseView extends IPagedResultView<IBrowseActivity> {

	/**
	 * Called by the activity when it kicks off the async query for artifacts.
	 */
	public void onQueryStarting();
	
	/**
	 * Called by the activity when the async query completes successfully with data.
	 * @param artifacts
	 * @param place
	 * @param hasMoreRows
	 */
	public void onQueryComplete(List<ArtifactSummary> artifacts, BrowsePlace place, boolean hasMoreRows);
	
	/**
	 * Called by the activity when the async query completes with an error.
	 */
	public void onQueryFailed(RemoteServiceException error);

}
