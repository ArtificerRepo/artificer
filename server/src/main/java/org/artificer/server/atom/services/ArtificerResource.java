/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.server.atom.services;

import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.atom.visitors.ArtifactToSummaryAtomEntryVisitor;
import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.MediaType;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.common.query.RelationshipType;
import org.artificer.common.query.ReverseRelationship;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Person;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * An JAX-RS resource with Artificer-specific capabilities, outside of the S-RAMP spec.
 *
 * @author Brett Meyer.
 */
@Path("/artificer")
public class ArtificerResource extends AbstractFeedResource {

    @GET
    @Path("reverseRelationship/{uuid}")
    @Produces(MediaType.APPLICATION_ATOM_XML_FEED)
    public Feed reverseRelationship(
            @Context HttpServletRequest request,
            @PathParam("uuid") String uuid) throws Exception {
        String baseUrl = ArtificerConfig.getBaseUrl(request.getRequestURL().toString());
        Feed feed = new Feed();
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_PROVIDER_QNAME, "Artificer");
        feed.setId(new URI("urn:uuid:" + UUID.randomUUID().toString()));
        feed.setTitle("Artificer"); //$NON-NLS-1$
        feed.setSubtitle("Reverse Relationships"); //$NON-NLS-1$
        feed.setUpdated(new Date());
        feed.getAuthors().add(new Person("anonymous")); //$NON-NLS-1$

        ArtifactToSummaryAtomEntryVisitor visitor = new ArtifactToSummaryAtomEntryVisitor(baseUrl, null);
        List<ReverseRelationship> relationships = queryService.reverseRelationships(uuid);
        for (ReverseRelationship relationship : relationships) {
            ArtifactSummary artifact = relationship.getSourceArtifact();
            Entry entry = ArtificerAtomUtils.wrapArtifactSummary(artifact);
            // Set the relationship metadata
            entry.getExtensionAttributes().put(ArtificerConstants.ARTIFICER_RELATIONSHIP_TYPE_QNAME,
                    relationship.getName());
            entry.getExtensionAttributes().put(ArtificerConstants.ARTIFICER_RELATIONSHIP_GENERIC_QNAME,
                    String.valueOf(relationship.getType().equals(RelationshipType.GENERIC)));
            feed.getEntries().add(entry);
            visitor.reset();
        }

        return feed;
    }
}
