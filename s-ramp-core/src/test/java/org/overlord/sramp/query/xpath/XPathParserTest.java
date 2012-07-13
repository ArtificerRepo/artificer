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
package org.overlord.sramp.query.xpath;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.TreeSet;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.overlord.sramp.query.xpath.ast.SrampQuery;

/**
 * Unit test for the {@link XPathParser} class.  This test case loads a number of test cases 
 * from *.properties files located in the src/test/resources folder.  See the files here:<br/>
 * <br/>
 * <pre>src/test/java/org/overlord/sramp/query/xpath/parser-test-cases</pre>
 * <br/>
 * These test case files are loaded as {@link Properties} objects and then are processed
 * sequentially.  All assertions should indicate which test case failed.
 *
 * @author eric.wittmann@redhat.com
 */
public class XPathParserTest {
	
	@Test
	public void testArtifactSet() throws Exception {
		Collection<Properties> testCases = getTestCases("artifact-set");
		
		XPathParser parser = new XPathParser();
		for (Properties properties : testCases) {
			String testCaseName = properties.getProperty("testcase.name");
			System.out.println("Executing test case: " + testCaseName);
			String xpath = properties.getProperty("xpath");
			String expectedModel = properties.getProperty("expected.artifactModel");
			String expectedType = properties.getProperty("expected.artifactType");
			String expectedErrorMessage = properties.getProperty("expected.errorMessage");
			
			try {
				SrampQuery query = parser.parseXPath(xpath);
				Assert.assertNotNull("Case [" + testCaseName + "]", query);
				Assert.assertNotNull("Case [" + testCaseName + "]", query.getArtifactSet());
				Assert.assertEquals("Case [" + testCaseName + "]", expectedModel, query.getArtifactSet().getArtifactModel());
				Assert.assertEquals("Case [" + testCaseName + "]", expectedType, query.getArtifactSet().getArtifactType());
			} catch (XPathParserException e) {
				Assert.assertNotNull("Got unexpected parse error: " + e.getMessage(), expectedErrorMessage);
				Assert.assertEquals("Case [" + testCaseName + "]", expectedErrorMessage, e.getMessage());
			}
		}
	}

	/**
	 * Gets the test cases from the given test case folder.
	 * @throws Exception
	 */
	private Collection<Properties> getTestCases(String testCaseFolder) throws Exception {
		URL testCaseDirUrl = XPathParserTest.class.getResource("parser-test-cases/" + testCaseFolder);
		File testCaseDir = new File(testCaseDirUrl.toURI());
		if (!testCaseDir.isDirectory())
			throw new Exception("Failed to find test case directory: " + testCaseDirUrl);
		
		@SuppressWarnings("unchecked")
		Collection<File> testCaseFiles = (Collection<File>) FileUtils.listFiles(testCaseDir, new String[] { "properties" }, false);
		testCaseFiles = new TreeSet<File>(testCaseFiles);
		Collection<Properties> testCases = new ArrayList<Properties>(testCaseFiles.size());
		for (File testCaseFile : testCaseFiles) {
			Properties props = new Properties();
			FileReader reader = new FileReader(testCaseFile);
			try {
				props.load(reader);
			} finally {
				IOUtils.closeQuietly(reader);
			}
			props.setProperty("testcase.name", testCaseFolder + "/" + testCaseFile.getName());
			testCases.add(props);
		}
		return testCases;
	}
	
}
