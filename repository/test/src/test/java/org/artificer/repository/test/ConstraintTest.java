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
package org.artificer.repository.test;

import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.common.error.ArtificerConflictException;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.repository.query.PagedResult;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Task;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.TaskEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.TaskTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Brett Meyer.
 */
public class ConstraintTest extends AbstractNoAuditingPersistenceTest {

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
            persistenceManager.deleteArtifact(task.getUuid(), ArtifactType.valueOf(task), false);
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
            persistenceManager.deleteArtifact(task.getUuid(), ArtifactType.valueOf(task), false);
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Clear the relationships.
        actor.getDoes().clear();
        persistenceManager.updateArtifact(actor, ArtifactType.valueOf(task));

        // Deleting the original should now work
        persistenceManager.deleteArtifact(task.getUuid(), ArtifactType.valueOf(task), false);
    }

    @Test
    public void testDeleteWithDerived() throws Exception {
        // Add the goods
        XsdDocument xsd = addXsd();
        WsdlDocument wsdl = addWsdl();

        // Create another artifact with a generic relationship targeting one of the XSD's derived artifacts.
        BaseArtifactType fooArtifact = addWithGenericRelationship();

        // Verify the XSD cannot be deleted
        boolean caught = false;
        try {
            persistenceManager.deleteArtifact(xsd.getUuid(), ArtifactType.valueOf(xsd), false);
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Clear the generic relationship
        persistenceManager.deleteArtifact(fooArtifact.getUuid(), ArtifactType.valueOf(fooArtifact), false);

        // Verify the XSD still cannot be deleted (the WSDL still imports it)
        caught = false;
        try {
            persistenceManager.deleteArtifact(xsd.getUuid(), ArtifactType.valueOf(xsd), false);
        } catch (ArtificerConflictException e) {
            caught = true;
        }
        assertTrue(caught);

        // Delete the WSDL
        persistenceManager.deleteArtifact(wsdl.getUuid(), ArtifactType.valueOf(wsdl), false);

        // Delete the XSD
        persistenceManager.deleteArtifact(xsd.getUuid(), ArtifactType.valueOf(xsd), false);

        // Verify the XSD derived artifacts were deleted (just check one)
        PagedResult<ArtifactSummary> derived = queryManager.createQuery("/s-ramp/xsd/ComplexTypeDeclaration").executeQuery();
        assertEquals(0, derived.getTotalSize());
    }

    @Test
    public void testForceDeleteWithDerived() throws Exception {
        // Add the goods
        XsdDocument xsd = addXsd();
        addWsdl();

        // Create another artifact with a generic relationship targeting one of the XSD's derived artifacts.
        addWithGenericRelationship();

        // Verify the XSD can be force deleted, even though the WSDL imports it
        persistenceManager.deleteArtifact(xsd.getUuid(), ArtifactType.valueOf(xsd), true);

        // Verify the XSD derived artifacts were deleted (just check one)
        PagedResult<ArtifactSummary> derived = queryManager.createQuery("/s-ramp/xsd/ComplexTypeDeclaration").executeQuery();
        assertEquals(0, derived.getTotalSize());
    }

    private XsdDocument addXsd() throws Exception {
        String xsdFileName = "jcr-sample-externalrefs.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + xsdFileName);
        XsdDocument xsd = new XsdDocument();
        xsd.setName(xsdFileName);
        xsd.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        return (XsdDocument) persistenceManager.persistArtifact(xsd, new ArtifactContent(xsdFileName, is));
    }

    private WsdlDocument addWsdl() throws Exception {
        String wsdlFileName = "jcr-sample-externalrefs.wsdl";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/wsdl/" + wsdlFileName);
        WsdlDocument wsdl = new WsdlDocument();
        wsdl.setName(wsdlFileName);
        wsdl.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
        return (WsdlDocument) persistenceManager.persistArtifact(wsdl, new ArtifactContent(wsdlFileName, is));
    }

    private BaseArtifactType addWithGenericRelationship() throws Exception {
        ArtifactSummary complexType = queryManager.createQuery("/s-ramp/xsd/ComplexTypeDeclaration").executeQuery().getResults().get(0);
        BaseArtifactType fooArtifact = ArtifactType.ExtendedArtifactType("FooType", false).newArtifactInstance();
        ArtificerModelUtils.addGenericRelationship(fooArtifact, "fooRelationship", complexType.getUuid());
        return persistenceManager.persistArtifact(fooArtifact, null);
    }
}
