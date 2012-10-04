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
import java.io.InputStream;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.err.SrampAtomExceptionMapper;
import org.overlord.sramp.atom.services.ArtifactResource;
import org.overlord.sramp.atom.services.FeedResource;
import org.overlord.sramp.atom.services.QueryResource;
import org.overlord.sramp.atom.services.brms.Assets;
import org.overlord.sramp.atom.services.brms.BrmsResource;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.jcr.JCRRepositoryCleaner;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;


/**
 * Tests the s-ramp query features of the atom api binding.
 *
 * @author kurt.stam@redhat.com
 */
public class BrmsResourceTest extends BaseResourceTest {

	@BeforeClass
	public static void setUp() throws Exception {
	    File repos = new File("target/repos");
	    if (repos.exists() && repos.isDirectory() ) {
	        repos.delete();
	    }
	    dispatcher.getRegistry().addPerRequestResource(ArtifactResource.class);
		dispatcher.getRegistry().addPerRequestResource(BrmsResource.class);
		dispatcher.getRegistry().addPerRequestResource(FeedResource.class);
		dispatcher.getRegistry().addPerRequestResource(QueryResource.class);
	}
	
	@Before
    public void clean() {
	    getProviderFactory().registerProvider(SrampAtomExceptionMapper.class);
        new JCRRepositoryCleaner().clean();
    }
	
	@AfterClass
    public static void cleanup() {
        PersistenceFactory.newInstance().shutdown();
    }

	/**
	 * Tests the BRMS packages.
	 * @throws Exception
	 */
	@Test
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
