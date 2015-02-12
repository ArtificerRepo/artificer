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
package org.artificer.integration.artifactbuilder;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.*;
import org.artificer.common.ArtifactContent;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.InputStream;
import java.util.*;

/**
 * Unit test for the {@link XsdDocumentArtifactBuilder} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class XsdDocumentArtifactBuilderTest {

	private static final Set<String> EXPECTED_ELEMENT_NAMES = set("excludedOwners", "taskStakeholders", //$NON-NLS-1$ //$NON-NLS-2$
			"recipients", "task", "leanTask", "potentialOwners", "businessAdministrators", "tasks", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"logicalPeopleGroups", "genericHumanRole", "notifications", "priority", "taskInitiator", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"notification", "import", "peopleAssignments", "humanInteractions"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final Set<String> EXPECTED_ATTRIBUTE_NAMES = set();
	private static final Set<String> EXPECTED_SIMPLE_TYPE_NAMES = set("tPotentialDelegatees", "tPattern", //$NON-NLS-1$ //$NON-NLS-2$
			"tRoutingPatternType", "tBoolean", "tCompositionType"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final Set<String> EXPECTED_COMPLEX_TYPE_NAMES = set("tFrom", "tAggregate", //$NON-NLS-1$ //$NON-NLS-2$
			"tMessageSchema", "tToPart", "tText", "tComposition", "tParameter", "tPeopleAssignments", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"tExtension", "tRendering", "tPriority-expr", "tTask", "tReassignment", "tNotification", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"tMessageField", "tDeadlines", "tCopy", "tDocumentation", "tDuration-expr", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"tNotificationInterface", "tCompletion", "tExtensibleElements", "tHumanInteractions", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"tTaskInterface", "tPotentialOwnerAssignment", "tNotifications", "tExtensions", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"tPresentationParameter", "tPresentationElements", "tDeadline-expr", "tTasks", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"tLogicalPeopleGroups", "tDescription", "tQuery", "tLocalNotification", "tLogicalPeopleGroup", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"tSubtask", "tExtensibleMixedContentElements", "tSequence", "tExpression", "tToParts", "tResult", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"tRenderings", "tGenericHumanRoleAssignmentBase", "tParallel", "tDeadline", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"tPresentationParameters", "tArgument", "tMessageChoice", "tDelegation", "tLocalTask", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"tBoolean-expr", "tEscalation", "tLeanTask", "tImport", "tDefaultCompletion", "tMessageDisplay", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"tCompletionBehavior", "tGenericHumanRoleAssignment", "tLiteral", "tTaskBase"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	@Test
	public void testDerive() throws Exception {
		DatatypeFactory dtFactory = DatatypeFactory.newInstance();

		XsdDocumentArtifactBuilder builder = new XsdDocumentArtifactBuilder();
		XsdDocument testSrcArtifact = new XsdDocument();
		testSrcArtifact.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
		testSrcArtifact.setUuid(UUID.randomUUID().toString());
		testSrcArtifact.setName("ws-humantask.xsd"); //$NON-NLS-1$
		testSrcArtifact.setVersion("1.0"); //$NON-NLS-1$
		testSrcArtifact.setContentEncoding("UTF-8"); //$NON-NLS-1$
		testSrcArtifact.setContentType("application/xml"); //$NON-NLS-1$
		testSrcArtifact.setContentSize(31723L);
		testSrcArtifact.setCreatedBy("anonymous"); //$NON-NLS-1$
		XMLGregorianCalendar xmlGC = dtFactory.newXMLGregorianCalendar(new GregorianCalendar());
		testSrcArtifact.setCreatedTimestamp(xmlGC);
		testSrcArtifact.setDescription("Hello world."); //$NON-NLS-1$
		testSrcArtifact.setLastModifiedBy("anonymous"); //$NON-NLS-1$
		testSrcArtifact.setLastModifiedTimestamp(xmlGC);

		InputStream testSrcContent = null;
		try {
			testSrcContent = getClass().getResourceAsStream("/sample-files/xsd/ws-humantask.xsd"); //$NON-NLS-1$
			Collection<BaseArtifactType> derivedArtifacts = builder.buildArtifacts(testSrcArtifact,
                    new ArtifactContent("ws-humantask.xsd", testSrcContent)).getDerivedArtifacts();
            Assert.assertNotNull(derivedArtifacts);
			Assert.assertEquals(83, derivedArtifacts.size());
			int numElements = 0;
			int numAttributes = 0;
			int numSimpleTypes = 0;
			int numComplexTypes = 0;
			Set<String> elementNames = new HashSet<String>();
			Set<String> attributeNames = new HashSet<String>();
			Set<String> simpleTypeNames = new HashSet<String>();
			Set<String> complexTypeNames = new HashSet<String>();
			for (BaseArtifactType derivedArtifact : derivedArtifacts) {
			    DerivedArtifactType dat = (DerivedArtifactType) derivedArtifact;
				Assert.assertEquals(testSrcArtifact.getUuid(), dat.getRelatedDocument().getValue());
				Assert.assertEquals(DocumentArtifactEnum.XSD_DOCUMENT, dat.getRelatedDocument().getArtifactType());

				if (dat instanceof ElementDeclaration) {
					numElements++;
					elementNames.add(((ElementDeclaration) dat).getNCName());
				} else if (dat instanceof AttributeDeclaration) {
					numAttributes++;
					attributeNames.add(((AttributeDeclaration) dat).getNCName());
				} else if (dat instanceof SimpleTypeDeclaration) {
					numSimpleTypes++;
					simpleTypeNames.add(((SimpleTypeDeclaration) dat).getNCName());
				} else if (dat instanceof ComplexTypeDeclaration) {
					numComplexTypes++;
					complexTypeNames.add(((ComplexTypeDeclaration) dat).getNCName());
				}
			}
			// Verify the counts
			Assert.assertEquals(17, numElements);
			Assert.assertEquals(0, numAttributes);
			Assert.assertEquals(5, numSimpleTypes);
			Assert.assertEquals(61, numComplexTypes);
			Assert.assertEquals(83, numElements + numAttributes + numSimpleTypes + numComplexTypes);

			// Verify the names
			Assert.assertEquals(EXPECTED_ELEMENT_NAMES, elementNames);
			Assert.assertEquals(EXPECTED_ATTRIBUTE_NAMES, attributeNames);
			Assert.assertEquals(EXPECTED_SIMPLE_TYPE_NAMES, simpleTypeNames);
			Assert.assertEquals(EXPECTED_COMPLEX_TYPE_NAMES, complexTypeNames);
		} finally {
			IOUtils.closeQuietly(testSrcContent);
		}
	}

	/**
	 * Make a set from values.
	 * @param values
	 */
	private static Set<String> set(String ... values) {
		Set<String> rval = new HashSet<String>();
		for (String val : values) {
			rval.add(val);
		}
		return rval;
	}

}
