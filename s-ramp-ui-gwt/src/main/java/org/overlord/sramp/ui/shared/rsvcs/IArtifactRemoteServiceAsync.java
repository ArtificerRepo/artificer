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
package org.overlord.sramp.ui.shared.rsvcs;

import org.overlord.sramp.ui.shared.beans.ArtifactDetails;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async version of {@link IArtifactRemoteService}.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IArtifactRemoteServiceAsync {

	/**
	 * Async version of {@link IArtifactRemoteService#getArtifactDetails(String, String, String)}
	 */
	public void getArtifactDetails(String model, String type, String artifactUUID,
			AsyncCallback<ArtifactDetails> callback);

}
