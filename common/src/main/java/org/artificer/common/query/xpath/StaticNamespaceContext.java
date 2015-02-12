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
package org.artificer.common.query.xpath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

/**
 * A static, map-based namespace context for resolving prefixes to namespaces in an S-RAMP
 * X-Path formatted Query.
 *
 * @author eric.wittmann@redhat.com
 */
public class StaticNamespaceContext implements NamespaceContext {

	/** Mapping of prefix->namespace. */
	private Map<String, String> nsMapping = new HashMap<String, String>();
	
	/**
	 * Default constructor.
	 */
	public StaticNamespaceContext() {
	}
	
	/**
	 * Adds a mapping.
	 * @param prefix namespace prefix
	 * @param namespace namespace
	 */
	public void addMapping(String prefix, String namespace) {
		this.nsMapping.put(prefix, namespace);
	}
	
	/**
	 * Removes a mapping.
	 * @param prefix namespace prefix
	 */
	public void removeMapping(String prefix) {
		this.nsMapping.remove(prefix);
	}

	/**
	 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
	@Override
	public String getNamespaceURI(String prefix) {
		return this.nsMapping.get(prefix);
	}

	/**
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	@Override
	public String getPrefix(String namespaceURI) {
		for (Entry<String, String> entry : this.nsMapping.entrySet()) {
			if (entry.getValue().equals(namespaceURI)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	 */
	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		Set<String> prefixes = new HashSet<String>();
		for (Entry<String, String> entry : this.nsMapping.entrySet()) {
			if (entry.getValue().equals(namespaceURI)) {
				prefixes.add(entry.getKey());
			}
		}
		return prefixes.iterator();
	}

}
