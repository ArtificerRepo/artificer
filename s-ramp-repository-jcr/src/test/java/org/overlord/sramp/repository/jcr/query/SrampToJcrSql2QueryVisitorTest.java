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
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact WHERE artifact.[sramp:artifactType] = 'XsdDocument'"
		},
		{
			"/s-ramp/xsd",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact WHERE artifact.[sramp:artifactModel] = 'xsd'"
		},
		{
			"/s-ramp",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact WHERE artifact.[sramp:artifactModel] LIKE '%'"
		},
		{
			"/s-ramp/xsd/XsdDocument[@name = 'foo']",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact WHERE artifact.[sramp:artifactType] = 'XsdDocument' AND (artifact.[sramp:name] = 'foo')"
		},
		{
			"/s-ramp/xsd/XsdDocument[@createdBy = 'lincoln73']",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact WHERE artifact.[sramp:artifactType] = 'XsdDocument' AND (artifact.[jcr:createdBy] = 'lincoln73')"
		},
		{
			"/s-ramp/xsd/XsdDocument[@prop1]",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact WHERE artifact.[sramp:artifactType] = 'XsdDocument' AND (artifact.[sramp-properties:prop1] LIKE '%')"
		},
		{
			"/s-ramp/xsd/XsdDocument[@version = '1.0' and @prop1 = 'value1']",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND (artifact.[version] = '1.0'" +
			" AND artifact.[sramp-properties:prop1] = 'value1')"
		},
		{
			"/s-ramp/xsd/XsdDocument[@version = '1.0' or @prop1 = 'value1']",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND (artifact.[version] = '1.0'" +
			" OR artifact.[sramp-properties:prop1] = 'value1')"
		},
		{
			"/s-ramp/xsd/XsdDocument[@maven.groupId = 'ggg' and @maven.artifactId = 'aaa' and @maven.version = '1.0.0']",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND (artifact.[sramp-properties:maven.groupId] = 'ggg'" +
			" AND artifact.[sramp-properties:maven.artifactId] = 'aaa'" +
			" AND artifact.[sramp-properties:maven.version] = '1.0.0')"
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument]",
			"SELECT artifact.*" +
			" FROM [sramp:baseArtifactType] AS artifact" +
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact) " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND (relationship1.[sramp:relationshipType] = 'relatedDocument')"
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument and includedXsds]",
			"SELECT artifact.*" +
			" FROM [sramp:baseArtifactType] AS artifact" +
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact)" +
			" JOIN [sramp:relationship] AS relationship2 ON ISCHILDNODE(relationship2, artifact) " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND (relationship1.[sramp:relationshipType] = 'relatedDocument' AND relationship2.[sramp:relationshipType] = 'includedXsds')"
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument[@name = 'foo']]",
			"SELECT artifact.*" +
			" FROM [sramp:baseArtifactType] AS artifact" +
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact) " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND ((relationship1.[sramp:relationshipType] = 'relatedDocument'" +
			" AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS target WHERE target.[sramp:name] = 'foo')))"
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument[@name = 'foo'] and importedBy[@uuid = '12345']]",
			"SELECT artifact.*" +
			" FROM [sramp:baseArtifactType] AS artifact" +
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact)" +
			" JOIN [sramp:relationship] AS relationship2 ON ISCHILDNODE(relationship2, artifact) " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND ((relationship1.[sramp:relationshipType] = 'relatedDocument'" +
			" AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS target WHERE target.[sramp:name] = 'foo'))" +
			" AND (relationship2.[sramp:relationshipType] = 'importedBy'" +
			" AND relationship2.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS target WHERE target.[sramp:uuid] = '12345')))"
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument[@name = 'foo'] or importedBy[@uuid = '12345']]",
			"SELECT artifact.*" +
			" FROM [sramp:baseArtifactType] AS artifact" +
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact)" +
			" JOIN [sramp:relationship] AS relationship2 ON ISCHILDNODE(relationship2, artifact) " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND ((relationship1.[sramp:relationshipType] = 'relatedDocument'" +
			" AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS target WHERE target.[sramp:name] = 'foo'))" +
			" OR (relationship2.[sramp:relationshipType] = 'importedBy'" +
			" AND relationship2.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS target WHERE target.[sramp:uuid] = '12345')))"
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
