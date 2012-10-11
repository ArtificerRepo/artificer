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
package org.overlord.sramp.atom.services.brms;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.atom.services.AbstractResourceTest;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;

import test.org.overlord.sramp.atom.TestUtils;


/**
 * Tests the BRMS API.
 *
 * @author kurt.stam@redhat.com
 */
public class BrmsResourceTest extends AbstractResourceTest {

	@BeforeClass
	public static void setUpBrms() throws Exception {
		dispatcher.getRegistry().addPerRequestResource(BrmsResource.class);
	}

	/**
	 * Tests the BRMS packages.
	 * @throws Exception
	 */
	@Test @Ignore
	public void testBrmsPackages() throws Exception {
	    //Upload a BrmsPackage
        String artifactFileName = "srampPackage.pkg";
        InputStream contentStream = this.getClass().getResourceAsStream("/brms/srampPackage/" + artifactFileName);
      
        ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/BrmsPkgDocument"));
        request.header("Slug", artifactFileName);
        request.body("application/octet-stream", contentStream);
        ClientResponse<Entry> response = request.post(Entry.class);
        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);

        System.out.println(arty.getUuid() + " " + arty.getArtifactType().value());
        
        //check that we can download this package's content
        ClientRequest request1 = new ClientRequest(generateURL("/brms/rest/packages/srampPackage/binary"));
        ClientResponse<InputStream> response1 = request1.get(InputStream.class);
        if (response1.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                + response.getStatus());
        }
        InputStream in = response1.getEntity();
        File file = new File("target/SRAMP-srampPackage.pkg");
        OutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);
        out.flush();
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        

        //obtain all BRMS packages using the brms rest API
		ClientRequest request2 = new ClientRequest(generateURL("/brms/rest/packages"));
		ClientResponse<Packages> response2 = request2.get(Packages.class);
		Packages packages = response2.getEntity();

		Assert.assertEquals(1, packages.getPackage().size());

		Assert.assertEquals("srampPackage",packages.getPackage().get(0).getTitle());
		
		
		//Now adding the assetInfo and then asking for the assets of this package
		
		//Update the entry by adding the asset info, for now in a property (later as a dependent artifact)
        Assert.assertTrue(arty instanceof UserDefinedArtifactType);
        UserDefinedArtifactType brmsPkgDocument = (UserDefinedArtifactType) arty;
        Property assetsProperty = new Property();
        assetsProperty.setPropertyName(BrmsConstants.ASSET_INFO_XML);
        //in real life we'd get the asset info from BRMS, and we should update the links so that they
        //point to artifacts in S-RAMP, rather then back to BRMS/drools-guvnor, for the moment
        //we don't care since jBPM only looks at the asset title
        InputStream assetsInputStream = this.getClass().getResourceAsStream("/brms/srampPackage/rest/assets.xml");
        String assetsXml = TestUtils.convertStreamToString(assetsInputStream);
        IOUtils.closeQuietly(assetsInputStream);
        //update the links
        assetsXml = assetsXml.replaceAll("http://localhost:8080/drools-guvnor", "http://localhost:8080/s-ramp/brms");
        assetsProperty.setPropertyValue(assetsXml);
        brmsPkgDocument.getProperty().add(assetsProperty);
        Entry entry3 = new Entry();
        ClientRequest request3 = new ClientRequest(generateURL("/s-ramp/user/BrmsPkgDocument/" + brmsPkgDocument.getUuid()));
        Artifact artifact = new Artifact();
        artifact.setUserDefinedArtifactType(brmsPkgDocument);
        entry3.setAnyOtherJAXBObject(artifact);
        request3.body(MediaType.APPLICATION_ATOM_XML_ENTRY, entry3);
        request3.put(Void.class);
        
		//obtain a list of assets in the package
        //Do a query using GET on the pseudo BRMS API
        ClientRequest request4 = new ClientRequest(generateURL("/brms/rest/packages/srampPackage/assets"));
        ClientResponse<Assets> response4 = request4.get(Assets.class);
        Assets assets = response4.getEntity();
        
        Assert.assertEquals(8, assets.getAsset().size());
        
        //Upload the process AND process-image, making sure the uuid is identical to the one mentioned 
        for (Assets.Asset asset : assets.getAsset()) {
            String fileFormat = asset.getMetadata().getFormat().toLowerCase();
            if (fileFormat.equals("package")) {
                //This is asset Info on the package itself, we can use this to update the BrmsPkgDocument
                //add things like versionNumber, description, author, notes etc.
            } else {
                //Upload the asset
                String fileName = asset.getTitle() + "." + asset.getMetadata().getFormat();
                String uuid = asset.getMetadata().getUuid();
                
                
                //reading the asset from disk
                InputStream assetInputStream = this.getClass().getResourceAsStream("/brms/srampPackage/" + fileName);
                //upload the asset using the uuid
                ArtifactType artifactType = ArtifactType.fromFileExtension(asset.getMetadata().getFormat());
//                UserDefinedArtifactType userDefinedArtifactType = (UserDefinedArtifactType) ArtifactType.getArtifactInstance(artifactType);
//                userDefinedArtifactType.setName(fileName);
//                userDefinedArtifactType.setUuid(uuid);
//                
//                String path = "/s-ramp/" + artifactType.getModel() + "/" + artifactType.getType();
//                ClientRequest request5 = new ClientRequest(generateURL(path));
//                MultipartRelatedOutput output = new MultipartRelatedOutput();
//                
//                Entry atomEntry = new Entry();
//                MediaType mediaType = new MediaType("application", "atom+xml");
//                artifact = new Artifact();
//                artifact.setUserDefinedArtifactType(userDefinedArtifactType);
//                atomEntry.setAnyOtherJAXBObject(arty);
//                output.addPart(atomEntry, mediaType);
//                
//                MediaType mediaType2 = MediaType.getInstance(artifactType.getMimeType());
//                output.addPart(assetInputStream, mediaType2);
                
                System.out.println("Uploading asset " + fileName + " " + artifactType);
                //request5.body(MultipartConstants.MULTIPART_RELATED, output);

                
                IOUtils.closeQuietly(assetInputStream);
            }
        }
	}
	




}
