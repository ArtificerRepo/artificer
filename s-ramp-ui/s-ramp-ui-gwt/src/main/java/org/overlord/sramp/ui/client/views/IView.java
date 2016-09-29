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

import org.overlord.sramp.ui.client.activities.IActivity;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Base View interface - all specific view interfaces must extend this one.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IView<A extends IActivity> extends IsWidget {

	/**
	 * Gets the activity for this view.
	 */
	public A getActivity();

	/**
	 * Sets the activity for this view.
	 * @param activity
	 */
	public void setActivity(A activity);
	
}
