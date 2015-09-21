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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Models an s-ramp ontology.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "Ontology")
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
	private final Map<URI, ArtificerOntologyClass> classIndexByUri = new HashMap<URI, ArtificerOntologyClass>();
	private final Map<String, ArtificerOntologyClass> classIndexById = new HashMap<String, ArtificerOntologyClass>();
	private long surrogateId;

	@Id
	@GeneratedValue
	public long getSurrogateId() {
		return surrogateId;
	}

	public void setSurrogateId(long surrogateId) {
		this.surrogateId = surrogateId;
	}

	// Note: Cannot be @Id!  Not guaranteed to be set by clients.
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
	 * @return the uuid
	 */
	@Column(columnDefinition = "char(36)")
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
	@Lob
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

	@OneToMany(mappedBy = "root", orphanRemoval = true, cascade = CascadeType.ALL)
	public List<ArtificerOntologyClass> getRootClasses() {
		return rootClasses;
	}

	/**
	 * @param rootClasses the rootClasses to set
	 */
	public void setRootClasses(List<ArtificerOntologyClass> rootClasses) {
		this.rootClasses = rootClasses;
	}

	@Transient
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
	 * Creates a new class within this ontology.
	 * @param id
	 */
	public ArtificerOntologyClass createClass(String id) {
		ArtificerOntologyClass c = new ArtificerOntologyClass();
		c.setId(id);
		String uri = this.getBase() + "#" + id;
		c.setUri(uri);
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
	@Temporal(TemporalType.DATE)
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
	@Temporal(TemporalType.DATE)
	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}

	/**
	 * @param lastModifiedOn the lastModifiedOn to set
	 */
	public void setLastModifiedOn(Date lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}
}
