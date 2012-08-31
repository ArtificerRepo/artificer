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
package org.overlord.sramp.repository.jcr.query;

import junit.framework.Assert;

import org.junit.Test;
import org.overlord.sramp.query.xpath.XPathParser;
import org.overlord.sramp.query.xpath.ast.Query;


/**
 * Tests the {@link SrampToJcrSql2QueryVisitor} class.
 * 
 * @author eric.wittmann@redhat.com
 */
public class SrampToJcrSql2QueryVisitorTest {
	
	private static final String [][] TEST_DATA = {
		{
			"/s-ramp/xsd/XsdDocument",
			"SELECT * FROM [overlord:artifact] WHERE [sramp:artifactType] = 'XsdDocument'"
		},
		{
			"/s-ramp/xsd",
			"SELECT * FROM [overlord:artifact] WHERE [sramp:artifactModel] = 'xsd'"
		},
		{
			"/s-ramp",
			"SELECT * FROM [overlord:artifact] WHERE [sramp:artifact] = 'true'"
		},
		{
			"/s-ramp/xsd/XsdDocument[@name = 'foo']",
			"SELECT * FROM [overlord:artifact] WHERE [sramp:artifactType] = 'XsdDocument' AND ([sramp:name] = 'foo')"
		},
		{
			"/s-ramp/xsd/XsdDocument[@createdBy = 'lincoln73']",
			"SELECT * FROM [overlord:artifact] WHERE [sramp:artifactType] = 'XsdDocument' AND ([jcr:createdBy] = 'lincoln73')"
		},
		{
			"/s-ramp/xsd/XsdDocument[@prop1]",
			"SELECT * FROM [overlord:artifact] WHERE [sramp:artifactType] = 'XsdDocument' AND ([sramp-properties:prop1] LIKE '%')"
		},
		{
			"/s-ramp/xsd/XsdDocument[@version = '1.0' and @prop1 = 'value1']",
			"SELECT * FROM [overlord:artifact] WHERE [sramp:artifactType] = 'XsdDocument'" +
			" AND ([version] = '1.0'" +
			" AND [sramp-properties:prop1] = 'value1')"
		},
		{
			"/s-ramp/xsd/XsdDocument[@version = '1.0' or @prop1 = 'value1']",
			"SELECT * FROM [overlord:artifact] WHERE [sramp:artifactType] = 'XsdDocument'" +
			" AND ([version] = '1.0'" +
			" OR [sramp-properties:prop1] = 'value1')"
		},
	};

	/**
	 * Tests the visitor.
	 */
	@Test
	public void testVisitor() {
		for (String[] testCase : TEST_DATA) {
			String srampXpath = testCase[0];
			String expectedJcrSQL2 = testCase[1];
			SrampToJcrSql2QueryVisitor visitor = new SrampToJcrSql2QueryVisitor();
			XPathParser parser = new XPathParser();
			Query srampQuery = parser.parseXPath(srampXpath);
			srampQuery.accept(visitor);
			String actualJcrSQL2 = visitor.getSql2Query();
			Assert.assertEquals(expectedJcrSQL2, actualJcrSQL2);
		}
	}
	
}
