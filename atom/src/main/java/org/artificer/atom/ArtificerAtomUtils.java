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
package org.artificer.atom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.artificer.common.MediaType;
import org.jboss.downloads.artificer._2013.auditing.AuditEntry;
import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.jboss.resteasy.plugins.providers.atom.Source;
import org.jboss.resteasy.plugins.providers.jaxb.JAXBContextFinder;
import org.jboss.resteasy.plugins.providers.jaxb.XmlJAXBContextFinder;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQueryData;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.ontology.ArtificerOntology;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Some useful static utils for users of the s-ramp client.
 *
 * @author eric.wittmann@redhat.com
 */
public final class ArtificerAtomUtils {

    private static final XmlJAXBContextFinder finder = new XmlJAXBContextFinder();
    
	/**
	 * Private constructor.
	 */
	private ArtificerAtomUtils() {
	}

	/**
	 * Unwraps the specific {@link BaseArtifactType} from the S-RAMP Artifact wrapper
	 * element.  This method requires the artifact's type.
	 * @param artifactType the s-ramp artifact type
	 * @param artifact the s-ramp wrapper {@link Artifact}
	 * @return a {@link BaseArtifactType}
	 */
	public static BaseArtifactType unwrapSrampArtifact(ArtifactType artifactType, Artifact artifact) {
		return artifactType.unwrap(artifact);
	}

	/**
	 * Unwraps a specific {@link BaseArtifactType} from the Atom {@link Entry} containing it.  This
	 * method grabs the {@link Artifact} child from the Atom {@link Entry} and then unwraps the
	 * {@link BaseArtifactType} from that.
	 * @param entry an Atom {@link Entry}
	 * @return a {@link BaseArtifactType}
	 */
	public static BaseArtifactType unwrapSrampArtifact(Entry entry) {
		ArtifactType artifactType = getArtifactType(entry);
		return unwrapSrampArtifact(artifactType, entry);
	}

