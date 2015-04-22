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
package org.artificer.repository.test;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactTypeEnum;
import org.artificer.repository.PersistenceManager.BatchItem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Tests adding multiple artifacts at the same time via the persistence manager's batch
 * functionality.
 *
 * @author eric.wittmann@redhat.com
 */
public class BatchPersistenceTest extends AbstractNoAuditingPersistenceTest {

	@Test
	public void testSimpleBatch() throws Exception {
	    List<BatchItem> items = new ArrayList<BatchItem>();
	    BatchItem item = createBatchItem("/sample-files/batch/simple-1.txt", new Document(), BaseArtifactEnum.DOCUMENT);
	    items.add(item);
	    item = createBatchItem("/sample-files/batch/simple-2.txt", new Document(), BaseArtifactEnum.DOCUMENT);
        items.add(item);

        List<Object> response = persistenceManager.persistBatch(items);
        for (Object object : response) {
            if (object instanceof Exception) {
                ((Exception) object).printStackTrace();
            }
            Assert.assertTrue(object instanceof BaseArtifactType);
        }

        BaseArtifactType simple1 = (BaseArtifactType) response.get(0);
        BaseArtifactType simple2 = (BaseArtifactType) response.get(1);

		Assert.assertEquals("simple-1.txt", simple1.getName());
        Assert.assertEquals("simple-2.txt", simple2.getName());
	}

    @Test
    public void testWsdlBatch() throws Exception {
        List<BatchItem> items = new ArrayList<BatchItem>();
        BatchItem item = createBatchItem("/sample-files/wsdl/jcr-sample-externalrefs.xsd", new XsdDocument(), BaseArtifactEnum.XSD_DOCUMENT);
        items.add(item);
        item = createBatchItem("/sample-files/wsdl/jcr-sample-externalrefs.wsdl", new WsdlDocument(), BaseArtifactEnum.WSDL_DOCUMENT);
        items.add(item);

        List<Object> response = persistenceManager.persistBatch(items);
        for (Object object : response) {
            Assert.assertTrue(object instanceof BaseArtifactType);
        }

        XsdDocument xsd = (XsdDocument) response.get(0);
        Assert.assertNotNull(xsd);
        Assert.assertEquals("jcr-sample-externalrefs.xsd", xsd.getName());
        Assert.assertEquals("urn:s-ramp:test:jcr-sample-externalrefs:types", xsd.getTargetNamespace());

        WsdlDocument wsdl = (WsdlDocument) response.get(1);
        Assert.assertNotNull(wsdl);
        Assert.assertEquals("jcr-sample-externalrefs.wsdl", wsdl.getName());
        Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", wsdl.getTargetNamespace());

        ElementDeclaration extInput = (ElementDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.ElementDeclaration, "extInput");
        ComplexTypeDeclaration extOutputType = (ComplexTypeDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.ComplexTypeDeclaration, "extOutputType");
        SimpleTypeDeclaration extSimpleType = (SimpleTypeDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.SimpleTypeDeclaration, "extSimpleType");
        Message findRequestMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findRequest");
        Message findResponseMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findResponse");
        Message findRequestSimpleMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findRequestSimple");

        // findRequestMessage assertions
        Part part = (Part) getArtifactByTarget(findRequestMessage.getPart().get(0));
        Assert.assertNull(part.getType());
        ElementDeclaration elem = (ElementDeclaration) getArtifactByTarget(part.getElement());
        Assert.assertEquals(extInput.getUuid(), elem.getUuid());
        // findResponseMessage assertions
        part = (Part) getArtifactByTarget(findResponseMessage.getPart().get(0));
        Assert.assertNull(part.getElement());
        ComplexTypeDeclaration complexType = (ComplexTypeDeclaration) getArtifactByTarget(part.getType());
        Assert.assertEquals(extOutputType.getUuid(), complexType.getUuid());
        // findRequestSimpleMessage assertions
        part = (Part) getArtifactByTarget(findRequestSimpleMessage.getPart().get(0));
        Assert.assertNull(part.getElement());
        SimpleTypeDeclaration type = (SimpleTypeDeclaration) getArtifactByTarget(part.getType());
        Assert.assertEquals(extSimpleType.getUuid(), type.getUuid());
    }
    
    /**
     * Creates a batch item for the given file.
     * @param filePath
     * @param document
     * @param type
     * @throws org.artificer.common.ArtificerException
     */
    private BatchItem createBatchItem(String filePath, DocumentArtifactType document, BaseArtifactEnum type) throws Exception {
        String artifactFileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        InputStream contentStream = this.getClass().getResourceAsStream(filePath);

        document.setArtifactType(type);
        document.setName(artifactFileName);

        return new BatchItem(filePath, document, new ArtifactContent(artifactFileName, contentStream));
    }


}
