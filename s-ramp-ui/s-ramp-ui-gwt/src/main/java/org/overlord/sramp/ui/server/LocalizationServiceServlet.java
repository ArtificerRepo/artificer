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
package org.overlord.sramp.ui.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;

/**
 * Provides the data used by the {@link ILocalizationService}.
 *
 * @author eric.wittmann@redhat.com
 */
public class LocalizationServiceServlet extends HttpServlet {

	private static final long serialVersionUID = LocalizationServiceServlet.class.hashCode();

	/**
	 * Constructor.
	 */
	public LocalizationServiceServlet() {
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		Locale locale = req.getLocale();
		Properties messages = getMessages(locale);
		writeMessageToResponse(messages, resp);
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

	/**
	 * Writes the messages in the {@link Properties} object to the response as a JSON object.
	 * @param messages the localized messages
	 * @param response the http response
	 * @throws IOException 
	 */
	private void writeMessageToResponse(Properties messages, HttpServletResponse response) throws IOException {
		response.setContentType("application/json; charset=UTF-8");
		StringBuilder builder = new StringBuilder();
		
		builder.append("({");
		Set<Entry<Object,Object>> entrySet = messages.entrySet();
		boolean first = true;
		for (Entry<Object, Object> entry : entrySet) {
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			if (first)
				first = false;
			else
				builder.append(",");
			builder.append("\r\n\t\"");
			builder.append(key);
			builder.append("\": \"");
			builder.append(val.replace("\"", "\\\""));
			builder.append("\"");
		}
		builder.append("\r\n})");
		
		String content = builder.toString();
		byte [] contentBytes = content.getBytes("UTF-8");
		response.setContentLength(contentBytes.length);
		response.getOutputStream().write(contentBytes);
	}
	
}
