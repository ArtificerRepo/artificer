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
package org.overlord.sramp.events.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.SrampConfig;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.events.ArtifactUpdateEvent;
import org.overlord.sramp.events.EventProducer;
import org.overlord.sramp.events.OntologyUpdateEvent;
import org.overlord.sramp.events.jms.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

import javax.jms.*;
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
 * Otherwise, we assume we're on a non-JavaEE server (Tomcat, EAP without
 * standalone-full, etc). We then create an embedded ActiveMQ broker over a TCP
 * port, then programmatically create all topics/queues. External clients can
 * then connect to it in one of two ways: 1.) Simply use the ActiveMQ libs and
 * API 2.) The ActiveMQ broker provides a lightweight JNDI implementation and
 * automatically exposes the ConnectionFactory (literally named
 * "ConnectionFactory"). To expose the topics/queues, the *client app* needs to
 * include a jndi.properties file (and ActiveMQ jar) on the classpath. The
 * contents should contain something like
 * "topic.[jndi name] = [activemq topic name]". [jndi name] is then available to
 * the client. Other than that properties file, the client is able to use
 * generic JNDI and JMS without any ActiveMQ APIs.
 *
 * @author Brett Meyer
 */
public class JMSEventProducer implements EventProducer {

    public static final String JMS_TYPE_ARTIFACT_CREATED = "sramp:artifactCreated"; //$NON-NLS-1$
    public static final String JMS_TYPE_ARTIFACT_UPDATED = "sramp:artifactUpdated"; //$NON-NLS-1$
    public static final String JMS_TYPE_ARTIFACT_DELETED = "sramp:artifactDeleted"; //$NON-NLS-1$
    public static final String JMS_TYPE_ONTOLOGY_CREATED = "sramp:ontologyCreated"; //$NON-NLS-1$
    public static final String JMS_TYPE_ONTOLOGY_UPDATED = "sramp:ontologyUpdated"; //$NON-NLS-1$
    public static final String JMS_TYPE_ONTOLOGY_DELETED = "sramp:ontologyDeleted"; //$NON-NLS-1$

    private static Logger LOG = LoggerFactory.getLogger(JMSEventProducer.class);

    private Connection connection = null;

    private Session session = null;

    private final List<Destination> destinations = new ArrayList<Destination>();

    @Override
    public void startup() {

        try {
            String connectionFactoryName = SrampConfig.getConfigProperty(
                    SrampConstants.SRAMP_CONFIG_EVENT_JMS_CONNECTIONFACTORY, "ConnectionFactory"); //$NON-NLS-1$
            
            // Note that both properties end up doing the same thing.  Technically, we could combine both into one
            // single sramp.config.events.jms.destinations, but leaving them split for readability.
            String topicNamesProp = SrampConfig.getConfigProperty(SrampConstants.SRAMP_CONFIG_EVENT_JMS_TOPICS, ""); //$NON-NLS-1$
            String[] topicNames = new String[0];
            if (StringUtils.isNotEmpty(topicNamesProp)) {
                topicNames = topicNamesProp.split(","); //$NON-NLS-1$
            }
            String queueNamesProp = SrampConfig.getConfigProperty(SrampConstants.SRAMP_CONFIG_EVENT_JMS_QUEUES, ""); //$NON-NLS-1$
            String[] queueNames = new String[0];
            if (StringUtils.isNotEmpty(queueNamesProp)) {
                queueNames = queueNamesProp.split(","); //$NON-NLS-1$
            }

            try {
                // First, see if a ConnectionFactory and Topic/Queue exists on JNDI.  If so, assume JMS is properly
                // setup in a Java EE container and simply use it.

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
            } catch (NamingException e) {
                // Otherwise, JMS wasn't setup. Assume we need to start an embedded
                // ActiveMQ broker and create the destinations.

                String bindAddress = "tcp://localhost:" //$NON-NLS-1$
                        + SrampConfig.getConfigProperty(SrampConstants.SRAMP_CONFIG_EVENT_JMS_PORT, "61616"); //$NON-NLS-1$

                LOG.warn(Messages.i18n.format("org.overlord.sramp.events.jms.embedded_broker", bindAddress)); //$NON-NLS-1$

                session = null;
                destinations.clear();

                BrokerService broker = new BrokerService();
                broker.addConnector(bindAddress);
                broker.start();

                // Event though we added a TCP connector, above, ActiveMQ also exposes the broker over the "vm"
                // protocol. It optimizes performance for connections on the same JVM.
                ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost"); //$NON-NLS-1$
                initActiveMQ(connectionFactory, topicNames, queueNames);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    private void initActiveMQ(ConnectionFactory connectionFactory, String[] topicNames, String[] queueNames)
            throws Exception {
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        for (String topicName : topicNames) {
            destinations.add(session.createTopic(topicName));
        }

        for (String queueName : queueNames) {
            destinations.add(session.createQueue(queueName));
        }
    }

    @Override
    public void artifactCreated(BaseArtifactType artifact) {
        publishEvent(artifact, JMS_TYPE_ARTIFACT_CREATED);
    }

    @Override
    public void artifactUpdated(BaseArtifactType updatedArtifact, BaseArtifactType oldArtifact) {
        ArtifactUpdateEvent event = new ArtifactUpdateEvent(updatedArtifact, oldArtifact);
        publishEvent(event, JMS_TYPE_ARTIFACT_UPDATED);
    }

    @Override
    public void artifactDeleted(BaseArtifactType artifact) {
        publishEvent(artifact, JMS_TYPE_ARTIFACT_DELETED);
    }

    @Override
    public void ontologyCreated(RDF ontology) {
        publishEvent(ontology, JMS_TYPE_ONTOLOGY_CREATED);
    }

    @Override
    public void ontologyUpdated(RDF updatedOntology, RDF oldOntology) {
        OntologyUpdateEvent event = new OntologyUpdateEvent(updatedOntology, oldOntology);
        publishEvent(event, JMS_TYPE_ONTOLOGY_UPDATED);
    }

    @Override
    public void ontologyDeleted(RDF ontology) {
        publishEvent(ontology, JMS_TYPE_ONTOLOGY_DELETED);
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
            Context jndiContext = (Context) initContext.lookup("java:comp/env"); //$NON-NLS-1$
            return jndiContext.lookup(name);
        } catch (NamingException e) {
            // EAP (no namespace)
            Context jndiContext = (Context) initContext.lookup("java:"); //$NON-NLS-1$
            return jndiContext.lookup(name);
        }
    }

    @Override
    public void shutdown() {
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
