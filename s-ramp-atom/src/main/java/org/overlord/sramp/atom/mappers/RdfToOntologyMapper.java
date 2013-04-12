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
package org.overlord.sramp.atom.mappers;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;
import org.w3._2002._07.owl_.Ontology;

/**
 * Maps RDF formatted data onto an S-RAMP ontology model.
 *
 * @author eric.wittmann@redhat.com
 */
public class RdfToOntologyMapper {

    private static final RdfToOntologyMapper instance = new RdfToOntologyMapper();

    /**
     * Converts an RDF to an S-RAMP Ontology.
     * @param rdf
     * @throws Exception
     */
    public static SrampOntology rdf2ontology(RDF rdf) throws Exception {
        SrampOntology ontology = new SrampOntology();
        instance.map(rdf, ontology);
        return ontology;
    }

	/**
	 * Constructor.
	 */
	public RdfToOntologyMapper() {
	}

	/**
	 * Does the data mapping.
	 * @param rdf
	 * @param ontology
	 * @throws Exception
	 */
	public void map(RDF rdf, SrampOntology ontology) throws Exception {
        String uuid = rdf.getOtherAttributes().get(new QName(SrampConstants.SRAMP_NS, "uuid"));
        if (uuid != null && uuid.trim().length() > 0) {
            ontology.setUuid(uuid);
        }

        Ontology rdfOntology = rdf.getOntology();

		String base = rdf.getOtherAttributes().get(new QName("http://www.w3.org/XML/1998/namespace", "base"));
		ontology.setBase(base);
		ontology.setId(rdfOntology.getID());
		ontology.setLabel(rdfOntology.getLabel());
		ontology.setComment(rdfOntology.getComment());

		// First create all the classes included in the RDF
		List<Object[]> classes = new ArrayList<Object[]>();
		Map<String, SrampOntology.Class> idIndex = new HashMap<String, SrampOntology.Class>();
		Map<String, SrampOntology.Class> uriIndex = new HashMap<String, SrampOntology.Class>();
		for (org.w3._2002._07.owl_.Class rdfClass : rdf.getClazz()) {
			SrampOntology.Class oclass = new SrampOntology.Class();
			oclass.setId(rdfClass.getID());
			oclass.setLabel(rdfClass.getLabel());
			oclass.setComment(rdfClass.getComment());
			String uri = base + "#" + rdfClass.getID();
			oclass.setUri(new URI(uri));
			Object[] classData = new Object[] {
					oclass, rdfClass.getSubClassOf() != null ? rdfClass.getSubClassOf().getResource() : null
			};
			classes.add(classData);
			idIndex.put(rdfClass.getID(), oclass);
			uriIndex.put(uri, oclass);
		}

		// And now figure out the relationships
		for (Object [] classData : classes) {
			SrampOntology.Class oclass = (SrampOntology.Class) classData[0];
			String resourceRef = (String) classData[1];
			if (resourceRef == null) {
				ontology.getRootClasses().add(oclass);
			} else {
				SrampOntology.Class parent = idIndex.get(resourceRef);
				if (parent == null) {
					parent = uriIndex.get(resourceRef);
				}
				if (parent == null) {
					throw new Exception("Failed to resolve parent class reference: " + resourceRef);
				}
				parent.getChildren().add(oclass);
				oclass.setParent(parent);
			}
		}
	}

}
