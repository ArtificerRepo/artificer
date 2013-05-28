/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.repository.jcr.modeshape.deriver;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.derived.AbstractXmlDeriver;
import org.overlord.sramp.common.derived.LinkerContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author eric.wittmann@redhat.com
 */
public class ExtendedArtifactDeriverTestDeriver extends AbstractXmlDeriver {

    /**
     * Constructor.
     */
    public ExtendedArtifactDeriverTestDeriver() {
    }

    /**
     * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#derive(java.util.Collection, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)
     */
    @Override
    protected void derive(Collection<BaseArtifactType> derivedArtifacts, BaseArtifactType artifact,
            Element rootElement, XPath xpath) throws IOException {
        try {
            processRoot(derivedArtifacts, artifact, rootElement, xpath);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * @see org.overlord.sramp.common.derived.ArtifactDeriver#link(org.overlord.sramp.common.derived.LinkerContext, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.util.Collection)
     */
    @Override
    public void link(LinkerContext context, BaseArtifactType sourceArtifact,
            Collection<BaseArtifactType> derivedArtifacts) {
        // Links are created during derive() - this is OK because this test deriver
        // doesn't need to look for references outside of itself
    }

    /**
     * @param derivedArtifacts
     * @param artifact
     * @param rootElement
     * @param xpath
     * @throws XPathExpressionException
     */
    private void processRoot(Collection<BaseArtifactType> derivedArtifacts, BaseArtifactType artifact,
            Element rootElement, XPath xpath) throws XPathExpressionException {
        NodeList nodes = (NodeList) this.query(xpath, rootElement, "./credit", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType credit = new ExtendedArtifactType();
            credit.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            credit.setUuid(UUID.randomUUID().toString());

            String type = node.getAttribute("type");
            String name = node.getAttribute("name");
            String part = node.getAttribute("part");
            String year = node.getAttribute("year");

            credit.setExtendedType(StringUtils.capitalize(type) + "Credit");
            credit.setName(name);

            SrampModelUtils.setCustomProperty(credit, "part", part);
            SrampModelUtils.setCustomProperty(credit, "year", year);

            derivedArtifacts.add(credit);

            // Set a relationship from original artifact to the listener
            credit.setUuid(UUID.randomUUID().toString());
            SrampModelUtils.addGenericRelationship(artifact, "hasCredit", credit.getUuid());
        }
    }

}
