/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository.jcr;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;

import java.io.InputStream;


/**
 * @author eric.wittmann@redhat.com
 */
public class ExtendedArtifactDeriverTest extends AbstractNoAuditingJCRPersistenceTest {

    @Test
    public void testExtendedArtifactDeriver() throws Exception {
        String artifactFileName = "gtgjrdih.xml";
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName);
        ExtendedDocument artifact = new ExtendedDocument();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_DOCUMENT);
        artifact.setExtendedType("ExtendedArtifactDeriverTestDocument");
        artifact.setName("jrd");

        BaseArtifactType pa = persistenceManager.persistArtifact(artifact, new ArtifactContent(artifactFileName, pdf));

        Assert.assertNotNull(pa);
        log.info("persisted gtgjrdih.xml to JCR, returned artifact uuid=" + pa.getUuid());

        Assert.assertEquals(ExtendedDocument.class, artifact.getClass());

        // Four derived ActingCredit artifacts should have been created.
        SrampQuery query = queryManager.createQuery("/s-ramp/ext/ActingCredit");
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertEquals(4, artifactSet.size());

        // Four total derived artifacts should have been created.
        query = queryManager.createQuery("/s-ramp[@derived='true']");
        artifactSet = query.executeQuery();
        Assert.assertEquals(4, artifactSet.size());

        // Also there are four derived arifacts that are related to the original
        query = queryManager.createQuery("/s-ramp/ext[relatedDocument[@name='jrd']]");
        artifactSet = query.executeQuery();
        Assert.assertEquals(4, artifactSet.size());

        // But only one named 'Rising Storm'
        query = queryManager.createQuery("/s-ramp/ext[relatedDocument[@name='jrd'] and @name='Rising Storm']");
        artifactSet = query.executeQuery();
        Assert.assertEquals(1, artifactSet.size());

        // Find the original artifact by one of the credits inside it
        query = queryManager.createQuery("/s-ramp/ext[hasCredit[@name='Rising Storm']]");
        artifactSet = query.executeQuery();
        Assert.assertEquals(1, artifactSet.size());

        // But don't find it when looking for a credit that's not there
        query = queryManager.createQuery("/s-ramp/ext[hasCredit[@name='Army of Darkness']]");
        artifactSet = query.executeQuery();
        Assert.assertEquals(0, artifactSet.size());
    }
}
