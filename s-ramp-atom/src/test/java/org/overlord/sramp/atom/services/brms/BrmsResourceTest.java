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

import java.io.InputStream;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.atom.services.AbstractResourceTest;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;


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
        String artifactFileName = "defaultPackage.pkg";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/user/" + artifactFileName);

        ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/BrmsPkgDocument"));
        request.header("Slug", artifactFileName);
        request.body("application/octet-stream", contentStream);
        ClientResponse<Entry> response = request.post(Entry.class);
        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
        System.out.println(arty.getArtifactType().value());
        //TODO maybe make this a multipart upload so we can preserve the UUID given to the package by BRMS

        //obtain all BRMS packages using the brms rest API
		ClientRequest request2 = new ClientRequest(generateURL("/brms/rest/packages"));
		ClientResponse<Packages> response2 = request2.get(Packages.class);
		Packages packages = response2.getEntity();

		Assert.assertEquals(1, packages.getPackage().size());
		Assert.assertEquals("defaultPackage.pkg",packages.getPackage().get(0).getTitle());
	}

	   /**
     * Tests the packages.
     * @throws Exception
     */
    @Test @Ignore
    public void testBrmsAssets() throws Exception {

        // Do a query using GET
        ClientRequest request = new ClientRequest(generateURL("/brms/rest/packages/defaultPackage/assets"));
        ClientResponse<Assets> response = request.get(Assets.class);
        Assets assets = response.getEntity();

        Assert.assertEquals(13, assets.getAsset().size());
    }


}
