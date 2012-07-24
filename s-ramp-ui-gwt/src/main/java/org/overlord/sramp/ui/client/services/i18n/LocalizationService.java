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

import org.overlord.sramp.ui.client.services.AbstractService;
import org.overlord.sramp.ui.client.services.IServiceLifecycleListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * A concrete implementation of an {@link ILocalizationService}.
 *
 * @author eric.wittmann@redhat.com
 */
public class LocalizationService extends AbstractService implements ILocalizationService {

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
		String url = GWT.getHostPageBaseURL() + "i18n";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		try {
			builder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					String jsonData = response.getText();
					initDictionary(jsonData);
					serviceListener.onStarted();
				}
				@Override
				public void onError(Request request, Throwable exception) {
					serviceListener.onError(exception);
				}
			});
		} catch (RequestException e) {
			serviceListener.onError(e);
		}
	}
	
	/**
	 * Initializes the localization dictionary.
	 * @param jsonData
	 */
	protected void initDictionary(String jsonData) {
		this.messages = LocalizationDictionary.create(jsonData);
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
