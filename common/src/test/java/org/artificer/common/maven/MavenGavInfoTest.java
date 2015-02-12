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
package org.artificer.common.maven;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link MavenGavInfo}.
 *
 * @author eric.wittmann@redhat.com
 */
public class MavenGavInfoTest {

	private static final String [][] TEST_DATA = {
		// Resource path,
		// groupId, artifactId, version, classifier, type, name, isHash, isSnapshot, snapshotTimestamp, isMetaData
		{
			"org/example/schema/my-schema/1.3/my-schema-1.3.xsd", //$NON-NLS-1$
			"org.example.schema", "my-schema", "1.3", null, "xsd", "my-schema-1.3.xsd", "false", "false", null, "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
		},
		{
			"xsd/XsdDocument/29873-21983-2497822-1989/1.0/29873-21983-2497822-1989-1.0.pom", //$NON-NLS-1$
			"xsd.XsdDocument", "29873-21983-2497822-1989", "1.0", null, "pom", "29873-21983-2497822-1989-1.0.pom", "false", "false", null, "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar", //$NON-NLS-1$
			"org.apache.commons", "commons-io", "1.3.2", null, "jar", "commons-io-1.3.2.jar", "false", "false", null, "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.pom.sha1", //$NON-NLS-1$
			"org.apache.commons", "commons-io", "1.3.2", null, "pom.sha1", "commons-io-1.3.2.pom.sha1", "true", "false", null, "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar.sha1", //$NON-NLS-1$
			"org.apache.commons", "commons-io", "1.3.2", null, "jar.sha1", "commons-io-1.3.2.jar.sha1", "true", "false", null, "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2-sources.jar", //$NON-NLS-1$
			"org.apache.commons", "commons-io", "1.3.2", "sources", "jar", "commons-io-1.3.2-sources.jar", "false", "false", null, "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
		},
		{
			"org/apache/commons/commons-io/1.3.2/commons-io-1.3.2-sources.jar.md5", //$NON-NLS-1$
			"org.apache.commons", "commons-io", "1.3.2", "sources", "jar.md5", "commons-io-1.3.2-sources.jar.md5", "true", "false", null, "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
		},
		{
			"org/artificer/test/test-wagon-push/0.0.1-SNAPSHOT/test-wagon-push-0.0.1-20120921.113704-1.jar", //$NON-NLS-1$
			"org.artificer.test", "test-wagon-push", "0.0.1-SNAPSHOT", null, "jar", "test-wagon-push-0.0.1-20120921.113704-1.jar", "false", "true", "20120921.113704-1", "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
		},
		{
			"org/artificer/test/test-wagon-push/0.0.1-SNAPSHOT/test-wagon-push-0.0.1-20120921.113704-1-sources.jar.md5", //$NON-NLS-1$
			"org.artificer.test", "test-wagon-push", "0.0.1-SNAPSHOT", "sources", "jar.md5", "test-wagon-push-0.0.1-20120921.113704-1-sources.jar.md5", "true", "true", "20120921.113704-1", "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
		},
		{
			"org/artificer/test/test-wagon-push/0.0.1-SNAPSHOT/test-wagon-push-0.0.1-20120921.113704-1.pom.sha1", //$NON-NLS-1$
			"org.artificer.test", "test-wagon-push", "0.0.1-SNAPSHOT", null, "pom.sha1", "test-wagon-push-0.0.1-20120921.113704-1.pom.sha1", "true", "true", "20120921.113704-1", "false" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
		},
        {
            "org/apache/commons/commons-io/maven-metadata.xml", //$NON-NLS-1$
            "org.apache.commons", "commons-io", null, null, "xml", "maven-metadata.xml", "false", "false", null, "true" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        },
        {
            "org/apache/commons/commons-io/maven-metadata.xml.md5", //$NON-NLS-1$
            "org.apache.commons", "commons-io", null, null, "xml.md5", "maven-metadata.xml.md5", "true", "false", null, "true" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        },
        {
            "org/apache/commons/commons-io/1.0.7-SNAPSHOT/maven-metadata.xml", //$NON-NLS-1$
            "org.apache.commons", "commons-io", "1.0.7-SNAPSHOT", null, "xml", "maven-metadata.xml", "false", "true", null, "true" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        },
	};

	@Test
	public void testGavInfo() {
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
            String expectedMetaData = testCase[10];

			MavenGavInfo gavInfo = MavenGavInfo.fromUrl(resourcePath);

			String helpfulMsg = "Failure in test case: " + resourcePath; //$NON-NLS-1$
			Assert.assertEquals(helpfulMsg, expectedGroupId, gavInfo.getGroupId());
			Assert.assertEquals(helpfulMsg, expectedArtifactId, gavInfo.getArtifactId());
			Assert.assertEquals(helpfulMsg, expectedVersion, gavInfo.getVersion());
			Assert.assertEquals(helpfulMsg, expectedClassifier, gavInfo.getClassifier());
			Assert.assertEquals(helpfulMsg, expectedType, gavInfo.getType());
			Assert.assertEquals(helpfulMsg, expectedName, gavInfo.getName());
			Assert.assertEquals(helpfulMsg, expectedHash, String.valueOf(gavInfo.isHash()));
			Assert.assertEquals(helpfulMsg, expectedSnapshot, String.valueOf(gavInfo.isSnapshot()));
			Assert.assertEquals(helpfulMsg, expectedSnapshotId, gavInfo.getSnapshotId());
            Assert.assertEquals(helpfulMsg, expectedMetaData, String.valueOf(gavInfo.isMavenMetaData()));
		}

		MavenGavInfo gavInfo = MavenGavInfo.fromUrl("org/apache/commons/commons-io/maven-metadata.xml.md5"); //$NON-NLS-1$
		Assert.assertEquals("MD5", gavInfo.getHashAlgorithm()); //$NON-NLS-1$
	}

}
