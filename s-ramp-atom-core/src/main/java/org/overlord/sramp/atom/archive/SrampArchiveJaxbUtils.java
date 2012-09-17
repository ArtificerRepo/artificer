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
package org.overlord.sramp.atom.archive;

import java.io.File;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Utility methods for using jaxb.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampArchiveJaxbUtils {

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
			return SrampAtomUtils.unwrapSrampArtifact(entry);
		} catch (JAXBException e) {
			throw e;
		} catch (Throwable t) {
			throw new JAXBException(t);
		}
	}

	/**
	 * Writes the artifact meta-data to the given working path.
	 * @param workPath
	 * @param artifact
	 * @throws JAXBException
	 */
	public static void writeMetaData(File workPath, BaseArtifactType artifact) throws JAXBException {
		try {
			Entry atomEntry = SrampAtomUtils.wrapSrampArtifact(artifact);
			Marshaller marshaller = getJaxbContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(atomEntry, workPath);
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
			Entry atomEntry = SrampAtomUtils.wrapSrampArtifact(artifact);
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
