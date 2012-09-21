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
		// Resource path,
		// groupId, artifactId, version, classifier, type, name, isHash, isSnapshot, snapshotTimestamp
		{
			"org/example/schema/my-schema/1.3/my-schema-1.3.xsd",
			"org.example.schema", "my-schema", "1.3", null, "xsd", "my-schema-1.3.xsd", "false", "false", null
		},
		{
			"xsd/XsdDocument/29873-21983-2497822-1989/1.0/29873-21983-2497822-1989-1.0.pom",
			"xsd.XsdDocument", "29873-21983-2497822-1989", "1.0", null, "pom", "29873-21983-2497822-1989-1.0.pom", "false", "false", null
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar",
			"org.apache.commons", "commons-io", "1.3.2", null, "jar", "commons-io-1.3.2.jar", "false", "false", null
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.pom.sha1",
			"org.apache.commons", "commons-io", "1.3.2", null, "pom.sha1", "commons-io-1.3.2.pom.sha1", "true", "false", null
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar.sha1",
			"org.apache.commons", "commons-io", "1.3.2", null, "jar.sha1", "commons-io-1.3.2.jar.sha1", "true", "false", null
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2-sources.jar",
			"org.apache.commons", "commons-io", "1.3.2", "sources", "jar", "commons-io-1.3.2-sources.jar", "false", "false", null
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2-sources.jar.md5",
			"org.apache.commons", "commons-io", "1.3.2", "sources", "jar.md5", "commons-io-1.3.2-sources.jar.md5", "true", "false", null
		},
		{
			"org/overlord/sramp/test/test-wagon-push/0.0.1-SNAPSHOT/test-wagon-push-0.0.1-20120921.113704-1.jar",
			"org.overlord.sramp.test", "test-wagon-push", "0.0.1-SNAPSHOT", null, "jar", "test-wagon-push-0.0.1-20120921.113704-1.jar", "false", "true", "20120921.113704-1"
		},
		{
			"org/overlord/sramp/test/test-wagon-push/0.0.1-SNAPSHOT/test-wagon-push-0.0.1-20120921.113704-1-sources.jar.md5",
			"org.overlord.sramp.test", "test-wagon-push", "0.0.1-SNAPSHOT", "sources", "jar.md5", "test-wagon-push-0.0.1-20120921.113704-1-sources.jar.md5", "true", "true", "20120921.113704-1"
		},
		{
			"org/overlord/sramp/test/test-wagon-push/0.0.1-SNAPSHOT/test-wagon-push-0.0.1-20120921.113704-1.pom.sha1",
			"org.overlord.sramp.test", "test-wagon-push", "0.0.1-SNAPSHOT", null, "pom.sha1", "test-wagon-push-0.0.1-20120921.113704-1.pom.sha1", "true", "true", "20120921.113704-1"
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
			String expectedClassifier = testCase[4];
			String expectedType = testCase[5];
			String expectedName = testCase[6];
			String expectedHash = testCase[7];
			String expectedSnapshot = testCase[8];
			String expectedSnapshotId = testCase[9];

			MavenGavInfo gavInfo = MavenGavInfo.fromResource(new Resource(resourcePath));

			String helpfulMsg = "Failure in test case: " + resourcePath;
			Assert.assertEquals(helpfulMsg, expectedGroupId, gavInfo.getGroupId());
			Assert.assertEquals(helpfulMsg, expectedArtifactId, gavInfo.getArtifactId());
			Assert.assertEquals(helpfulMsg, expectedVersion, gavInfo.getVersion());
			Assert.assertEquals(helpfulMsg, expectedClassifier, gavInfo.getClassifier());
			Assert.assertEquals(helpfulMsg, expectedType, gavInfo.getType());
			Assert.assertEquals(helpfulMsg, expectedName, gavInfo.getName());
			Assert.assertEquals(helpfulMsg, expectedHash, String.valueOf(gavInfo.isHash()));
			Assert.assertEquals(helpfulMsg, expectedSnapshot, String.valueOf(gavInfo.isSnapshot()));
			Assert.assertEquals(helpfulMsg, expectedSnapshotId, gavInfo.getSnapshotId());
		}
	}

}
