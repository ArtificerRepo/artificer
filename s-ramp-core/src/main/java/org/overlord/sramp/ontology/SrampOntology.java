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
package org.overlord.sramp.ontology;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Models an s-ramp ontology.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampOntology {

	private String uuid;
	private String label;
	private String comment;
	private String base;
	private String id;
	private List<SrampOntology.Class> rootClasses = new ArrayList<SrampOntology.Class>();
	private Map<URI, SrampOntology.Class> classIndex = new HashMap<URI, SrampOntology.Class>();

	/**
	 * Constructor.
	 */
	public SrampOntology() {
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the base
	 */
	public String getBase() {
		return base;
	}

	/**
	 * @param base the base to set
	 */
	public void setBase(String base) {
		this.base = base;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the rootClasses
	 */
	public List<SrampOntology.Class> getRootClasses() {
		return rootClasses;
	}

	/**
	 * @param rootClasses the rootClasses to set
	 */
	public void setRootClasses(List<SrampOntology.Class> rootClasses) {
		this.rootClasses = rootClasses;
	}

	/**
	 * Creates a new class within this ontology.
	 * @param id
	 */
	public SrampOntology.Class createClass(String id) {
		SrampOntology.Class c = new SrampOntology.Class();
		c.setId(id);
		String uri = this.getBase() + "#" + id;
		try {
			c.setUri(new URI(uri));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return c;
	}

	/**
	 * Finds a class by its unique URI.
	 * @param uri
	 */
	public synchronized SrampOntology.Class findClass(URI uri) {
		if (classIndex.containsKey(uri)) {
			return classIndex.get(uri);
		} else {
			SrampOntology.Class found = null;
			for (SrampOntology.Class candidate : rootClasses) {
				found = candidate.findClass(uri);
				if (found != null) {
					break;
				}
			}
			if (found != null) {
				classIndex.put(uri, found);
			}
			return found;
		}
	}

	/**
	 * Models a single class in an s-ramp ontology.
	 *
	 * @author eric.wittmann@redhat.com
	 */
	public static class Class {

		private String id;
		private String label;
		private String comment;
		private URI uri;
		private SrampOntology.Class parent;
		private List<SrampOntology.Class> children = new ArrayList<SrampOntology.Class>();

		/**
		 * Constructor.
		 */
		public Class() {
		}

		/**
		 * Recursively finds a class matching the given URI.
		 * @param uri
		 */
		public Class findClass(URI uri) {
			if (this.uri.equals(uri)) {
				return this;
			} else {
				for (Class c : this.children) {
					Class found = c.findClass(uri);
					if (found != null) {
						return found;
					}
				}
			}
			return null;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * @param label the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * @return the comment
		 */
		public String getComment() {
			return comment;
		}

		/**
		 * @param comment the comment to set
		 */
		public void setComment(String comment) {
			this.comment = comment;
		}

		/**
		 * @return the parent
		 */
		public SrampOntology.Class getParent() {
			return parent;
		}

		/**
		 * @param parent the parent to set
		 */
		public void setParent(SrampOntology.Class parent) {
			this.parent = parent;
		}

		/**
		 * @return the children
		 */
		public List<SrampOntology.Class> getChildren() {
			return children;
		}

		/**
		 * @param children the children to set
		 */
		public void setChildren(List<SrampOntology.Class> children) {
			this.children = children;
		}

		/**
		 * @return the uri
		 */
		public URI getUri() {
			return uri;
		}

		/**
		 * @param uri the uri to set
		 */
		public void setUri(URI uri) {
			this.uri = uri;
		}

		/**
		 * Normalize the hierarchy into a list of URIs.  The returned list of
		 * URIs will contain this class and all ancestors.
		 */
		public Set<URI> normalize() {
			Set<URI> uris = new HashSet<URI>();
			SrampOntology.Class current = this;
			while (current != null) {
				uris.add(current.getUri());
				current = current.getParent();
			}
			return uris;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}
	}
}
