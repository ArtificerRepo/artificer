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
package org.artificer.events.jms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.events.ArtifactUpdateEvent;
import org.artificer.events.EventProducer;
import org.artificer.events.OntologyUpdateEvent;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a JMS implementation of the {@link EventProducer}.
 *
 * During {@link #startup()}, we check for the existence of a JMS
 * "ConnectionFactory" and any configured topics/queues, both through JNDI
 * names. If they exist, it means we're in a JavaEE environment and things are
 * correctly configured. We simply use the existing JMS framework and the
 * pre-existing topics/queues.
 *
 * @author Brett Meyer
 */
public class JMSEventProducer implements EventProducer {

    public static final String JMS_TYPE_ARTIFACT_CREATED = "artificer:artifactCreated";
    public static final String JMS_TYPE_ARTIFACT_UPDATED = "artificer:artifactUpdated";
    public static final String JMS_TYPE_ARTIFACT_DELETED = "artificer:artifactDeleted";
    public static final String JMS_TYPE_ONTOLOGY_CREATED = "artificer:ontologyCreated";
    public static final String JMS_TYPE_ONTOLOGY_UPDATED = "artificer:ontologyUpdated";
    public static final String JMS_TYPE_ONTOLOGY_DELETED = "artificer:ontologyDeleted";

    private static Logger LOG = LoggerFactory.getLogger(JMSEventProducer.class);

    private Connection connection = null;

    private Session session = null;

    private final List<Destination> destinations = new ArrayList<Destination>();

    @Override
    public void startup() {
        if (ArtificerConfig.isJmsEnabled()) {
            try {
                String connectionFactoryName = ArtificerConfig.getConfigProperty(
                        ArtificerConstants.ARTIFICER_CONFIG_EVENT_JMS_CONNECTIONFACTORY, "ConnectionFactory");

                // Note that both properties end up doing the same thing.  Technically, we could combine both into one
                // single sramp.config.events.jms.destinations, but leaving them split for readability.
                String topicNamesProp = ArtificerConfig.getConfigProperty(ArtificerConstants.ARTIFICER_CONFIG_EVENT_JMS_TOPICS, "");
                String[] topicNames = new String[0];
                if (StringUtils.isNotEmpty(topicNamesProp)) {
                    topicNames = topicNamesProp.split(",");
                }
                String queueNamesProp = ArtificerConfig.getConfigProperty(ArtificerConstants.ARTIFICER_CONFIG_EVENT_JMS_QUEUES, "");
                String[] queueNames = new String[0];
                if (StringUtils.isNotEmpty(queueNamesProp)) {
                    queueNames = queueNamesProp.split(",");
                }

                // See if a ConnectionFactory and Topic/Queue exists on JNDI.  If so, assume JMS is properly
                // setup in a Java EE container and use it.

                ConnectionFactory connectionFactory = (ConnectionFactory) jndiLookup(connectionFactoryName);
                connection = connectionFactory.createConnection();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                for (String topicName : topicNames) {
                    Topic topic = (Topic) jndiLookup(topicName);
                    destinations.add(topic);
                }

                for (String queueName : queueNames) {
                    Queue queue = (Queue) jndiLookup(queueName);
                    destinations.add(queue);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void artifactCreated(BaseArtifactType artifact) {
        if (ArtificerConfig.isJmsEnabled()) {
            publishEvent(artifact, JMS_TYPE_ARTIFACT_CREATED);
        }
    }

    @Override
    public void artifactUpdated(BaseArtifactType updatedArtifact, BaseArtifactType oldArtifact) {
        if (ArtificerConfig.isJmsEnabled()) {
            ArtifactUpdateEvent event = new ArtifactUpdateEvent(updatedArtifact, oldArtifact);
            publishEvent(event, JMS_TYPE_ARTIFACT_UPDATED);
        }
    }

    @Override
    public void artifactDeleted(BaseArtifactType artifact) {
        if (ArtificerConfig.isJmsEnabled()) {
            publishEvent(artifact, JMS_TYPE_ARTIFACT_DELETED);
        }
    }

    @Override
    public void ontologyCreated(ArtificerOntology ontology) {
        if (ArtificerConfig.isJmsEnabled()) {
            publishEvent(ontology, JMS_TYPE_ONTOLOGY_CREATED);
        }
    }

    @Override
    public void ontologyUpdated(ArtificerOntology updatedOntology, ArtificerOntology oldOntology) {
        if (ArtificerConfig.isJmsEnabled()) {
            OntologyUpdateEvent event = new OntologyUpdateEvent(updatedOntology, oldOntology);
            publishEvent(event, JMS_TYPE_ONTOLOGY_UPDATED);
        }
    }

    @Override
    public void ontologyDeleted(ArtificerOntology ontology) {
        if (ArtificerConfig.isJmsEnabled()) {
            publishEvent(ontology, JMS_TYPE_ONTOLOGY_DELETED);
        }
    }

    private void publishEvent(Object payload, String type) {
        for (Destination destination : destinations) {
            MessageProducer producer = null;
            try {
                producer = session.createProducer(destination);
                TextMessage textMessage = session.createTextMessage();
                textMessage.setJMSType(type);

                ObjectMapper mapper = new ObjectMapper();
                String text = mapper.writeValueAsString(payload);
                textMessage.setText(text);

                producer.send(textMessage);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if (producer != null) {
                    try {
                        producer.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    private Object jndiLookup(String name) throws NamingException {
        Context initContext = new InitialContext();
        try {
            Context jndiContext = (Context) initContext.lookup("java:comp/env");
            return jndiContext.lookup(name);
        } catch (NamingException e) {
            // EAP (no namespace)
            Context jndiContext = (Context) initContext.lookup("java:");
            return jndiContext.lookup(name);
        }
    }

    @Override
    public void shutdown() {
        if (ArtificerConfig.isJmsEnabled()) {
            try {
                session.close();
            } catch (Exception e) {
            }
            try {
                connection.close();
            } catch (Exception e) {
            }
        }
    }
}
