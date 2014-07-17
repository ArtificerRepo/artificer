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
package org.overlord.sramp.common.query.xpath;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.common.query.xpath.ast.Query;
import org.overlord.sramp.common.query.xpath.visitors.XPathSerializationVisitor;

/**
 * Unit test for the {@link XPathParser} class.  This test case loads a number of test cases
 * from *.properties files located in the src/test/resources folder.  See the files here:<br/>
 * <br/>
 * <pre>src/test/java/org/overlord/sramp/query/xpath/parser-test-cases</pre>
 * <br/>
 * These test case files are loaded as {@link Properties} objects and then are processed
 * sequentially.  All assertions should indicate which test case failed so any error message
 * can be correlated back to the proper .properties file.
 *
 * @author eric.wittmann@redhat.com
 */
public class XPathParserTest {

	@Test
	public void testXPathParser() throws Exception {
		Collection<Properties> testCases = getTestCases();

		XPathParser parser = new XPathParser();
		XPathSerializationVisitor visitor = new XPathSerializationVisitor();
		for (Properties properties : testCases) {
			String testCaseName = properties.getProperty("testcase.name"); //$NON-NLS-1$
			String xpath = properties.getProperty("xpath"); //$NON-NLS-1$
			String expectedXpath = properties.getProperty("expected.xpath"); //$NON-NLS-1$
			String expectedErrorMessage = properties.getProperty("expected.errorMessage"); //$NON-NLS-1$

			try {
				Query query = parser.parseXPath(xpath);
				visitor.reset();
				query.accept(visitor);
				String actualXpath = visitor.getXPath();
				Assert.assertNotNull("Case [" + testCaseName + "]", query); //$NON-NLS-1$ //$NON-NLS-2$
				Assert.assertEquals("Case [" + testCaseName + "]", expectedXpath, actualXpath); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (XPathParserException e) {
				Assert.assertNotNull("Case [" + testCaseName + "] Got unexpected parse error: " + e.getMessage(), expectedErrorMessage); //$NON-NLS-1$ //$NON-NLS-2$
				Assert.assertEquals("Case [" + testCaseName + "]", expectedErrorMessage, e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (AssertionError e) {
				throw e;
			} catch (Throwable t) {
				Assert.fail("Case [" + testCaseName + "] Got unexpected error: " + t.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
//		System.out.println("All " + testCases.size() + " XPath parser test cases passed.");
	}

	/**
	 * Gets the test cases.
	 * @throws Exception
	 */
	private Collection<Properties> getTestCases() throws Exception {
		URL testCaseDirUrl = XPathParserTest.class.getResource("parser-test-cases"); //$NON-NLS-1$
		Assert.assertNotNull("Failed to find test case directory!", testCaseDirUrl); //$NON-NLS-1$
		File testCaseDir = new File(testCaseDirUrl.toURI());
		if (!testCaseDir.isDirectory())
			throw new Exception("Failed to find test case directory: " + testCaseDirUrl); //$NON-NLS-1$

		Collection<File> testCaseFiles = FileUtils.listFiles(testCaseDir, new String[] { "properties" }, true); //$NON-NLS-1$
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
			props.setProperty("testcase.name", testCaseFile.getName()); //$NON-NLS-1$
			testCases.add(props);
		}
		return testCases;
	}
	
}
