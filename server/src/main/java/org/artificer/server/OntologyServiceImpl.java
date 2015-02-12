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
package org.artificer.server;

import org.artificer.common.ontology.OntologyValidator;
import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.events.EventProducer;
import org.artificer.events.EventProducerFactory;
import org.artificer.repository.PersistenceManager;
import org.artificer.server.core.api.OntologyService;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import java.util.List;
import java.util.Set;

/**
 * @author Brett Meyer.
 */
@Stateful(name = "OntologyService")
@Remote(OntologyService.class)
public class OntologyServiceImpl extends AbstractServiceImpl implements OntologyService {

    @Override
    public ArtificerOntology create(ArtificerOntology ontology) throws Exception  {
        OntologyValidator.validateOntology(ontology);

        PersistenceManager persistenceManager = persistenceManager();
        ontology = persistenceManager.persistOntology(ontology);

        Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
        for (EventProducer eventProducer : eventProducers) {
            eventProducer.ontologyCreated(ontology);
        }

        return ontology;
    }

    @Override
    public void update(String uuid, ArtificerOntology ontology) throws Exception {
        OntologyValidator.validateOntology(ontology);

        ArtificerOntology oldOntology = get(uuid);

        PersistenceManager persistenceManager = persistenceManager();
        persistenceManager.updateOntology(ontology);

        Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
        for (EventProducer eventProducer : eventProducers) {
            eventProducer.ontologyUpdated(ontology, oldOntology);
        }
    }

    @Override
    public ArtificerOntology get(String uuid) throws Exception {
        return persistenceManager().getOntology(uuid);
    }

    @Override
    public List<ArtificerOntology> get() throws Exception {
        return persistenceManager().getOntologies();
    }

    @Override
    public void delete(String uuid) throws Exception {
        ArtificerOntology ontology = get(uuid);

        persistenceManager().deleteOntology(uuid);

        Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
        for (EventProducer eventProducer : eventProducers) {
            eventProducer.ontologyDeleted(ontology);
        }
    }
}
