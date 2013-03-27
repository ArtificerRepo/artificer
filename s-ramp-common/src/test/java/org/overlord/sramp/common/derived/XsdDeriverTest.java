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
package org.overlord.sramp.common.derived;

import java.io.InputStream;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

/**
 * Unit test for the {@link XsdDeriver} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class XsdDeriverTest {

	private static final Set<String> EXPECTED_ELEMENT_NAMES = set("excludedOwners", "taskStakeholders",
			"recipients", "task", "leanTask", "potentialOwners", "businessAdministrators", "tasks",
			"logicalPeopleGroups", "genericHumanRole", "notifications", "priority", "taskInitiator",
			"notification", "import", "peopleAssignments", "humanInteractions");
	private static final Set<String> EXPECTED_ATTRIBUTE_NAMES = set();
	private static final Set<String> EXPECTED_SIMPLE_TYPE_NAMES = set("tPotentialDelegatees", "tPattern",
			"tRoutingPatternType", "tBoolean", "tCompositionType");
	private static final Set<String> EXPECTED_COMPLEX_TYPE_NAMES = set("tFrom", "tAggregate",
			"tMessageSchema", "tToPart", "tText", "tComposition", "tParameter", "tPeopleAssignments",
			"tExtension", "tRendering", "tPriority-expr", "tTask", "tReassignment", "tNotification",
			"tMessageField", "tDeadlines", "tCopy", "tDocumentation", "tDuration-expr",
			"tNotificationInterface", "tCompletion", "tExtensibleElements", "tHumanInteractions",
			"tTaskInterface", "tPotentialOwnerAssignment", "tNotifications", "tExtensions",
			"tPresentationParameter", "tPresentationElements", "tDeadline-expr", "tTasks",
			"tLogicalPeopleGroups", "tDescription", "tQuery", "tLocalNotification", "tLogicalPeopleGroup",
			"tSubtask", "tExtensibleMixedContentElements", "tSequence", "tExpression", "tToParts", "tResult",
			"tRenderings", "tGenericHumanRoleAssignmentBase", "tParallel", "tDeadline",
			"tPresentationParameters", "tArgument", "tMessageChoice", "tDelegation", "tLocalTask",
			"tBoolean-expr", "tEscalation", "tLeanTask", "tImport", "tDefaultCompletion", "tMessageDisplay",
			"tCompletionBehavior", "tGenericHumanRoleAssignment", "tLiteral", "tTaskBase");

	/**
	 * Test method for {@link org.overlord.sramp.common.repository.derived.XsdDeriver#derive(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.io.InputStream)}.
	 */
	@Test
	public void testDerive() throws Exception {
		DatatypeFactory dtFactory = DatatypeFactory.newInstance();

		XsdDeriver deriver = new XsdDeriver();
		XsdDocument testSrcArtifact = new XsdDocument();
		testSrcArtifact.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
		testSrcArtifact.setUuid(UUID.randomUUID().toString());
		testSrcArtifact.setName("ws-humantask.xsd");
		testSrcArtifact.setVersion("1.0");
		testSrcArtifact.setContentEncoding("UTF-8");
		testSrcArtifact.setContentType("application/xml");
		testSrcArtifact.setContentSize(31723L);
		testSrcArtifact.setCreatedBy("anonymous");
		XMLGregorianCalendar xmlGC = dtFactory.newXMLGregorianCalendar(new GregorianCalendar());
		testSrcArtifact.setCreatedTimestamp(xmlGC);
		testSrcArtifact.setDescription("Hello world.");
		testSrcArtifact.setLastModifiedBy("anonymous");
		testSrcArtifact.setLastModifiedTimestamp(xmlGC);

		InputStream testSrcContent = null;
		try {
			testSrcContent = getClass().getResourceAsStream("/sample-files/xsd/ws-humantask.xsd");
			Collection<BaseArtifactType> derivedArtifacts = deriver.derive(testSrcArtifact, testSrcContent);
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
