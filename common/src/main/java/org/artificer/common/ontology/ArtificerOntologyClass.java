package org.artificer.common.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Models a single class in an s-ramp ontology.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
public class ArtificerOntologyClass implements Serializable {

    private String id;
    private String label;
    private String comment;
    private URI uri;
    private ArtificerOntology root;
    private ArtificerOntologyClass parent;
    private List<ArtificerOntologyClass> children = new ArrayList<ArtificerOntologyClass>();

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

    @ManyToOne
    @JsonIgnore // MUST ignore to prevent stack overflow during marshalling
    public ArtificerOntology getRoot() {
        return root;
    }

    public void setRoot(ArtificerOntology root) {
        this.root = root;
    }

    @ManyToOne
    @JsonIgnore // MUST ignore to prevent stack overflow during marshalling
    public ArtificerOntologyClass getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(ArtificerOntologyClass parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
    @Id
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
     * Normalize the hierarchy into a list of ArtificerOntologyClasses.  The returned list of
     * ArtificerOntologyClasses will contain this class and all ancestors.
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
}
