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
package org.overlord.sramp.client.ontology;

import java.util.Date;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Source;

/**
 * Models a summary of a single S-RAMP ontology from a Feed.
 *
 * @author eric.wittmann@redhat.com
 */
public class OntologySummary {

	private Entry entry;

	/**
	 * Constructor.
	 * @param entry
	 */
	public OntologySummary(Entry entry) {
		this.entry = entry;
	}

	/**
	 * @return the ontology uuid
	 */
	public String getUuid() {
		return this.entry.getId().toString().replace("urn:uuid:", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return the ontology's last modified timestamp
	 */
	public Date getLastModifiedTimestamp() {
		return entry.getUpdated();
	}

	/**
	 * @return the ontology's label
	 */
	public String getLabel() {
		return entry.getTitle();
	}

	/**
	 * @return the ontology's created timestamp
	 */
	public Date getCreatedTimestamp() {
		return entry.getPublished();
	}

	/**
	 * @return the ontology's created by
	 */
	public String getCreatedBy() {
		return entry.getAuthors().get(0).getName();
	}

	/**
	 * @return the ontology's comment
	 */
	public String getComment() {
		return entry.getSummary();
	}

	/**
	 * @return the ontology's comment
	 */
	public String getBase() {
		Source source = entry.getSource();
		if (source != null && source.getBase() != null) {
			return source.getBase().toString();
		} else {
			return null;
		}
	}

	public String getId() {
		Source source = entry.getSource();
		if (source != null && source.getId() != null) {
			return source.getId().toString();
		} else {
			return null;
		}
	}

}
