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
package org.artificer.repository.test.artifactbuilder;

import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.integration.artifactbuilder.XmlArtifactBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.io.IOException;
import java.util.UUID;

/**
 * @author eric.wittmann@redhat.com
 */
public class ExtendedArtifactTestArtifactBuilder extends XmlArtifactBuilder {

    @Override
    protected void derive() throws IOException {
        try {
            NodeList nodes = (NodeList) query(rootElement, "./credit", XPathConstants.NODESET);
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

                ArtificerModelUtils.setCustomProperty(credit, "part", part);
                ArtificerModelUtils.setCustomProperty(credit, "year", year);

                getDerivedArtifacts().add(credit);

                // Set a relationship from original artifact to the listener
                credit.setUuid(UUID.randomUUID().toString());
                ArtificerModelUtils.addGenericRelationship(getPrimaryArtifact(), "hasCredit", credit.getUuid());
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
