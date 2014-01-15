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

import javax.xml.namespace.QName;

import org.overlord.sramp.common.ontology.SrampOntology;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;
import org.w3._2000._01.rdf_schema_.SubClassOf;
import org.w3._2002._07.owl_.Ontology;

/**
 * Maps S-RAMP ontology data into an RDF model.  See the s-ramp specification for details,
 * but note that the RDF model in this case is a subset of the OWL Lite specification.
 *
 * @author eric.wittmann@redhat.com
 */
public class OntologyToRdfMapper {

	/**
	 * Constructor.
	 */
	public OntologyToRdfMapper() {
	}

	/**
	 * Does the data mapping.
	 * @param ontology
	 * @param rdf
	 */
	public void map(SrampOntology ontology, RDF rdf) {
		Ontology rdfOntology = new Ontology();
		rdfOntology.setID(ontology.getId());
		rdfOntology.setLabel(ontology.getLabel());
		rdfOntology.setComment(ontology.getComment());
		rdf.getOtherAttributes().put(new QName("http://www.w3.org/XML/1998/namespace", "base"), ontology.getBase()); //$NON-NLS-1$ //$NON-NLS-2$
		rdf.setOntology(rdfOntology);

		for (SrampOntology.SrampOntologyClass oclass : ontology.getAllClasses()) {
			org.w3._2002._07.owl_.Class rdfClass = new org.w3._2002._07.owl_.Class();
			rdfClass.setID(oclass.getId());
			rdfClass.setLabel(oclass.getLabel());
			rdfClass.setComment(oclass.getComment());
			if (oclass.getParent() != null) {
				SubClassOf subclass = new SubClassOf();
				subclass.setResource(oclass.getParent().getUri().toString());
				rdfClass.setSubClassOf(subclass);
			}
			rdf.getClazz().add(rdfClass);
		}
	}

}
