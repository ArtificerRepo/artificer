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
package org.overlord.sramp.ui.shared.types;

/**
 * Enum shared between the browse view and the remote query service
 * to determine the currently active filter.
 *
 * @author eric.wittmann@redhat.com
 */
public enum ArtifactFilter {

	all(null, "/s-ramp", "views.browse.artifact.filter.all"),
	xml("xml", "/s-ramp/core/XmlDocument", "views.browse.artifact.filter.xml"),
	xsd("xsd", "/s-ramp/xsd/XsdDocument", "views.browse.artifact.filter.xsd"),
	wsdl("wsdl", "/s-ramp/wsdl/WsdlDocument", "views.browse.artifact.filter.wsdl"),
	policy("policy", "/s-ramp/policy/PolicyDocument", "views.browse.artifact.filter.policy"),
	doc("doc", "/s-ramp/core/Document", "views.browse.artifact.filter.doc");

	private String code;
	private String queryBase;
	private String i18nKey;

	/**
	 * Constructor.
	 * @param code
	 * @param queryBase
	 * @param i18nKey
	 */
	private ArtifactFilter(String code, String queryBase, String i18nKey) {
		this.code = code;
		this.queryBase = queryBase;
		this.i18nKey = i18nKey;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the queryBase
	 */
	public String getQueryBase() {
		return queryBase;
	}

	/**
	 * @return the i18nKey
	 */
	public String getI18nKey() {
		return i18nKey;
	}

}
