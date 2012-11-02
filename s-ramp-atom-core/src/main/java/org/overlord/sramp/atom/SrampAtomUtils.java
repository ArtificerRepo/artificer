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
package org.overlord.sramp.atom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.overlord.sramp.ArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Some useful static utils for users of the s-ramp client.
 *
 * @author eric.wittmann@redhat.com
 */
public final class SrampAtomUtils {

	/**
	 * Private constructor.
	 */
	private SrampAtomUtils() {
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
			return unwrapSrampArtifact(artifactType, artifact);
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
			entry.setId(new URI(artifact.getUuid()));
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
		Method method = Artifact.class.getMethod("set" + artifact.getClass().getSimpleName(), artifact.getClass());
		method.invoke(srampArty, artifact);
		entry.setAnyOtherJAXBObject(srampArty);

		return entry;
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
	public static ArtifactType getArtifactType(Entry entry) {
		// Try the Category
		List<Category> categories = entry.getCategories();
		for (Category cat : categories) {
			if ("x-s-ramp:2010:type".equals(cat.getScheme().toString())) {
				String atype = cat.getTerm();
				return ArtifactType.valueOf(atype);
			}
		}

		// Check the 'self' link
		Link link = entry.getLinkByRel("self");
		if (link != null) {
			URI href = link.getHref();
			String path = href.getPath();
			String [] split = path.split("/");
			String atype = split[split.length - 2];
			//String amodel = split[split.length - 3];
			return ArtifactType.valueOf(atype);
		}

		// Finally, try to figure it out by introspecting the 'artifact' extension element
		try {
			Artifact artifactWrapper = getArtifactWrapper(entry);
			if (artifactWrapper != null) {
				return ArtifactType.valueOf(artifactWrapper);
			}
		} catch (JAXBException e) {
		}

		// If all else fails!
		return ArtifactType.valueOf("Document");
	}

	/**
	 * Unwrap some other jaxb object from its Atom Entry wrapper.
	 * @param entry
	 * @param clazz
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unwrap(Entry entry, Class<T> clazz) throws JAXBException {
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
}
