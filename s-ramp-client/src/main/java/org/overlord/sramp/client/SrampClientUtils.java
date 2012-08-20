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
package org.overlord.sramp.client;

import java.net.URI;

import javax.xml.bind.JAXBException;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.overlord.sramp.ArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Some useful static utils for users of the s-ramp client.
 *
 * @author eric.wittmann@redhat.com
 */
public final class SrampClientUtils {

	/**
	 * Private constructor.
	 */
	private SrampClientUtils() {
	}
	
	/**
	 * Unwraps the specific {@link BaseArtifactType} from the S-RAMP Artifact wrapper
	 * element.  This method requires the artifact's type.
	 * @param artifactType the s-ramp artifact type
	 * @param artifact the s-ramp wrapper {@link Artifact}
	 * @return a {@link BaseArtifactType}
	 */
	public static BaseArtifactType unwrapSrampArtifact(String artifactType, Artifact artifact) {
		return unwrapSrampArtifact(ArtifactType.valueOf(artifactType), artifact);
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
	 * @param artifactType the s-ramp artifact type
	 * @param entry an Atom {@link Entry}
	 * @return a {@link BaseArtifactType}
	 * @throws JAXBException 
	 */
	public static BaseArtifactType unwrapSrampArtifact(String artifactType, Entry entry) throws JAXBException {
		return unwrapSrampArtifact(ArtifactType.valueOf(artifactType), entry);
	}

	/**
	 * Unwraps a specific {@link BaseArtifactType} from the Atom {@link Entry} containing it.  This
	 * method grabs the {@link Artifact} child from the Atom {@link Entry} and then unwraps the
	 * {@link BaseArtifactType} from that.
	 * @param artifactType the s-ramp artifact type
	 * @param entry an Atom {@link Entry}
	 * @return a {@link BaseArtifactType}
	 * @throws JAXBException 
	 */
	public static BaseArtifactType unwrapSrampArtifact(ArtifactType artifactType, Entry entry) throws JAXBException {
		Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
		return unwrapSrampArtifact(artifactType, artifact);
	}

	/**
	 * Figures out the S-RAMP artifact model for the given {@link Entry}.
	 * @param entry
	 */
	public static String getArtifactModel(Entry entry) {
		Link link = entry.getLinkByRel("self");
		URI href = link.getHref();
		String path = href.getPath();
		String [] split = path.split("/");
		return split[split.length - 3];
	}

	/**
	 * Figures out the S-RAMP artifact type for the given {@link Entry}.
	 * @param entry
	 */
	public static String getArtifactType(Entry entry) {
		Link link = entry.getLinkByRel("self");
		URI href = link.getHref();
		String path = href.getPath();
		String [] split = path.split("/");
		return split[split.length - 2];
	}

}
