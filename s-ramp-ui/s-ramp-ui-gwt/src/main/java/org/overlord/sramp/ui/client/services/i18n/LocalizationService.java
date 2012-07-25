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
package org.overlord.sramp.ui.client.services.i18n;

import java.util.Map;

import org.overlord.sramp.ui.client.services.AbstractService;
import org.overlord.sramp.ui.client.services.IServiceLifecycleListener;
import org.overlord.sramp.ui.shared.rsvcs.ILocalizationRemoteService;
import org.overlord.sramp.ui.shared.rsvcs.ILocalizationRemoteServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A concrete implementation of an {@link ILocalizationService}.
 *
 * @author eric.wittmann@redhat.com
 */
public class LocalizationService extends AbstractService implements ILocalizationService {

	private final ILocalizationRemoteServiceAsync localizationRemoteService = GWT.create(ILocalizationRemoteService.class);

	private LocalizationDictionary messages;
	
	/**
	 * Constructor.
	 */
	public LocalizationService() {
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.services.AbstractService#start(org.overlord.sramp.ui.client.services.IServiceLifecycleListener)
	 */
	@Override
	public void start(final IServiceLifecycleListener serviceListener) {
		localizationRemoteService.getMessages(new AsyncCallback<Map<String, String>>() {
			@Override
			public void onSuccess(Map<String, String> result) {
				initDictionary(result);
				serviceListener.onStarted();
			}
			@Override
			public void onFailure(Throwable caught) {
				serviceListener.onError(caught);
			}
		});
	}

	/**
	 * Initializes the localization dictionary.
	 * @param messages
	 */
	protected void initDictionary(Map<String, String> messages) {
		this.messages = LocalizationDictionary.create(messages);
	}

	/**
	 * @see org.overlord.sramp.ui.client.services.i18n.ILocalizationService#translate(java.lang.String, java.lang.String)
	 */
	@Override
	public String translate(String key) {
		String val = this.messages.get(key);
		if (val == null)
			return "**" + key + "**";
		else
			return val;
	}
	
}
