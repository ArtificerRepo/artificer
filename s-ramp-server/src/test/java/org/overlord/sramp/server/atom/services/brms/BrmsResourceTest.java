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
package org.overlord.sramp.server.atom.services.brms;

import static org.overlord.sramp.common.test.resteasy.TestPortProvider.generateURL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.multipart.MultipartConstants;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedOutput;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.services.brms.BrmsConstants;
import org.overlord.sramp.atom.services.brms.assets.Assets;
import org.overlord.sramp.atom.services.brms.packages.Packages;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.server.atom.services.AbstractNoAuditingResourceTest;

import test.org.overlord.sramp.server.TestUtils;


/**
 * Tests the BRMS API.
 *
 * @author kurt.stam@redhat.com
 */
public class BrmsResourceTest extends AbstractNoAuditingResourceTest {

	@BeforeClass
	public static void setUpBrms() throws Exception {
		dispatcher.getRegistry().addPerRequestResource(BrmsResource.class);
	}

	@Test
    public void testNoSuchPackage() {
	    try {
    	    ClientRequest request = new ClientRequest(generateURL("/brms/rest/packages/srampPackage/binary")); //$NON-NLS-1$
            @SuppressWarnings("unused")
            ClientResponse<InputStream> response = request.get(InputStream.class);
            Assert.fail("Expecting to find no such package."); //$NON-NLS-1$
	    } catch (Exception e) {
	        Assert.assertEquals(e.getClass(), SrampAtomException.class);
	    }

	}

	/**
	 * Tests the BRMS packages.
	 * @throws Exception
	 */
	@Test
	public void testBrmsPackages() throws Exception {
	    //Upload a BrmsPackage
        String artifactFileName = "srampPackage.pkg"; //$NON-NLS-1$
        InputStream contentStream = this.getClass().getResourceAsStream("/brms/srampPackage/" + artifactFileName); //$NON-NLS-1$

        ClientRequest request = new ClientRequest(generateURL("/s-ramp/ext/BrmsPkgDocument")); //$NON-NLS-1$
        request.header("Slug", artifactFileName); //$NON-NLS-1$
        request.body("application/octet-stream", contentStream); //$NON-NLS-1$
        ClientResponse<Entry> response = request.post(Entry.class);
        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);

//        System.out.println(arty.getUuid() + " " + arty.getArtifactType().value());

