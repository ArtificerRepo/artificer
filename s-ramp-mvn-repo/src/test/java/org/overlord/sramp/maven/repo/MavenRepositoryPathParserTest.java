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
package org.overlord.sramp.maven.repo;

import junit.framework.Assert;

import org.junit.Test;

/**
 * JUnit test for {@link MavenRepositoryPathParser}.
 *
 * @author eric.wittmann@redhat.com
 */
public class MavenRepositoryPathParserTest {
	
	private static final String [][] TEST_CASES = {
		{
			"/",  // Path used for testing
			null, null, null, null, null, null,  // Expected segments after parsing
			"/"  // Expected value of toString()
		},
		{
			"/core",
			"core", null, null, null, null, null,
			"/core/"
		},
		{
			"/core/",
			"core", null, null, null, null, null,
			"/core/"
		},
		{
			"/core/XsdDocument",
			"core", "XsdDocument", null, null, null, null,
			"/core/XsdDocument/"
		},
		{
			"/core/XsdDocument/",
			"core", "XsdDocument", null, null, null, null,
			"/core/XsdDocument/"
		},
		{
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66",
			"core", "XsdDocument", "0cbafbd0-c528-11e1-9b21-0800200c9a66", null, null, null,
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/"
		},
		{
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/",
			"core", "XsdDocument", "0cbafbd0-c528-11e1-9b21-0800200c9a66", null, null, null,
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/"
		},
		{
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/1.0.7",
			"core", "XsdDocument", "0cbafbd0-c528-11e1-9b21-0800200c9a66", "1.0.7", null, null,
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/1.0.7/"
		},
		{
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/1.0.7/",
			"core", "XsdDocument", "0cbafbd0-c528-11e1-9b21-0800200c9a66", "1.0.7", null, null,
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/1.0.7/"
		},
		{
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/1.0.7/0cbafbd0-c528-11e1-9b21-0800200c9a66.xsd",
			"core", "XsdDocument", "0cbafbd0-c528-11e1-9b21-0800200c9a66", "1.0.7", "0cbafbd0-c528-11e1-9b21-0800200c9a66", "xsd",
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/1.0.7/0cbafbd0-c528-11e1-9b21-0800200c9a66.xsd"
		},
		{
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/1.0.7/0cbafbd0-c528-11e1-9b21-0800200c9a66.pom",
			"core", "XsdDocument", "0cbafbd0-c528-11e1-9b21-0800200c9a66", "1.0.7", "0cbafbd0-c528-11e1-9b21-0800200c9a66", "pom",
			"/core/XsdDocument/0cbafbd0-c528-11e1-9b21-0800200c9a66/1.0.7/0cbafbd0-c528-11e1-9b21-0800200c9a66.pom"
		}

	};

	/**
	 * Test method for {@link org.overlord.sramp.maven.repo.MavenRepositoryPathParser#parse(java.lang.String)}.
	 */
	@Test
	public void testParse() {
		MavenRepositoryPathParser parser = new MavenRepositoryPathParser();
		for (String[] testCase : TEST_CASES) {
			String testPath = testCase[0];
			String expectedModel = testCase[1];
			String expectedType = testCase[2];
			String expectedUuid = testCase[3];
			String expectedVersion = testCase[4];
			String expectedFileName = testCase[5];
			String expectedExtension = testCase[6];
			String expectedToString = testCase[7];
			MavenRepositoryPath parsedPath = parser.parse(testPath);
			String infoMsg = "Assertion failed on test case: '" + testPath + "'.";
			Assert.assertEquals(infoMsg, expectedModel, parsedPath.getArtifactModel());
			Assert.assertEquals(infoMsg, expectedType, parsedPath.getArtifactType());
			Assert.assertEquals(infoMsg, expectedUuid, parsedPath.getArtifactUuid());
			Assert.assertEquals(infoMsg, expectedVersion, parsedPath.getArtifactVersion());
			Assert.assertEquals(infoMsg, expectedFileName, parsedPath.getArtifactFileName());
			Assert.assertEquals(infoMsg, expectedExtension, parsedPath.getArtifactExtension());
			Assert.assertEquals(infoMsg, expectedToString, parsedPath.toString());
		}
	}

}
