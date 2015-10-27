package org.artificer.common.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Models a single class in an s-ramp ontology.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "OntologyClass")
public class ArtificerOntologyClass implements Serializable {

    private String id;
    private String label;
    private String annotation;
    private String uri;
    private ArtificerOntology root;
    private ArtificerOntologyClass parent;
    private List<ArtificerOntologyClass> children = new ArrayList<ArtificerOntologyClass>();
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
        if (this.uri.equals(uri.toString())) {
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
    public String getAnnotation() {
        return annotation;
    }

    /**
     * @param comment the comment to set
     */
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
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
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Normalize the hierarchy into a list of ArtificerOntologyClasses.  The returned list of
     * ArtificerOntologyClasses will contain this class and all ancestors.
     */
    public Set<URI> normalize() throws URISyntaxException {
        Set<URI> uris = new HashSet<>();
        ArtificerOntologyClass current = this;
        while (current != null) {
            uris.add(new URI(current.getUri()));
            current = current.getParent();
        }
        return uris;
    }
}
