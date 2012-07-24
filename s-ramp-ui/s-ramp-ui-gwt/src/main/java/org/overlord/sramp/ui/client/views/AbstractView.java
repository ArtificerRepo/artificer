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

import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for all view implementations.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractView<A extends IActivity> extends Composite implements IView<A> {
	
	private A activity;
	
	/**
	 * Constructor.
	 */
	public AbstractView() {
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.views.IView#getActivity()
	 */
	@Override
	public A getActivity() {
		return activity;
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IView#setActivity(org.overlord.sramp.ui.client.activities.IActivity)
	 */
	@Override
	public void setActivity(A activity) {
		this.activity = activity;
	}

}
