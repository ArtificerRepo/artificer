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
package org.overlord.sramp.ui.server.rsvcs;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.overlord.sramp.ui.shared.rsvcs.ILocalizationRemoteService;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of the localization remote service.
 * 
 * TODO cache the messages in memory - no need to read them from the properties file every time
 *
 * @author eric.wittmann@redhat.com
 */
public class LocalizationRemoteService extends RemoteServiceServlet implements ILocalizationRemoteService {

	private static final long serialVersionUID = LocalizationRemoteService.class.hashCode();

	/**
	 * Constructor.
	 */
	public LocalizationRemoteService() {
	}

	/**
	 * @see org.overlord.sramp.ui.shared.rsvcs.ILocalizationRemoteService#getMessages()
	 */
	@Override
	public Map<String, String> getMessages() throws RemoteServiceException {
		try {
			Locale locale = this.getThreadLocalRequest().getLocale();
			Properties messages = getMessages(locale);
			HashMap<String, String> map = new HashMap<String, String>();
			for (Entry<Object, Object> entry : messages.entrySet())
				map.put((String) entry.getKey(), (String) entry.getValue());
			return map;
		} catch (IOException e) {
			throw new RemoteServiceException(e);
		}
	}

	/**
	 * Gets the proper collection of messages for the given locale.
	 * @param locale the current/desired locale
	 * @return the localized messages
	 * @throws IOException 
	 */
	private Properties getMessages(Locale locale) throws IOException {
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		if ("".equals(lang))
			lang = null;
		if ("".equals(country))
			country = null;

		Properties props = null;
		if (lang != null && country != null) {
			props = load("messages_" + lang + "_" + country + ".properties");
		}
		if (props == null && lang != null) {
			props = load("messages_" + lang + ".properties");
		}
		if (props == null) {
			props = load("messages.properties");
		}
		
		return props;
	}

	/**
	 * Loads messages from a messages.properties file.
	 * 
	 * @param messagesFileName the filename of the properties file containing the messages
	 * @return the messages
	 * @throws IOException 
	 */
	private Properties load(String messagesFileName) throws IOException {
		InputStream is = null;
		try {
			is = getClass().getClassLoader().getResourceAsStream("/i18n/" + messagesFileName);
			if (is == null)
				return null;
			Properties props = new Properties();
			props.load(is);
			return props;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

}
