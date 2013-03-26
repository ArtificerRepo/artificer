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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Assert;

import org.junit.Test;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampServerException;
import org.overlord.sramp.common.query.xpath.XPathParser;
import org.overlord.sramp.common.query.xpath.ast.Query;
import org.overlord.sramp.repository.jcr.ClassificationHelper;


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
            "/s-ramp/xsd[@derived = 'true']",
            "SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact WHERE artifact.[sramp:artifactModel] = 'xsd' AND (artifact.[sramp:derived] = 'true')"
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
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:exactlyClassifiedByAllOf(., '#China')]",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument' AND (artifact.[sramp:classifiedBy] = '#China')"
		},
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:exactlyClassifiedByAllOf(., '#Spicy', '#Sweet')]",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND ((artifact.[sramp:classifiedBy] = '#Spicy' AND artifact.[sramp:classifiedBy] = '#Sweet'))"
		},
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:exactlyClassifiedByAnyOf(., '#China')]",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument' AND (artifact.[sramp:classifiedBy] = '#China')"
		},
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:exactlyClassifiedByAnyOf(., '#Spicy', '#Sweet')]",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND ((artifact.[sramp:classifiedBy] = '#Spicy' OR artifact.[sramp:classifiedBy] = '#Sweet'))"
		},
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:classifiedByAnyOf(., '#Spicy', '#Sweet')]",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND ((artifact.[sramp:normalizedClassifiedBy] = '#Spicy' OR artifact.[sramp:normalizedClassifiedBy] = '#Sweet'))"
		},
		{
			"/s-ramp/xsd/XsdDocument[xp2:matches(@name, '.*account.*')]",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact" +
			" WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND (artifact.[sramp:name] LIKE '%account%')"
		},
		{
			"/s-ramp/xsd/XsdDocument[xp2:matches(@description, 'Hello.*')]",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact" +
			" WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND (artifact.[sramp:description] LIKE 'Hello%')"
		},
		{
			"/s-ramp/xsd/XsdDocument[xp2:matches(@description, 'Hello.*') and xp2:matches(@version, '.*')]",
			"SELECT artifact.* FROM [sramp:baseArtifactType] AS artifact" +
			" WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND (artifact.[sramp:description] LIKE 'Hello%' AND artifact.[version] LIKE '%')"
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument[fn:matches(@name, 'fo.*')]]",
			"SELECT artifact.*" +
			" FROM [sramp:baseArtifactType] AS artifact" +
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact) " +
			"WHERE artifact.[sramp:artifactType] = 'XsdDocument'" +
			" AND ((relationship1.[sramp:relationshipType] = 'relatedDocument'" +
			" AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS target WHERE target.[sramp:name] LIKE 'fo%')))"
		},
	};

	/**
	 * Tests the visitor.
	 * @throws SrampException
	 */
	@Test
	public void testVisitor() throws SrampException {
		for (String[] testCase : TEST_DATA) {
			String srampXpath = testCase[0];
			String expectedJcrSQL2 = testCase[1];
			SrampToJcrSql2QueryVisitor visitor = new SrampToJcrSql2QueryVisitor(new ClassificationHelper() {
				@Override
				public Collection<URI> resolveAll(Collection<String> classifiedBy) throws SrampException {
					Collection<URI> uris = new HashSet<URI>();
					for (String c : classifiedBy) {
						uris.add(resolve(c));
					}
					return uris;
				}

				@Override
				public URI resolve(String classifiedBy) throws SrampException {
					try {
						return new URI(classifiedBy);
					} catch (URISyntaxException e) {
						throw new SrampServerException(e);
					}
				}

				@Override
				public Collection<URI> normalizeAll(Collection<URI> classifications) throws SrampException {
					try {
						Collection<URI> uris = new HashSet<URI>();
						uris.addAll(classifications);
						uris.add(new URI("#AdditionalNormalizedClassification"));
						return uris;
					} catch (URISyntaxException e) {
						throw new SrampServerException(e);
					}
				}

				@Override
				public Collection<URI> normalize(URI classification) throws SrampException {
					try {
						Collection<URI> uris = new HashSet<URI>();
						uris.add(classification);
						uris.add(new URI("#AdditionalNormalizedClassification"));
						return uris;
					} catch (URISyntaxException e) {
						throw new SrampServerException(e);
					}
				}
			});
			XPathParser parser = new XPathParser();
			Query srampQuery = parser.parseXPath(srampXpath);
			srampQuery.accept(visitor);
			String actualJcrSQL2 = visitor.getSql2Query();
			Assert.assertEquals(expectedJcrSQL2, actualJcrSQL2);
		}
	}

}
