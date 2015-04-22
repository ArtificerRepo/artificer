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
package org.artificer.repository;

import org.artificer.common.ArtificerException;
import org.artificer.common.error.ArtificerUserException;
import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.common.ontology.ArtificerOntologyClass;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Brett Meyer.
 */
public abstract class AbstractPersistenceManager implements PersistenceManager, ClassificationHelper {

    @Override
    public URI resolve(String classifiedBy) throws ArtificerException {
        URI classifiedUri = null;
        try {
            classifiedUri = new URI(classifiedBy);
        } catch (URISyntaxException e) {
            throw ArtificerUserException.invalidClassifiedBy(classifiedBy);
        }
        Collection<ArtificerOntology> ontologies = getOntologies();
        for (ArtificerOntology ontology : ontologies) {
            ArtificerOntologyClass sclass = ontology.findClass(classifiedBy);
            if (sclass == null) {
                sclass = ontology.findClass(classifiedUri);
            }
            if (sclass != null) {
                return sclass.getUri();
            }
        }
        throw ArtificerUserException.invalidClassifiedBy(classifiedBy);
    }

    @Override
    public Collection<URI> normalize(URI classification) throws ArtificerException {
        List<ArtificerOntology> ontologies = getOntologies();
        for (ArtificerOntology ontology : ontologies) {
            ArtificerOntologyClass sclass = ontology.findClass(classification);
            if (sclass != null) {
                return sclass.normalize();
            }
        }
        throw ArtificerUserException.invalidClassifiedBy(classification.toString());
    }

    @Override
    public Collection<URI> resolveAll(Collection<String> classifiedBy) throws ArtificerException {
        Set<URI> resolved = new HashSet<>(classifiedBy.size());
        for (String classification : classifiedBy) {
            resolved.add(resolve(classification));
        }
        return resolved;
    }

    @Override
    public Collection<URI> normalizeAll(Collection<URI> classifications) throws ArtificerException {
        Set<URI> resolved = new HashSet<>(classifications.size());
        for (URI classification : classifications) {
            resolved.addAll(normalize(classification));
        }
        return resolved;
    }
}