        //check that we can download this package's content
        ClientRequest request1 = new ClientRequest(generateURL("/brms/rest/packages/srampPackage/binary")); //$NON-NLS-1$
        ClientResponse<InputStream> response1 = request1.get(InputStream.class);
        if (response1.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " //$NON-NLS-1$
                + response1.getStatus());
        }
        InputStream in = response1.getEntity();
        File file = new File("target/SRAMP-srampPackage.pkg"); //$NON-NLS-1$
        OutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);
        out.flush();
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);


        //obtain all BRMS packages using the brms rest API
		ClientRequest request2 = new ClientRequest(generateURL("/brms/rest/packages")); //$NON-NLS-1$
		ClientResponse<Packages> response2 = request2.get(Packages.class);
		Packages packages = response2.getEntity();

		Assert.assertEquals(1, packages.getPackage().size());

		Assert.assertEquals("srampPackage",packages.getPackage().get(0).getTitle()); //$NON-NLS-1$

		//Now adding the assetInfo and then asking for the assets of this package
		//Update the entry by adding the asset info, for now in a property (later as a dependent artifact)
        Assert.assertTrue(arty instanceof ExtendedDocument);
        ExtendedDocument brmsPkgDocument = (ExtendedDocument) arty;
        Property assetsProperty = new Property();
        assetsProperty.setPropertyName(BrmsConstants.ASSET_INFO_XML);
        //in real life we'd get the asset info from BRMS, and we should update the links so that they
        //point to artifacts in S-RAMP, rather then back to BRMS/drools-guvnor, for the moment
        //we don't care since jBPM only looks at the asset title
        InputStream assetsInputStream = this.getClass().getResourceAsStream("/brms/srampPackage/rest/assets.xml"); //$NON-NLS-1$
        String assetsXml = TestUtils.convertStreamToString(assetsInputStream);
        IOUtils.closeQuietly(assetsInputStream);
        //update the links
        assetsXml = assetsXml.replaceAll("http://localhost:8080/drools-guvnor", "http://localhost:8080/s-ramp-server/brms"); //$NON-NLS-1$ //$NON-NLS-2$
        assetsProperty.setPropertyValue(assetsXml);
        brmsPkgDocument.getProperty().add(assetsProperty);
        Entry entry3 = new Entry();
        ClientRequest request3 = new ClientRequest(generateURL("/s-ramp/ext/BrmsPkgDocument/" + brmsPkgDocument.getUuid())); //$NON-NLS-1$
        Artifact artifact = new Artifact();
        artifact.setExtendedDocument(brmsPkgDocument);
        entry3.setAnyOtherJAXBObject(artifact);
        request3.body(MediaType.APPLICATION_ATOM_XML_ENTRY, entry3);
        request3.put(Void.class);

		//obtain a list of assets in the package
        //Do a query using GET on the pseudo BRMS API
        ClientRequest request4 = new ClientRequest(generateURL("/brms/rest/packages/srampPackage/assets")); //$NON-NLS-1$
        ClientResponse<Assets> response4 = request4.get(Assets.class);
        Assets assets = response4.getEntity();

        Assert.assertEquals(8, assets.getAsset().size());

        //Upload the process AND process-image, making sure the uuid is identical to the one mentioned
        for (Assets.Asset asset : assets.getAsset()) {
            String fileFormat = asset.getMetadata().getFormat().toLowerCase();
            if (fileFormat.equals("package")) { //$NON-NLS-1$
                //This is asset Info on the package itself, we can use this to update the BrmsPkgDocument
                //add things like versionNumber, description, author, notes etc.
            } else {
                //Upload the asset
                String fileName = asset.getTitle() + "." + asset.getMetadata().getFormat(); //$NON-NLS-1$
                String uuid = asset.getMetadata().getUuid();
                //reading the asset from disk
                InputStream assetInputStream = this.getClass().getResourceAsStream("/brms/srampPackage/" + fileName); //$NON-NLS-1$
                //upload the asset using the uuid
                @SuppressWarnings("deprecation")
                ArtifactType artifactType = ArtifactType.fromFileExtension(asset.getMetadata().getFormat());
                if (artifactType.isExtendedType()) {
                    artifactType = ArtifactType.ExtendedDocument(artifactType.getExtendedType());
                }
                BaseArtifactType baseArtifactType = artifactType.newArtifactInstance();
                baseArtifactType.setName(fileName);
                baseArtifactType.setUuid(uuid);

                String path = "/s-ramp/" + artifactType.getModel() + "/" + artifactType.getType(); //$NON-NLS-1$ //$NON-NLS-2$
                ClientRequest request5 = new ClientRequest(generateURL(path));
                MultipartRelatedOutput output = new MultipartRelatedOutput();
                
                Entry atomEntry = new Entry();
                MediaType mediaType = new MediaType("application", "atom+xml"); //$NON-NLS-1$ //$NON-NLS-2$
                artifact = new Artifact();

                System.out.println("Creating artifact of type: " + artifactType.getArtifactType().getTypeClass()); //$NON-NLS-1$

                //get the right method call
                String methodStr = "set" + artifactType.getArtifactType(); //$NON-NLS-1$
                Method method = artifact.getClass().getMethod(methodStr, artifactType.getArtifactType().getTypeClass());
                method.invoke(artifact,baseArtifactType);

                //artifact.setExtendedArtifactType(baseArtifactType);
                atomEntry.setAnyOtherJAXBObject(artifact);
                output.addPart(atomEntry, mediaType);

                MediaType mediaType2 = MediaType.getInstance(artifactType.getMimeType());
                output.addPart(assetInputStream, mediaType2);

                System.out.println("Uploading asset " + fileName + " " + artifactType); //$NON-NLS-1$ //$NON-NLS-2$
                request5.body(MultipartConstants.MULTIPART_RELATED, output);
                ClientResponse<Entry> assetResponse = request5.post(Entry.class);
                IOUtils.closeQuietly(assetInputStream);

                Entry assetEntry = assetResponse.getEntity();
                BaseArtifactType assetArtifact = SrampAtomUtils.unwrapSrampArtifact(assetEntry);
                System.out.println("Uploaded asset " + assetArtifact.getName() + " " + assetArtifact.getUuid()); //$NON-NLS-1$ //$NON-NLS-2$

            }
        }

        //now see if we can retrieve some of the assets
        //for example the Evaluation.bpmn file should be available under
        // http://localhost:8080/drools-guvnor/rest/packages/srampPackage/assets/Evaluation/binary
        //Do a query using GET on the pseudo BRMS API, to get the content of the bpmn
        ClientRequest request7 = new ClientRequest(generateURL("/brms/rest/packages/srampPackage/assets/Evaluation/binary")); //$NON-NLS-1$
        ClientResponse<InputStream> response7 = request7.get(InputStream.class);
        if (response1.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " //$NON-NLS-1$
                + response.getStatus());
        }
        InputStream in7 = response7.getEntity();
        File file7 = new File("target/SRAMP-Evaluation.bpmn"); //$NON-NLS-1$
        OutputStream out7 = new FileOutputStream(file7);
        IOUtils.copy(in7, out7);
        out7.flush();
        IOUtils.closeQuietly(in7);
        IOUtils.closeQuietly(out7);
        Assert.assertTrue(file7.exists());
        long size = file7.length();
        Assert.assertTrue(size >= 12483L);
        //also check if we can retrieve the image
        ClientRequest request8 = new ClientRequest(generateURL("/brms/rest/packages/srampPackage/assets/Evaluation-image/binary")); //$NON-NLS-1$
        ClientResponse<InputStream> response8 = request8.get(InputStream.class);
        if (response1.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " //$NON-NLS-1$
                + response.getStatus());
        }
        InputStream in8 = response8.getEntity();
        File file8 = new File("target/SRAMP-Evaluation-image.png"); //$NON-NLS-1$
        OutputStream out8 = new FileOutputStream(file8);
        IOUtils.copy(in8, out8);
        out8.flush();
        IOUtils.closeQuietly(in8);
        IOUtils.closeQuietly(out8);
        Assert.assertTrue(file8.exists());
        Assert.assertEquals(14029l,file8.length());
	}
}
