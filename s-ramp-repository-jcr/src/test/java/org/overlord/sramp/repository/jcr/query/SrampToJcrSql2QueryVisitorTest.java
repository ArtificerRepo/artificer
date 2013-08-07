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
			"/s-ramp/xsd/XsdDocument", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 WHERE artifact1.[sramp:artifactModel] = 'xsd'" //$NON-NLS-1$
		},
        {
            "/s-ramp/xsd[@derived = 'true']", //$NON-NLS-1$
            "SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 WHERE artifact1.[sramp:artifactModel] = 'xsd' AND (artifact1.[sramp:derived] = 'true')" //$NON-NLS-1$
        },
		{
			"/s-ramp", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1" //$NON-NLS-1$
		},
        {
            "/s-ramp[@prop1 = 'value1']", //$NON-NLS-1$
            "SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 WHERE (artifact1.[sramp-properties:prop1] = 'value1')" //$NON-NLS-1$
        },
		{
			"/s-ramp/xsd/XsdDocument[@name = 'foo']", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 WHERE artifact1.[sramp:artifactType] = 'XsdDocument' AND (artifact1.[sramp:name] = 'foo')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[@createdBy = 'lincoln73']", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 WHERE artifact1.[sramp:artifactType] = 'XsdDocument' AND (artifact1.[jcr:createdBy] = 'lincoln73')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[@prop1]", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 WHERE artifact1.[sramp:artifactType] = 'XsdDocument' AND (artifact1.[sramp-properties:prop1] IS NOT NULL)" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[@version = '1.0' and @prop1 = 'value1']", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND (artifact1.[version] = '1.0'" + //$NON-NLS-1$
			" AND artifact1.[sramp-properties:prop1] = 'value1')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[@version = '1.0' or @prop1 = 'value1']", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND (artifact1.[version] = '1.0'" + //$NON-NLS-1$
			" OR artifact1.[sramp-properties:prop1] = 'value1')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[@maven.groupId = 'ggg' and @maven.artifactId = 'aaa' and @maven.version = '1.0.0']", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND (artifact1.[sramp-properties:maven.groupId] = 'ggg'" + //$NON-NLS-1$
			" AND artifact1.[sramp-properties:maven.artifactId] = 'aaa'" + //$NON-NLS-1$
			" AND artifact1.[sramp-properties:maven.version] = '1.0.0')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument]", //$NON-NLS-1$
			"SELECT artifact1.*" + //$NON-NLS-1$
			" FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1) " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND (relationship1.[sramp:relationshipType] = 'relatedDocument')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument and includedXsds]", //$NON-NLS-1$
			"SELECT artifact1.*" + //$NON-NLS-1$
			" FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1)" + //$NON-NLS-1$
			" JOIN [sramp:relationship] AS relationship2 ON ISCHILDNODE(relationship2, artifact1) " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND (relationship1.[sramp:relationshipType] = 'relatedDocument' AND relationship2.[sramp:relationshipType] = 'includedXsds')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument[@name = 'foo']]", //$NON-NLS-1$
			"SELECT artifact1.*" + //$NON-NLS-1$
			" FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1) " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND ((relationship1.[sramp:relationshipType] = 'relatedDocument'" + //$NON-NLS-1$
			" AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS artifact2 WHERE artifact2.[sramp:name] = 'foo')))" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument[@name = 'foo'] and importedBy[@uuid = '12345']]", //$NON-NLS-1$
			"SELECT artifact1.*" + //$NON-NLS-1$
			" FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1)" + //$NON-NLS-1$
			" JOIN [sramp:relationship] AS relationship2 ON ISCHILDNODE(relationship2, artifact1) " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND ((relationship1.[sramp:relationshipType] = 'relatedDocument'" + //$NON-NLS-1$
			" AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS artifact2 WHERE artifact2.[sramp:name] = 'foo'))" + //$NON-NLS-1$
			" AND (relationship2.[sramp:relationshipType] = 'importedBy'" + //$NON-NLS-1$
			" AND relationship2.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS artifact3 WHERE artifact3.[sramp:uuid] = '12345')))" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument[@name = 'foo'] or importedBy[@uuid = '12345']]", //$NON-NLS-1$
			"SELECT artifact1.*" + //$NON-NLS-1$
			" FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1)" + //$NON-NLS-1$
			" JOIN [sramp:relationship] AS relationship2 ON ISCHILDNODE(relationship2, artifact1) " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND ((relationship1.[sramp:relationshipType] = 'relatedDocument'" + //$NON-NLS-1$
			" AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS artifact2 WHERE artifact2.[sramp:name] = 'foo'))" + //$NON-NLS-1$
			" OR (relationship2.[sramp:relationshipType] = 'importedBy'" + //$NON-NLS-1$
			" AND relationship2.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS artifact3 WHERE artifact3.[sramp:uuid] = '12345')))" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:exactlyClassifiedByAllOf(., '#China')]", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument' AND (artifact1.[sramp:classifiedBy] = '#China')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:exactlyClassifiedByAllOf(., '#Spicy', '#Sweet')]", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND ((artifact1.[sramp:classifiedBy] = '#Spicy' AND artifact1.[sramp:classifiedBy] = '#Sweet'))" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:exactlyClassifiedByAnyOf(., '#China')]", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument' AND (artifact1.[sramp:classifiedBy] = '#China')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:exactlyClassifiedByAnyOf(., '#Spicy', '#Sweet')]", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND ((artifact1.[sramp:classifiedBy] = '#Spicy' OR artifact1.[sramp:classifiedBy] = '#Sweet'))" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[s-ramp:classifiedByAnyOf(., '#Spicy', '#Sweet')]", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1 " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND ((artifact1.[sramp:normalizedClassifiedBy] = '#Spicy' OR artifact1.[sramp:normalizedClassifiedBy] = '#Sweet'))" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[xp2:matches(@name, '.*account.*')]", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
			" WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND (artifact1.[sramp:name] LIKE '%account%')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[xp2:matches(@description, 'Hello.*')]", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
			" WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND (artifact1.[sramp:description] LIKE 'Hello%')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[xp2:matches(@description, 'Hello.*') and xp2:matches(@version, '.*')]", //$NON-NLS-1$
			"SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
			" WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND (artifact1.[sramp:description] LIKE 'Hello%' AND artifact1.[version] LIKE '%')" //$NON-NLS-1$
		},
		{
			"/s-ramp/xsd/XsdDocument[relatedDocument[fn:matches(@name, 'fo.*')]]", //$NON-NLS-1$
			"SELECT artifact1.*" + //$NON-NLS-1$
			" FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
			" JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1) " + //$NON-NLS-1$
			"WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
			" AND ((relationship1.[sramp:relationshipType] = 'relatedDocument'" + //$NON-NLS-1$
			" AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS artifact2 WHERE artifact2.[sramp:name] LIKE 'fo%')))" //$NON-NLS-1$
		},
        {
            "/s-ramp/wsdl/PortType[@name = 'OrderServicePT']/operation", //$NON-NLS-1$
            "SELECT artifact2.*" + //$NON-NLS-1$
            " FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
            " JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1)" + //$NON-NLS-1$
            " JOIN [sramp:baseArtifactType] AS artifact2 ON relationship1.[sramp:relationshipTarget] = artifact2.[jcr:uuid] " + //$NON-NLS-1$
            "WHERE artifact1.[sramp:artifactType] = 'PortType'" + //$NON-NLS-1$
            " AND (artifact1.[sramp:name] = 'OrderServicePT')" + //$NON-NLS-1$
            " AND relationship1.[sramp:relationshipType] = 'operation'" //$NON-NLS-1$
        },
        {
            "/s-ramp/wsdl/PortType[@name = 'OrderServicePT']/operation[@name = 'newOrder']", //$NON-NLS-1$
            "SELECT artifact2.*" + //$NON-NLS-1$
            " FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
            " JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1)" + //$NON-NLS-1$
            " JOIN [sramp:baseArtifactType] AS artifact2 ON relationship1.[sramp:relationshipTarget] = artifact2.[jcr:uuid] " + //$NON-NLS-1$
            "WHERE artifact1.[sramp:artifactType] = 'PortType'" + //$NON-NLS-1$
            " AND (artifact1.[sramp:name] = 'OrderServicePT')" + //$NON-NLS-1$
            " AND relationship1.[sramp:relationshipType] = 'operation'" + //$NON-NLS-1$
            " AND (artifact2.[sramp:name] = 'newOrder')" //$NON-NLS-1$
        },
        {
            "/s-ramp/wsdl/PortType[relatedDocument[@name = 'OrderServicePT']]/operation[@name = 'newOrder']", //$NON-NLS-1$
            "SELECT artifact3.*" + //$NON-NLS-1$
            " FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
            " JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1)" + //$NON-NLS-1$
            " JOIN [sramp:relationship] AS relationship2 ON ISCHILDNODE(relationship2, artifact1)" + //$NON-NLS-1$
            " JOIN [sramp:baseArtifactType] AS artifact3 ON relationship2.[sramp:relationshipTarget] = artifact3.[jcr:uuid] " + //$NON-NLS-1$
            "WHERE artifact1.[sramp:artifactType] = 'PortType'" + //$NON-NLS-1$
            " AND ((relationship1.[sramp:relationshipType] = 'relatedDocument' AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS artifact2 WHERE artifact2.[sramp:name] = 'OrderServicePT')))" + //$NON-NLS-1$
            " AND relationship2.[sramp:relationshipType] = 'operation'" + //$NON-NLS-1$
            " AND (artifact3.[sramp:name] = 'newOrder')" //$NON-NLS-1$
        },
        {
            "/s-ramp/xsd/XsdDocument[xp2:not(@prop1)]", //$NON-NLS-1$
            "SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
            " WHERE artifact1.[sramp:artifactType] = 'XsdDocument' AND (NOT (artifact1.[sramp-properties:prop1] IS NOT NULL))" //$NON-NLS-1$
        },
        {
            "/s-ramp/xsd/XsdDocument[xp2:not(@name = 'foo')]", //$NON-NLS-1$
            "SELECT artifact1.* FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
            " WHERE artifact1.[sramp:artifactType] = 'XsdDocument' AND (NOT (artifact1.[sramp:name] = 'foo'))" //$NON-NLS-1$
        },
        {
            "/s-ramp/xsd/XsdDocument[xp2:not(relatedDocument)]", //$NON-NLS-1$
            "SELECT artifact1.*" + //$NON-NLS-1$
            " FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
            " JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1) " + //$NON-NLS-1$
            "WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
            " AND (NOT (relationship1.[sramp:relationshipType] = 'relatedDocument'))" //$NON-NLS-1$
        },
        {
            "/s-ramp/xsd/XsdDocument[xp2:not(relatedDocument[@name = 'foo'])]", //$NON-NLS-1$
            "SELECT artifact1.*" + //$NON-NLS-1$
            " FROM [sramp:baseArtifactType] AS artifact1" + //$NON-NLS-1$
            " JOIN [sramp:relationship] AS relationship1 ON ISCHILDNODE(relationship1, artifact1) " + //$NON-NLS-1$
            "WHERE artifact1.[sramp:artifactType] = 'XsdDocument'" + //$NON-NLS-1$
            " AND (NOT ((relationship1.[sramp:relationshipType] = 'relatedDocument'" + //$NON-NLS-1$
            " AND relationship1.[sramp:relationshipTarget] IN (SELECT [jcr:uuid] FROM [sramp:baseArtifactType] AS artifact2 WHERE artifact2.[sramp:name] = 'foo'))))" //$NON-NLS-1$
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
						uris.add(new URI("#AdditionalNormalizedClassification")); //$NON-NLS-1$
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
						uris.add(new URI("#AdditionalNormalizedClassification")); //$NON-NLS-1$
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
