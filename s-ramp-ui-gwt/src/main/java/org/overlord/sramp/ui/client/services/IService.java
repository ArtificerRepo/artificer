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
package org.overlord.sramp.ui.client.services;

/**
 * All client-side services should implement this interface.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IService {

	/**
	 * Called to start the service.  The service *must* call the IServiceListener back when it
	 * is done starting.  This provides the Service with an opportunity to do asynchronous
	 * start logic.
	 */
	public void start(IServiceLifecycleListener serviceListener);
	
}
