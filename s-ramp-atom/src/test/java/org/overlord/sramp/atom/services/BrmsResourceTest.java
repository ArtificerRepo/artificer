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
package org.overlord.sramp.atom.services;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.overlord.sramp.atom.services.brms.Assets;
import org.overlord.sramp.atom.services.brms.Packages;
import org.overlord.sramp.repository.jcr.JCRRepositoryCleaner;


/**
 * Tests the s-ramp query features of the atom api binding.
 *
 * @author kurt.stam@redhat.com
 */
public class BrmsResourceTest extends BaseResourceTest {

	@Before
	public void setUp() throws Exception {
		dispatcher.getRegistry().addPerRequestResource(BrmsResource.class);
		new JCRRepositoryCleaner().clean();
	}

	/**
	 * Tests the packages.
	 * @throws Exception
	 */
	@Test
	public void testBrmsPackages() throws Exception {

		// Do a query using GET
		ClientRequest request = new ClientRequest(generateURL("/brms/rest/packages"));
		ClientResponse<Packages> response = request.get(Packages.class);
		Packages packages = response.getEntity();
		
		Assert.assertEquals(1, packages.getPackage().size());
	}

	   /**
     * Tests the packages.
     * @throws Exception
     */
    @Test
    public void testBrmsAssets() throws Exception {

        // Do a query using GET
        ClientRequest request = new ClientRequest(generateURL("/brms/rest/packages/defaultPackage/assets"));
        ClientResponse<Assets> response = request.get(Assets.class);
        Assets assets = response.getEntity();
        
        Assert.assertEquals(13, assets.getAsset().size());
    }


}
