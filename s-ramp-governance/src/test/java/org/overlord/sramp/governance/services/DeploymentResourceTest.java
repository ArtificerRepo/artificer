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
package org.overlord.sramp.governance.services;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;

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
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.services.brms.assets.Assets;
import org.overlord.sramp.atom.services.brms.packages.Packages;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;


/**
 * Tests the Deployment API.
 *
 * @author kurt.stam@redhat.com
 */
public class DeploymentResourceTest extends BaseResourceTest {

	@BeforeClass
	public static void setUpBrms() throws Exception {
		dispatcher.getRegistry().addPerRequestResource(DeploymentResource.class);
	}
	
	/**
	 * This is an integration test, and only works if artifact 'e67e1b09-1de7-4945-a47f-45646752437a'
     * exists in the repo; check the following urls to find out:
     * 
	 * http://localhost:8080/s-ramp-atom/s-ramp?query=/s-ramp[@uuid%3D'e67e1b09-1de7-4945-a47f-45646752437a']
	 * http://localhost:8080/s-ramp-atom/s-ramp/user/BpmnDocument/e67e1b09-1de7-4945-a47f-45646752437a
	 * 
	 * @throws Exception
	 */
	@Test //@Ignore
	public void testDeploy() {
	    ClientRequest request = new ClientRequest(generateURL("/deploy/copy/dev/e67e1b09-1de7-4945-a47f-45646752437a"));
        try {
            @SuppressWarnings("unused")
            ClientResponse response = request.post();
            System.out.println(response.getStatus());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
}
