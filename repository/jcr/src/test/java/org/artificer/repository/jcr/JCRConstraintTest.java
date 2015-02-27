/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.repository.jcr;

import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerException;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.common.error.ArtificerConflictException;
import org.artificer.common.ontology.ArtificerOntology;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Task;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.TaskEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.TaskTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

import java.io.InputStream;
import java.util.Iterator;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Brett Meyer.
 */
public class JCRConstraintTest extends AbstractNoAuditingJCRPersistenceTest {

    @Test
    public void testDelete() throws Exception {
        // Add an artifact
        Task task = new Task();
        task.setArtifactType(BaseArtifactEnum.TASK);
        task = (Task) persistenceManager.persistArtifact(task, null);

        // Create another artifact with a generic relationship targeting the original.
        Actor actor = new Actor();
        actor.setArtifactType(BaseArtifactEnum.ACTOR);
        ArtificerModelUtils.addGenericRelationship(actor, "fooRelationship", task.getUuid());
        actor = (Actor) persistenceManager.persistArtifact(actor, null);

        // Verify deleting the original fails
        boolean caught = false;
        try {
            persistenceManager.deleteArtifact(task.getUuid(), ArtifactType.valueOf(task));
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Clear the relationships and add a modeled one.
        actor.getRelationship().clear();
        TaskTarget taskTarget = new TaskTarget();
        taskTarget.setArtifactType(TaskEnum.TASK);
        taskTarget.setValue(task.getUuid());
        actor.getDoes().add(taskTarget);
        persistenceManager.updateArtifact(actor, ArtifactType.valueOf(task));

        // Verify deleting the original fails
        caught = false;
        try {
            persistenceManager.deleteArtifact(task.getUuid(), ArtifactType.valueOf(task));
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Clear the relationships.
        actor.getDoes().clear();
        persistenceManager.updateArtifact(actor, ArtifactType.valueOf(task));

        // Deleting the original should now work
        persistenceManager.deleteArtifact(task.getUuid(), ArtifactType.valueOf(task));
    }

    @Test
    public void testDeleteWithDerived() throws Exception {
        // Add the goods
        String xsdFileName = "jcr-sample-externalrefs.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + xsdFileName);
        XsdDocument xsd = new XsdDocument();
        xsd.setName(xsdFileName);
        xsd.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        xsd = (XsdDocument) persistenceManager.persistArtifact(xsd, new ArtifactContent(xsdFileName, is));
        String wsdlFileName = "jcr-sample-externalrefs.wsdl";
        is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + wsdlFileName);
        WsdlDocument wsdl = new WsdlDocument();
        wsdl.setName(wsdlFileName);
        wsdl.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
        wsdl = (WsdlDocument) persistenceManager.persistArtifact(wsdl, new ArtifactContent(wsdlFileName, is));
        assertEquals(1, wsdl.getImportedXsds().size());

        // Create another artifact with a generic relationship targeting one of the XSD's derived artifacts.
        BaseArtifactType complexType = queryManager.createQuery("/s-ramp/xsd/ComplexTypeDeclaration").executeQuery().iterator().next();
        BaseArtifactType fooArtifact = ArtifactType.ExtendedArtifactType("FooType", false).newArtifactInstance();
        ArtificerModelUtils.addGenericRelationship(fooArtifact, "fooRelationship", complexType.getUuid());
        fooArtifact = persistenceManager.persistArtifact(fooArtifact, null);

        // Verify the XSD cannot be deleted
        boolean caught = false;
        try {
            persistenceManager.deleteArtifact(xsd.getUuid(), ArtifactType.valueOf(xsd));
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Clear the generic relationship
        persistenceManager.deleteArtifact(fooArtifact.getUuid(), ArtifactType.valueOf(fooArtifact));

        // Delete the XSD
        persistenceManager.deleteArtifact(xsd.getUuid(), ArtifactType.valueOf(xsd));

        // Re-obtain the WSDL and verify the derived relationship was automatically deleted
        wsdl = (WsdlDocument) getArtifactByUUID(wsdl.getUuid());
        assertEquals(0, wsdl.getImportedXsds().size());
    }

    @Test
    public void testUpdateContent() throws Exception {
        // Add the goods
        String xsdFileName = "jcr-sample-externalrefs.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + xsdFileName);
        XsdDocument xsd = new XsdDocument();
        xsd.setName(xsdFileName);
        xsd.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        xsd = (XsdDocument) persistenceManager.persistArtifact(xsd, new ArtifactContent(xsdFileName, is));

        // Grab one of the derived artifacts, to be used later
        BaseArtifactType complexType = queryManager.createQuery("/s-ramp/xsd/ComplexTypeDeclaration").executeQuery().iterator().next();

        // Create a generic relationship on the primary artifact targeting the derived artifact, to be used later.
        ArtificerModelUtils.addGenericRelationship(xsd, "fooRelationship", complexType.getUuid());
        persistenceManager.updateArtifact(xsd, ArtifactType.valueOf(xsd));

        // Create another artifact with a generic relationship targeting one of the XSD's derived artifacts.
        BaseArtifactType fooArtifact = ArtifactType.ExtendedArtifactType("FooType", false).newArtifactInstance();
        ArtificerModelUtils.addGenericRelationship(fooArtifact, "fooRelationship", complexType.getUuid());
        fooArtifact = persistenceManager.persistArtifact(fooArtifact, null);

        // Verify the XSD cannot be updated
        boolean caught = false;
        try {
            is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + xsdFileName);
            persistenceManager.updateArtifactContent(xsd.getUuid(), ArtifactType.valueOf(xsd), new ArtifactContent(xsdFileName, is));
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Clear the generic relationship
        persistenceManager.deleteArtifact(fooArtifact.getUuid(), ArtifactType.valueOf(fooArtifact));

        // Add a custom property to one of the XSD's derived artifacts
        Property property = new Property();
        property.setPropertyName("fooName");
        property.setPropertyValue("fooValue");
        complexType.getProperty().add(property);
        persistenceManager.updateArtifact(complexType, ArtifactType.valueOf(complexType));

        // Verify the XSD cannot be updated
        caught = false;
        try {
            is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + xsdFileName);
            persistenceManager.updateArtifactContent(xsd.getUuid(), ArtifactType.valueOf(xsd), new ArtifactContent(xsdFileName, is));
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Clear the custom property
        complexType.getProperty().clear();

        // Add a classifier to one of the XSD's derived artifacts.
        ArtificerOntology ontology = createOntology();
        complexType.getClassifiedBy().add(ontology.findClass("World").getUri().toString());
        persistenceManager.updateArtifact(complexType, ArtifactType.valueOf(complexType));

        // Verify the XSD cannot be updated
        caught = false;
        try {
            is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + xsdFileName);
            persistenceManager.updateArtifactContent(xsd.getUuid(), ArtifactType.valueOf(xsd), new ArtifactContent(xsdFileName, is));
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Clear the classifier
        complexType.getClassifiedBy().clear();
        persistenceManager.updateArtifact(complexType, ArtifactType.valueOf(complexType));

        // Now update the content, for reals
        is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + xsdFileName);
        persistenceManager.updateArtifactContent(xsd.getUuid(), ArtifactType.valueOf(xsd), new ArtifactContent(xsdFileName, is));

        // Verify the derived artifacts were re-generated
        Iterator<BaseArtifactType> complexTypes = queryManager.createQuery("/s-ramp/xsd/ComplexTypeDeclaration")
                .executeQuery().iterator();
        assertTrue(complexTypes.hasNext());
        while (complexTypes.hasNext()) {
            BaseArtifactType newComplexType = complexTypes.next();
            assertNotEquals(complexType.getUuid(), newComplexType);
        }

        // Verify the XSD's generic relationship was removed
        xsd = (XsdDocument) getArtifactByUUID(xsd.getUuid());
        assertEquals(0, xsd.getRelationship().size());
    }

    @Test
    public void testDeleteContent() throws Exception {
        // Add the goods
        String xsdFileName = "jcr-sample-externalrefs.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + xsdFileName);
        XsdDocument xsd = new XsdDocument();
        xsd.setName(xsdFileName);
        xsd.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        xsd = (XsdDocument) persistenceManager.persistArtifact(xsd, new ArtifactContent(xsdFileName, is));

        // Grab one of the derived artifacts, to be used later
        BaseArtifactType complexType = queryManager.createQuery("/s-ramp/xsd/ComplexTypeDeclaration").executeQuery().iterator().next();

        // Create a generic relationship on the primary artifact targeting the derived artifact, to be used later.
        ArtificerModelUtils.addGenericRelationship(xsd, "fooRelationship", complexType.getUuid());
        persistenceManager.updateArtifact(xsd, ArtifactType.valueOf(xsd));

        // Create another artifact with a generic relationship targeting one of the XSD's derived artifacts.
        BaseArtifactType fooArtifact = ArtifactType.ExtendedArtifactType("FooType", false).newArtifactInstance();
        ArtificerModelUtils.addGenericRelationship(fooArtifact, "fooRelationship", complexType.getUuid());
        fooArtifact = persistenceManager.persistArtifact(fooArtifact, null);

        // Verify the XSD content cannot be deleted
        boolean caught = false;
        try {
            persistenceManager.deleteArtifactContent(xsd.getUuid(), ArtifactType.valueOf(xsd));
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Clear the generic relationship
        persistenceManager.deleteArtifact(fooArtifact.getUuid(), ArtifactType.valueOf(fooArtifact));

        // Now delete the content, for reals
        persistenceManager.deleteArtifactContent(xsd.getUuid(), ArtifactType.valueOf(xsd));

        // Verify the derived artifacts were deleted
        Iterator<BaseArtifactType> complexTypes = queryManager.createQuery("/s-ramp/xsd/ComplexTypeDeclaration")
                .executeQuery().iterator();
        assertFalse(complexTypes.hasNext());

        // Verify the XSD's generic relationship was removed
        xsd = (XsdDocument) getArtifactByUUID(xsd.getUuid());
        assertEquals(0, xsd.getRelationship().size());
    }

    private ArtificerOntology createOntology() throws ArtificerException {
        ArtificerOntology ontology = new ArtificerOntology();
        ontology.setBase("urn:example.org/test");
        ontology.setLabel("Test Ontology");
        ontology.setComment("This is my test ontology.");

        ArtificerOntology.ArtificerOntologyClass world = ontology.createClass("World");
        world.setParent(null);
        world.setComment("The entire world");
        world.setLabel("World");

        ontology.getRootClasses().add(world);

        return persistenceManager.persistOntology(ontology);
    }
}
