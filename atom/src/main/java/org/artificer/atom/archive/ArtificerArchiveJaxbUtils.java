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
package org.artificer.atom.archive;

import java.io.File;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.artificer.atom.ArtificerAtomUtils;

/**
 * Utility methods for using jaxb.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerArchiveJaxbUtils {

	private static JAXBContext jaxbContext;
	private static JAXBContext getJaxbContext() throws JAXBException {
		if (jaxbContext == null) {
			jaxbContext = JAXBContext.newInstance(Entry.class, Artifact.class);
		}
		return jaxbContext;
	}

	/**
	 * Reads the meta-data (*.atom) file and returns a JAXB object.
	 * @param metaDataFile
	 * @throws JAXBException
	 */
	public static BaseArtifactType readMetaData(File metaDataFile) throws JAXBException {
		try {
			Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
			Entry entry = (Entry) unmarshaller.unmarshal(metaDataFile);
			return ArtificerAtomUtils.unwrapSrampArtifact(entry);
		} catch (JAXBException e) {
			throw e;
		} catch (Throwable t) {
			throw new JAXBException(t);
		}
	}

	/**
	 * Writes the artifact meta-data to the given working path.
	 * @param outputFile
	 * @param artifact
	 * @throws JAXBException
	 */
	public static void writeMetaData(File outputFile, BaseArtifactType artifact) throws JAXBException {
		writeMetaData(outputFile, artifact, true);
	}

	/**
	 * Writes the artifact meta-data to the given working path.
	 * @param outputFile
	 * @param artifact
	 * @param wrap
	 * @throws JAXBException
	 */
	public static void writeMetaData(File outputFile, BaseArtifactType artifact, boolean wrap) throws JAXBException {
		try {
			Marshaller marshaller = getJaxbContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			Entry atomEntry = ArtificerAtomUtils.wrapSrampArtifact(artifact);
			if (wrap) {
				marshaller.marshal(atomEntry, outputFile);
			} else {
				marshaller.marshal(atomEntry.getAnyOtherJAXBObject(), outputFile);
			}
		} catch (JAXBException e) {
			throw e;
		} catch (Exception e) {
			throw new JAXBException(e);
		}
	}

	/**
	 * Writes the artifact meta-data to the given output stream.
	 * @param outputStream
	 * @param artifact
	 * @throws JAXBException
	 */
	public static void writeMetaData(OutputStream outputStream, BaseArtifactType artifact) throws JAXBException {
		try {
			Entry atomEntry = ArtificerAtomUtils.wrapSrampArtifact(artifact);
			Marshaller marshaller = getJaxbContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(atomEntry, outputStream);
		} catch (JAXBException e) {
			throw e;
		} catch (Exception e) {
			throw new JAXBException(e);
		}
	}

}
