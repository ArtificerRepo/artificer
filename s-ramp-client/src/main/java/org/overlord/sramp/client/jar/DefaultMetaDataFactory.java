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
package org.overlord.sramp.client.jar;

import java.util.UUID;

import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Document;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * Default implementation of a meta-data factory, providing a reasonable starting point
 * for custom extensions (though it can also be used as-is to provide a very S-RAMP strict
 * impl).
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultMetaDataFactory implements MetaDataFactory {

	/**
	 * Constructor.
	 */
	public DefaultMetaDataFactory() {
	}

	/**
	 * @see org.overlord.sramp.client.jar.MetaDataFactory#createMetaData(org.overlord.sramp.client.jar.DiscoveredArtifact)
	 */
	@Override
	public BaseArtifactType createMetaData(DiscoveredArtifact artifact) {
		BaseArtifactType metaData = null;
		String archivePath = artifact.getArchivePath().toLowerCase();
		if (archivePath.endsWith(".xml")) {
			metaData = new XmlDocument();
			metaData.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);
		} else if (artifact.getArchivePath().endsWith(".wsdl")) {
			metaData = new WsdlDocument();
			metaData.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
		} else if (artifact.getArchivePath().endsWith(".xsd")) {
			metaData = new XsdDocument();
			metaData.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
		} else if (artifact.getArchivePath().endsWith(".wspolicy")) {
			metaData = new XsdDocument();
			metaData.setArtifactType(BaseArtifactEnum.POLICY_DOCUMENT);
		} else {
			metaData = new Document();
			metaData.setArtifactType(BaseArtifactEnum.DOCUMENT);
		}
		metaData.setUuid(UUID.randomUUID().toString());
		metaData.setName(artifact.getName());
		return metaData;
	}

}
