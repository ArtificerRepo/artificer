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

import org.overlord.sramp.ui.client.services.IService;

/**
 * A simple localization service.  This service is responsible for providing localized translations
 * of strings that are shown to the user.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ILocalizationService extends IService {

	/**
	 * Called to translate a message using whatever the current locale might be.
	 * @param key the message key
	 * @return the translated message
	 */
	public String translate(String key, Object ... args);
	
}