	/**
	 * Unwraps a specific {@link BaseArtifactType} from the Atom {@link Entry} containing it.  This
	 * method grabs the {@link Artifact} child from the Atom {@link Entry} and then unwraps the
	 * {@link BaseArtifactType} from that.
	 * @param artifactType the s-ramp artifact type
	 * @param entry an Atom {@link Entry}
	 * @return a {@link BaseArtifactType}
	 */
	public static BaseArtifactType unwrapSrampArtifact(ArtifactType artifactType, Entry entry) {
		try {
			Artifact artifact = getArtifactWrapper(entry);
			if (artifact != null) {
			    // Don't trust the extended types - there is some ambiguity there.
			    if (artifactType.isExtendedType()) {
			        artifactType = disambiguateExtendedType(entry, artifactType);
			    }
		        return unwrapSrampArtifact(artifactType, artifact);
			} else {
                return null;
			}
		} catch (JAXBException e) {
			// This is unlikely to happen.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the {@link Artifact} jaxb object from the {@link Entry}.
	 * @param entry
	 * @throws JAXBException
	 */
	private static Artifact getArtifactWrapper(Entry entry) throws JAXBException {
		return unwrap(entry, Artifact.class);
	}

	/**
	 * Wraps the given s-ramp artifact in an Atom {@link Entry}.
	 * @param artifact
	 * @throws URISyntaxException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static Entry wrapSrampArtifact(BaseArtifactType artifact) throws URISyntaxException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException,
			NoSuchMethodException {
		// TODO leverage the artifact->entry visitors here
		Entry entry = new Entry();
		if (artifact.getUuid() != null)
			entry.setId(new URI("urn:uuid:" + artifact.getUuid())); //$NON-NLS-1$
		if (artifact.getLastModifiedTimestamp() != null)
			entry.setUpdated(artifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
		if (artifact.getName() != null)
			entry.setTitle(artifact.getName());
		if (artifact.getCreatedTimestamp() != null)
			entry.setPublished(artifact.getCreatedTimestamp().toGregorianCalendar().getTime());
		if (artifact.getCreatedBy() != null)
			entry.getAuthors().add(new Person(artifact.getCreatedBy()));
		if (artifact.getDescription() != null)
			entry.setSummary(artifact.getDescription());

		Artifact srampArty = new Artifact();
		Method method = Artifact.class.getMethod("set" + artifact.getClass().getSimpleName(), artifact.getClass()); //$NON-NLS-1$
		method.invoke(srampArty, artifact);
		entry.setAnyOtherJAXBObject(srampArty);

		return entry;
	}
    
    public static Entry wrapStoredQuery(StoredQuery storedQuery) throws Exception {
        Entry entry = new Entry();
        entry.setId(new URI("urn:uuid:" + storedQuery.getQueryName())); //$NON-NLS-1$
        entry.setTitle("Stored Query: " + storedQuery.getQueryName()); //$NON-NLS-1$
        
        // TODO: This is really stupid.  Going to push back on this w/ the TC.  The Atom binding spec should simply
        // reuse the core model's StoredQuery.
        StoredQueryData data = new StoredQueryData();
        data.setQueryName(storedQuery.getQueryName());
        data.setQueryString(storedQuery.getQueryExpression());
        data.getPropertyName().addAll(storedQuery.getPropertyName());
        entry.setAnyOtherJAXBObject(data);
        
        Content content = new Content();
        content.setText("Stored Query Entry"); //$NON-NLS-1$
        entry.setContent(content);
        
        Category category = new Category();
        category.setTerm("query"); //$NON-NLS-1$
        category.setLabel("Stored Query Entry"); //$NON-NLS-1$
        category.setScheme(ArtificerAtomConstants.X_S_RAMP_TYPE_URN);
        entry.getCategories().add(category);
        
        return entry;
    }
    
    public static Entry wrapOntology(ArtificerOntology ontology, RDF rdf) throws Exception {
        Entry entry = new Entry();
        entry.setId(new URI("urn:uuid:" + ontology.getUuid())); //$NON-NLS-1$
        entry.setPublished(ontology.getCreatedOn());
        entry.setUpdated(ontology.getLastModifiedOn());
        entry.getAuthors().add(new Person(ontology.getCreatedBy()));
        entry.setTitle(ontology.getLabel());
        entry.setSummary(ontology.getComment());
        Source source = new Source();
        source.setBase(new URI(ontology.getBase()));
        source.setId(new URI(ontology.getId()));
        entry.setSource(source);

        if (rdf != null) {
            entry.setAnyOtherJAXBObject(rdf);
        }
        
        Content content = new Content();
        content.setText("Classification Entry"); //$NON-NLS-1$
        entry.setContent(content);
        
        Category category = new Category();
        category.setTerm("classification"); //$NON-NLS-1$
        category.setLabel("Classification Entry"); //$NON-NLS-1$
        category.setScheme(ArtificerAtomConstants.X_S_RAMP_TYPE_URN);
        entry.getCategories().add(category);
        
        return entry;
    }

	/**
     * Figures out the S-RAMP artifact type for the given {@link Entry}.
	 *
	 * @param entry
	 */
	public static ArtifactType getArtifactType(Entry entry) {
		ArtifactType type = getArtifactTypeFromEntry(entry);
		if (type.isExtendedType()) {
            boolean derived = "true".equals(entry.getExtensionAttributes().get(ArtificerConstants.SRAMP_DERIVED_QNAME)); //$NON-NLS-1$
            String extendedType = (String) entry.getExtensionAttributes().get(ArtificerConstants.SRAMP_EXTENDED_TYPE_QNAME);
            type.setExtendedDerivedType(derived);
            type.setExtendedType(extendedType);
		}
        return type;
	}

    /**
     * Figures out the S-RAMP artifact type for the given {@link Entry}.  There are a number of
     * ways we can do this.  We'll try them all:
     *
     * 1) check the 'self' link, parsing it for the type and model information
     * 2) check the Atom Category - there should be one for the artifact type
     * 3) unwrap the Entry's {@link Artifact} and get the artifactType value (xml attribute)
     *
     * @param entry
     */
    protected static ArtifactType getArtifactTypeFromEntry(Entry entry) {
        // Take a look at what's in the JAXB artifact wrapper (if one exists).
        try {
            Artifact artifactWrapper = getArtifactWrapper(entry);
            if (artifactWrapper != null) {
                String hint = null;
                Element wrapperNode = getArtifactWrapperNode(entry);
                if (wrapperNode != null) {
                    hint = getArtifactWrappedElementName(wrapperNode);
                }
                return ArtifactType.valueOf(artifactWrapper, hint);
            }
        } catch (JAXBException e) {
        }

        // Try the Category
		List<Category> categories = entry.getCategories();
		for (Category cat : categories) {
			if (ArtificerAtomConstants.X_S_RAMP_TYPE.equals(cat.getScheme().toString())) {
				String atype = cat.getTerm();
				ArtifactType artifactType = ArtifactType.valueOf(atype);
		        if (artifactType.isExtendedType()) {
		            artifactType = disambiguateExtendedType(entry, artifactType);
		        }
                return artifactType;
			}
		}

		// Check the 'self' link
		Link link = entry.getLinkByRel("self"); //$NON-NLS-1$
		if (link != null) {
			URI href = link.getHref();
			String path = href.getPath();
			String [] split = path.split("/"); //$NON-NLS-1$
			String atype = split[split.length - 2];
			//String amodel = split[split.length - 3];
			ArtifactType artifactType = ArtifactType.valueOf(atype);
            if (artifactType.isExtendedType()) {
                artifactType = disambiguateExtendedType(entry, artifactType);
            }
            return artifactType;
		}

		// If all else fails!
		return ArtifactType.valueOf("Document"); //$NON-NLS-1$
    }

    /**
     * Attempts to figure out whether we're dealing with an {@link ExtendedArtifactType} or a
     * {@link ExtendedDocument} by looking for clues in the {@link Entry}.
     * @param entry
     * @param artifactType
     */
    protected static ArtifactType disambiguateExtendedType(Entry entry, ArtifactType artifactType) {
        String et = artifactType.getExtendedType();
        boolean convertToDocument = false;
        // Dis-ambiguate the extended types (or try to)
        if (entry.getContent() != null) {
            convertToDocument = true;
        } else {
            Element node = getArtifactWrapperNode(entry);
            if (node != null) {
                String type = getArtifactWrappedElementName(node);
                if (ExtendedDocument.class.getSimpleName().equals(type)) {
                    convertToDocument = true;
                }
            }
        }

        if (convertToDocument) {
            artifactType = ArtifactType.valueOf(BaseArtifactEnum.EXTENDED_DOCUMENT);
            artifactType.setExtendedType(et);
        }
        return artifactType;
    }

	/**
	 * Unwrap some other jaxb object from its Atom Entry wrapper.
	 * @param entry
	 * @param clazz
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unwrap(Entry entry, Class<T> clazz) throws JAXBException {
	    setFinder(entry);
	    if (entry.getAnyOtherElement() == null && entry.getAnyOther().isEmpty())
	        return null;
		T object = entry.getAnyOtherJAXBObject(clazz);
		if (object == null) {
			for (Object anyOther : entry.getAnyOther()) {
				if (anyOther != null && anyOther.getClass().equals(clazz)) {
					object = (T) anyOther;
					break;
				}
			}
		}
		return object;
	}

	/**
	 * TODO: remove this once we fix RestEasy to set the finder automatically on the Entry during unmarshaling
     * @param entry
     */
    private static void setFinder(Entry entry) {
        // Eat any exception we might encounter - if this fails it'll just revert to creating
        // a new JAXBContext each time.  This is slow but works.
        try {
            Method method = entry.getClass().getDeclaredMethod("setFinder", JAXBContextFinder.class); //$NON-NLS-1$
            method.setAccessible(true);
            method.invoke(entry, finder);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (NoSuchMethodException e) {
        }
    }

    /**
	 * Gets the XML node for the wrapper 'artifact' element from the {@link Entry}.
	 * @param entry
	 */
	protected static Element getArtifactWrapperNode(Entry entry) {
	    Element element = entry.getAnyOtherElement();
	    if (isWrapperElement(element)) {
	        return element;
	    }
        for (Object anyOther : entry.getAnyOther()) {
            if (anyOther instanceof Element && isWrapperElement((Element) anyOther)) {
                return (Element) anyOther;
            }
        }
	    return null;
	}

	/**
	 * @param artifactWrapperElement
	 * @return the local name of the artifact that is wrapped by the {@link Artifact} wrapper
	 */
	protected static String getArtifactWrappedElementName(Element artifactWrapperElement) {
	    NodeList nodes = artifactWrapperElement.getChildNodes();
	    for (int i = 0; i < nodes.getLength(); i++) {
	        Node item = nodes.item(i);
	        if (item.getNodeType() == Node.ELEMENT_NODE) {
	            return item.getLocalName();
	        }
	    }
	    return null;
	}

    /**
     * @param element
     * @return true if the {@link Element} is the artifact wrapper element
     */
    private static boolean isWrapperElement(Element element) {
        if (element == null)
            return false;
        QName qname = new QName(element.getNamespaceURI(), element.getLocalName());
        return qname.equals(ArtificerConstants.S_RAMP_WRAPPER_ELEM);
    }

    /**
     * Unwraps the Ontology from the Atom Entry.
     * @param entry
     */
    public static RDF unwrapRDF(Entry entry) {
        try {
            return unwrap(entry, RDF.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unwraps the audit entry from the Atom Entry.
     * @param entry
     */
    public static AuditEntry unwrapAuditEntry(Entry entry) {
        try {
            return unwrap(entry, AuditEntry.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unwraps the stored query entry from the Atom Entry.
     * @param entry
     */
    public static StoredQuery unwrapStoredQuery(Entry entry) {
        try {
            // TODO: Again, this is stupid.  StoredQuery and StoredQueryData should be combined.
            StoredQueryData data = unwrap(entry, StoredQueryData.class);
            StoredQuery storedQuery = new StoredQuery();
            storedQuery.setQueryExpression(data.getQueryString());
            storedQuery.setQueryName(data.getQueryName());
            storedQuery.getPropertyName().addAll(data.getPropertyName());
            return storedQuery;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void setXmlContentType(BaseArtifactType artifact, String src, Entry entry) {
        if (artifact instanceof XmlDocument) {
            XmlDocument xmlDocument = (XmlDocument) artifact;
            Content content = new Content();
            // Required by spec.  #getContentEncoding guaranteed to be set by XmlArtifactBuilder
            content.setRawType(MediaType.APPLICATION_XML_TYPE + "; charset=" + xmlDocument.getContentEncoding());
            content.setSrc(URI.create(src));
            entry.setContent(content);
        }
    }

}
