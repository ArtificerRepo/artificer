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
package org.artificer.common.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
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
public class ArtificerOntology implements Serializable {

	private String uuid;
	private String label;
	private String comment;
	private String base;
	private String id;
	private String createdBy;
	private Date createdOn;
	private String lastModifiedBy;
	private Date lastModifiedOn;
	private List<ArtificerOntologyClass> rootClasses = new ArrayList<ArtificerOntologyClass>();
	private Map<URI, ArtificerOntologyClass> classIndexByUri = new HashMap<URI, ArtificerOntologyClass>();
	private Map<String, ArtificerOntologyClass> classIndexById = new HashMap<String, ArtificerOntologyClass>();

	/**
	 * Constructor.
	 */
	public ArtificerOntology() {
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
	public List<ArtificerOntologyClass> getRootClasses() {
		return rootClasses;
	}

	/**
	 * @return a list of all classes in the ontology
	 */
	public List<ArtificerOntologyClass> getAllClasses() {
		List<ArtificerOntologyClass> allClasses = new ArrayList<ArtificerOntologyClass>();
		addAllClasses(allClasses, getRootClasses());
		return allClasses;
	}

	/**
	 * Adds all classes recursively to the list of classes.
	 * @param allClasses
	 * @param classes
	 */
	private void addAllClasses(List<ArtificerOntologyClass> allClasses, List<ArtificerOntologyClass> classes) {
		allClasses.addAll(classes);
		for (ArtificerOntologyClass c : classes) {
			addAllClasses(allClasses, c.getChildren());
		}
	}

	/**
	 * @param rootClasses the rootClasses to set
	 */
	public void setRootClasses(List<ArtificerOntologyClass> rootClasses) {
		this.rootClasses = rootClasses;
	}

	/**
	 * Creates a new class within this ontology.
	 * @param id
	 */
	public ArtificerOntologyClass createClass(String id) {
		ArtificerOntologyClass c = new ArtificerOntologyClass();
		c.setId(id);
		String uri = this.getBase() + "#" + id; //$NON-NLS-1$
		try {
			c.setUri(new URI(uri));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return c;
	}

	/**
	 * Finds a class by its unique id within the ontology.
	 * @param id
	 */
	public synchronized ArtificerOntologyClass findClass(String id) {
		if (classIndexById.containsKey(id)) {
			return classIndexById.get(id);
		} else {
			ArtificerOntologyClass found = null;
			for (ArtificerOntologyClass candidate : rootClasses) {
				found = candidate.findClass(id);
				if (found != null) {
					break;
				}
			}
			if (found != null) {
				classIndexById.put(id, found);
			}
			return found;
		}
	}

	/**
	 * Finds a class by its unique URI.
	 * @param uri
	 */
	public synchronized ArtificerOntologyClass findClass(URI uri) {
		if (classIndexByUri.containsKey(uri)) {
			return classIndexByUri.get(uri);
		} else {
			ArtificerOntologyClass found = null;
			for (ArtificerOntologyClass candidate : rootClasses) {
				found = candidate.findClass(uri);
				if (found != null) {
					break;
				}
			}
			if (found != null) {
				classIndexByUri.put(uri, found);
			}
			return found;
		}
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the createdOn
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @return the lastModifiedBy
	 */
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	/**
	 * @param lastModifiedBy the lastModifiedBy to set
	 */
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	/**
	 * @return the lastModifiedOn
	 */
	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}

	/**
	 * @param lastModifiedOn the lastModifiedOn to set
	 */
	public void setLastModifiedOn(Date lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	/**
	 * Models a single class in an s-ramp ontology.
	 *
	 * @author eric.wittmann@redhat.com
	 */
	public static class ArtificerOntologyClass implements Serializable {

		private String id;
		private String label;
		private String comment;
		private URI uri;
        @JsonIgnore // MUST ignore to prevent stack overflow during marshalling
		private ArtificerOntologyClass parent;
		private List<ArtificerOntologyClass> children = new ArrayList<ArtificerOntologyClass>();

		/**
		 * Constructor.
		 */
		public ArtificerOntologyClass() {
		}

		/**
		 * Recursively finds a class matching the given ID.
		 * @param id
		 */
		public ArtificerOntologyClass findClass(String id) {
			if (this.id.equals(id)) {
				return this;
			} else {
				for (ArtificerOntologyClass c : this.children) {
					ArtificerOntologyClass found = c.findClass(id);
					if (found != null) {
						return found;
					}
				}
			}
			return null;
		}

		/**
		 * Recursively finds a class matching the given URI.
		 * @param uri
		 */
		public ArtificerOntologyClass findClass(URI uri) {
			if (this.uri.equals(uri)) {
				return this;
			} else {
				for (ArtificerOntologyClass c : this.children) {
					ArtificerOntologyClass found = c.findClass(uri);
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
		public ArtificerOntologyClass getParent() {
			return parent;
		}

		/**
		 * @param parent the parent to set
		 */
		public void setParent(ArtificerOntologyClass parent) {
			this.parent = parent;
		}

		/**
		 * @return the children
		 */
		public List<ArtificerOntologyClass> getChildren() {
			return children;
		}

		/**
		 * @param children the children to set
		 */
		public void setChildren(List<ArtificerOntologyClass> children) {
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
			ArtificerOntologyClass current = this;
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
