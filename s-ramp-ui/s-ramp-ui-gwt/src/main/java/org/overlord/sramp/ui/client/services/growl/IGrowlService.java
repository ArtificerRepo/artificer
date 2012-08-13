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
package org.overlord.sramp.ui.client.services.growl;

import org.overlord.sramp.ui.client.services.IService;

/**
 * A lightweight notification service.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IGrowlService extends IService {

	/**
	 * Growls a simple message at the user.
	 * @param title
	 * @param message
	 * @return the unique ID of the growl
	 */
	public int growl(String title, String message);

	/**
	 * Growls at the user.
	 * @param title
	 * @param message
	 * @param type
	 * @return the unique ID of the growl
	 */
	public int growl(String title, String message, GrowlType type);

	/**
	 * Called by clients to inform the growl service that a progress style growl has 
	 * completed.
	 * @param growlId
	 */
	public void onProgressComplete(int growlId, String message);

}
