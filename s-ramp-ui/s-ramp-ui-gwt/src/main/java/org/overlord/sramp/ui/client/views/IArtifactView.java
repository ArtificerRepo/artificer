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

import org.overlord.sramp.ui.client.activities.IArtifactActivity;
import org.overlord.sramp.ui.client.places.ArtifactPlace;
import org.overlord.sramp.ui.shared.beans.ArtifactDetails;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

/**
 * Artifact view interface.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IArtifactView extends IView<IArtifactActivity> {
	
	/**
	 * Called by the activity when it begins downloading the artifact.
	 * @param currentPlace
	 */
	public void onArtifactLoading(ArtifactPlace currentPlace);
	
	/**
	 * Called by the activity when the artifact download completes.
	 * @param artifact
	 */
	public void onArtifactLoaded(ArtifactDetails artifact);
	
	/**
	 * Called by the activity when the download of the full artifact details
	 * fails for some reason.
	 * @param error
	 */
	public void onArtifactLoadError(RemoteServiceException error);
	
}
