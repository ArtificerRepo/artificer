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
package org.overlord.sramp.ui.client.util;

import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;
import org.overlord.sramp.ui.client.widgets.dialogs.ErrorDialog;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;

/**
 * Base class for UI async handlers that does some basic error processing.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class RemoteServiceAsyncCallback<T> implements AsyncCallback<T> {
	
	/**
	 * Constructor.
	 */
	public RemoteServiceAsyncCallback() {
	}

	/**
	 * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
	 */
	@Override
	public final void onFailure(Throwable caught) {
		if (caught instanceof IncompatibleRemoteServiceException) {
			onIncompatibleRemoteServiceFailure((IncompatibleRemoteServiceException) caught);
		}
		if (caught instanceof RemoteServiceException) {
			onRemoteServiceFailure((RemoteServiceException) caught);
		}
	}

	/**
	 * Subclasses can override if they choose - handles the case when the remote service is
	 * incompatible with the client.  This can happen when the server is updated but the client
	 * is not.
	 * 
	 * By default this method will display an error dialog to the user, prompting them to 
	 * refresh the browser.
	 * @param caught
	 */
	protected void onIncompatibleRemoteServiceFailure(IncompatibleRemoteServiceException caught) {
		ILocalizationService i18n = Services.getServices().getService(ILocalizationService.class);

		ErrorDialog dialog = new ErrorDialog(
				i18n.translate("errors.incompatible-service.title"), 
				i18n.translate("errors.incompatible-service.message"), 
				null);
		dialog.center();
		dialog.show();
	}

	/**
	 * Subclasses must implement this to handle the 'typical' case where an exception is 
	 * thrown from a remote service.
	 * @param caught
	 */
	protected abstract void onRemoteServiceFailure(RemoteServiceException caught);
}
