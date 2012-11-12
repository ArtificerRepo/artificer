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
package org.overlord.sramp.ui.shared.beans;

import java.io.Serializable;

/**
 * Models the detailed information about a single artifact relationship.
 *
 * @author eric.wittmann@redhat.com
 */
public class RelationshipDetails implements Serializable {

	private static final long serialVersionUID = 7839458650041701411L;

	private String type;
	private String targetUuid;
	private String href;

	/**
	 * Constructor.
	 */
	public RelationshipDetails() {
		// c'tor needed for GWT RPC serialization
	}

	/**
	 * Constructor.
	 * @param type
	 */
	public RelationshipDetails(String type) {
		this.setType(type);
	}

	/**
	 * @return the targetUuid
	 */
	public String getTargetUuid() {
		return targetUuid;
	}

	/**
	 * @param targetUuid the targetUuid to set
	 */
	public void setTargetUuid(String targetUuid) {
		this.targetUuid = targetUuid;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @param href the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}



}
