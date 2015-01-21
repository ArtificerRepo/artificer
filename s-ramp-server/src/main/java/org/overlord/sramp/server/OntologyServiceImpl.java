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
package org.overlord.sramp.server;

import org.overlord.sramp.common.ontology.OntologyValidator;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.events.EventProducer;
import org.overlord.sramp.events.EventProducerFactory;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.server.core.api.OntologyService;

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
    public SrampOntology create(SrampOntology ontology) throws Exception  {
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
    public void update(String uuid, SrampOntology ontology) throws Exception {
        OntologyValidator.validateOntology(ontology);

        SrampOntology oldOntology = get(uuid);

        PersistenceManager persistenceManager = persistenceManager();
        persistenceManager.updateOntology(ontology);

        Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
        for (EventProducer eventProducer : eventProducers) {
            eventProducer.ontologyUpdated(ontology, oldOntology);
        }
    }

    @Override
    public SrampOntology get(String uuid) throws Exception {
        return persistenceManager().getOntology(uuid);
    }

    @Override
    public List<SrampOntology> get() throws Exception {
        return persistenceManager().getOntologies();
    }

    @Override
    public void delete(String uuid) throws Exception {
        SrampOntology ontology = get(uuid);

        persistenceManager().deleteOntology(uuid);

        Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
        for (EventProducer eventProducer : eventProducers) {
            eventProducer.ontologyDeleted(ontology);
        }
    }
}
