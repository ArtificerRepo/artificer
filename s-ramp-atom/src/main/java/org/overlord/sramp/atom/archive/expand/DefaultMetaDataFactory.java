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
package org.overlord.sramp.atom.archive.expand;

import java.util.UUID;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.SrampModelUtils;

/**
 * Default implementation of a meta-data factory, providing a reasonable starting point
 * for custom extensions (though it can also be used as-is to provide a very S-RAMP strict
 * impl).
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultMetaDataFactory implements MetaDataFactory {

    public static final String PARENT_UUID = "parent.uuid"; //$NON-NLS-1$

    private ZipToSrampArchiveContext context;

	/**
	 * Constructor.
	 */
	public DefaultMetaDataFactory() {
	}

	/**
	 * @see org.overlord.sramp.atom.archive.expand.MetaDataFactory#setContext(org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveContext)
	 */
	@Override
	public void setContext(ZipToSrampArchiveContext context) {
	    this.context = context;
	}

	/**
	 * @see org.overlord.sramp.atom.archive.jar.client.jar.MetaDataFactory#createMetaData(org.overlord.sramp.atom.archive.jar.client.jar.DiscoveredArtifact)
	 */
	@Override
	public BaseArtifactType createMetaData(DiscoveredArtifact discoveredArtifact) {
		BaseArtifactType artifact = createArtifact(discoveredArtifact);
		addStandardMetaData(discoveredArtifact, artifact);
		return artifact;
	}

    /**
     * Adds some standard meta-data properties to the artifact instance.
     * @param discoveredArtifact
     * @param artifact
     */
    protected void addStandardMetaData(DiscoveredArtifact discoveredArtifact, BaseArtifactType artifact) {
        artifact.setUuid(UUID.randomUUID().toString());
		artifact.setName(discoveredArtifact.getName());
		SrampModelUtils.setCustomProperty(artifact, "batch.archive-path", discoveredArtifact.getArchivePath()); //$NON-NLS-1$
		if (this.context != null) {
    		String parentUUID = (String) this.context.get(PARENT_UUID);
    		if (parentUUID != null)
    		    SrampModelUtils.addGenericRelationship(artifact, "expandedFromDocument", parentUUID); //$NON-NLS-1$
		}
    }

    /**
     * Creates the artifact instance based on the type of the discovered artifact.
     * @param discoveredArtifact
     */
    protected BaseArtifactType createArtifact(DiscoveredArtifact discoveredArtifact) {
        BaseArtifactType metaData = null;
		String archivePath = discoveredArtifact.getArchivePath().toLowerCase();
		if (archivePath.endsWith(".xml")) { //$NON-NLS-1$
			metaData = new XmlDocument();
			metaData.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);
		} else if (archivePath.endsWith(".wsdl")) { //$NON-NLS-1$
			metaData = new WsdlDocument();
			metaData.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
		} else if (archivePath.endsWith(".xsd")) { //$NON-NLS-1$
			metaData = new XsdDocument();
			metaData.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
		} else if (archivePath.endsWith(".wspolicy")) { //$NON-NLS-1$
			metaData = new XsdDocument();
			metaData.setArtifactType(BaseArtifactEnum.POLICY_DOCUMENT);
		} else {
			metaData = new Document();
			metaData.setArtifactType(BaseArtifactEnum.DOCUMENT);
		}
        return metaData;
    }

}
