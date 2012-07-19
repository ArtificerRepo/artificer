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
package org.overlord.sramp.wagon.models;

import junit.framework.Assert;

import org.apache.maven.wagon.resource.Resource;
import org.junit.Test;

/**
 * Test for {@link MavenGavInfo}.
 *
 * @author eric.wittmann@redhat.com
 */
public class MavenGavInfoTest {

	private static final String [][] TEST_DATA = {
		{
			"org/example/schema/my-schema/1.3/my-schema-1.3.xsd",
			"org.example.schema", "my-schema", "1.3", "xsd"
		},
		{
			"xsd/XsdDocument/29873-21983-2497822-1989/1.0/29873-21983-2497822-1989-1.0.pom",
			"xsd.XsdDocument", "29873-21983-2497822-1989", "1.0", "pom"
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar",
			"org.apache.commons", "commons-io", "1.3.2", "jar"
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.pom.sha1",
			"org.apache.commons", "commons-io", "1.3.2", "pom.sha1"
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar.sha1",
			"org.apache.commons", "commons-io", "1.3.2", "jar.sha1"
		},
	};
	
	/**
	 * Test method for {@link org.overlord.sramp.wagon.models.MavenGavInfo#fromResource(org.apache.maven.wagon.resource.Resource)}.
	 */
	@Test
	public void testFromResource() {
		for (String[] testCase : TEST_DATA) {
			String resourcePath = testCase[0];
			String expectedGroupId = testCase[1];
			String expectedArtifactId = testCase[2];
			String expectedVersion = testCase[3];
			String expectedType = testCase[4];
			
			MavenGavInfo gavInfo = MavenGavInfo.fromResource(new Resource(resourcePath));
			
			String helpfulMsg = "Failure in test case: " + resourcePath;
			Assert.assertEquals(helpfulMsg, expectedGroupId, gavInfo.getGroupId());
			Assert.assertEquals(helpfulMsg, expectedArtifactId, gavInfo.getArtifactId());
			Assert.assertEquals(helpfulMsg, expectedVersion, gavInfo.getVersion());
			Assert.assertEquals(helpfulMsg, expectedType, gavInfo.getType());
		}
	}

}
