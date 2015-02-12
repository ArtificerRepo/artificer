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
package org.artificer.test.events.jms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.events.ArtifactUpdateEvent;
import org.artificer.events.OntologyUpdateEvent;
import org.artificer.test.AbstractIntegrationTest;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;
import org.w3._2002._07.owl_.Ontology;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Brett Meyer
 *
 */
public class JMSEventProducerTest extends AbstractIntegrationTest {

    private static final String WILDFLY_INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String WILDFLY_PROVIDER_URL = "http-remoting://localhost:8080";
    private static final String WILDFLY_CONNECTIONFACTORY_JNDI = "jms/RemoteConnectionFactory";
    private static final String WILDFLY_TOPIC_JNDI = "jms/artificer/events/topic";
    
    private static final String EAP_INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String EAP_PROVIDER_URL = "remote://localhost:4447";
    private static final String EAP_CONNECTIONFACTORY_JNDI = "jms/RemoteConnectionFactory";
    private static final String EAP_TOPIC_JNDI = "jms/artificer/events/topic";
    
    private List<TextMessage> textMessages;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty(ArtificerConstants.ARTIFICER_CONFIG_EVENT_JMS_ENABLED, "true"); //$NON-NLS-1$
    }
    
    @Test
    public void testArtifactTopic() throws Exception {
        textMessages = new ArrayList<TextMessage>();
        
        // 3 == create, update, delete
        final CountDownLatch lock = new CountDownLatch(3);
        
        Connection connection = subscribe(lock);
        
        // create
        ArtificerAtomApiClient client = client();
        ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType("FooArtifactType"); //$NON-NLS-1$
        artifact.setName("Foo"); //$NON-NLS-1$
        artifact.setDescription("created"); //$NON-NLS-1$
        ExtendedArtifactType persistedArtifact = (ExtendedArtifactType) client.createArtifact(artifact);
        
        // update
        persistedArtifact.setDescription("updated");
        client.updateArtifactMetaData(persistedArtifact);
        
        // delete
        client.deleteArtifact(persistedArtifact.getUuid(), ArtifactType.valueOf(artifact));
        
        lock.await(10000, TimeUnit.MILLISECONDS);
        
        assertEquals(3, textMessages.size());
        
        ObjectMapper mapper = new ObjectMapper();
        
        // sramp:artifactCreated
        TextMessage textMessage = textMessages.get(0);
        assertNotNull(textMessage);
        assertEquals("artificer:artifactCreated", textMessage.getJMSType());
        assertTrue(textMessage.getText() != null && textMessage.getText().length() > 0);
        ExtendedArtifactType eventArtifact = mapper.readValue(textMessage.getText(), ExtendedArtifactType.class);
        assertNotNull(eventArtifact);
        assertEquals(artifact.getExtendedType(), eventArtifact.getExtendedType());
        assertEquals(artifact.getName(), eventArtifact.getName());
        assertEquals(artifact.getDescription(), eventArtifact.getDescription());
        
        // sramp:artifactUpdated
        textMessage = textMessages.get(1);
        assertNotNull(textMessage);
        assertEquals("artificer:artifactUpdated", textMessage.getJMSType());
        assertTrue(textMessage.getText() != null && textMessage.getText().length() > 0);
        ArtifactUpdateEvent updateEvent = mapper.readValue(textMessage.getText(), ArtifactUpdateEvent.class);
        assertNotNull(updateEvent);
        assertNotNull(updateEvent.getOldArtifact());
        assertNotNull(updateEvent.getUpdatedArtifact());
        assertEquals(artifact.getExtendedType(), ((ExtendedArtifactType) updateEvent.getOldArtifact()).getExtendedType());
        assertEquals(artifact.getName(), updateEvent.getOldArtifact().getName());
        assertEquals(artifact.getDescription(), updateEvent.getOldArtifact().getDescription());
        assertEquals(artifact.getExtendedType(), ((ExtendedArtifactType) updateEvent.getUpdatedArtifact()).getExtendedType());
        assertEquals(artifact.getName(), updateEvent.getUpdatedArtifact().getName());
        assertEquals(persistedArtifact.getDescription(), updateEvent.getUpdatedArtifact().getDescription());
        
        // sramp:artifactDeleted
        textMessage = textMessages.get(2);
        assertNotNull(textMessage);
        assertEquals("artificer:artifactDeleted", textMessage.getJMSType());
        assertTrue(textMessage.getText() != null && textMessage.getText().length() > 0);
        eventArtifact = mapper.readValue(textMessage.getText(), ExtendedArtifactType.class);
        assertNotNull(eventArtifact);
        assertEquals(artifact.getExtendedType(), eventArtifact.getExtendedType());
        assertEquals(artifact.getName(), eventArtifact.getName());
        assertEquals(persistedArtifact.getDescription(), eventArtifact.getDescription());
        
        connection.close();
    }
    
    @Test
    public void testOntologyTopic() throws Exception {
        textMessages = new ArrayList<TextMessage>();
        
        // 3 == create, update, delete
        final CountDownLatch lock = new CountDownLatch(3);
        
        Connection connection = subscribe(lock);
        
        // create
        ArtificerAtomApiClient client = client();
        RDF rdf = new RDF();
        rdf.getOtherAttributes().put(new QName("http://www.w3.org/XML/1998/namespace", "base"), "foo");
        Ontology ontology = new Ontology();
        ontology.setID("Color");
        ontology.setLabel("Color");
        rdf.setOntology(ontology);
        org.w3._2002._07.owl_.Class clazz1 = new org.w3._2002._07.owl_.Class();
        clazz1.setID("Red");
        clazz1.setLabel("Red");
        org.w3._2002._07.owl_.Class clazz2 = new org.w3._2002._07.owl_.Class();
        clazz2.setID("Blue");
        clazz2.setLabel("Blue");
        rdf.getClazz().add(clazz1);
        rdf.getClazz().add(clazz2);
        RDF persistedRdf = client.addOntology(rdf);
        
        String uuid = persistedRdf.getOtherAttributes().get(
                new QName("http://docs.oasis-open.org/s-ramp/ns/s-ramp-v1.0", "uuid")).replace("urn:uuid:", "");
        
        // update
        persistedRdf.getOntology().setLabel("ColorUpdated");
        client.updateOntology(uuid, persistedRdf);
        
        // delete
        client.deleteOntology(uuid);
        
        lock.await(10000, TimeUnit.MILLISECONDS);
        
        assertEquals(3, textMessages.size());
        
        ObjectMapper mapper = new ObjectMapper();
        
        // sramp:ontologyCreated
        TextMessage textMessage = textMessages.get(0);
        assertNotNull(textMessage);
        assertEquals("artificer:ontologyCreated", textMessage.getJMSType());
        assertTrue(textMessage.getText() != null && textMessage.getText().length() > 0);
        ArtificerOntology eventOntology = mapper.readValue(textMessage.getText(), ArtificerOntology.class);
        assertNotNull(eventOntology);
        assertEquals(rdf.getOntology().getID(), eventOntology.getId());
        assertEquals(rdf.getOntology().getLabel(), eventOntology.getLabel());
        assertEquals(2, eventOntology.getRootClasses().size());
        assertEquals(clazz1.getID(), eventOntology.getRootClasses().get(0).getId());
        assertEquals(clazz1.getLabel(), eventOntology.getRootClasses().get(0).getLabel());
        assertEquals(clazz2.getID(), eventOntology.getRootClasses().get(1).getId());
        assertEquals(clazz2.getLabel(), eventOntology.getRootClasses().get(1).getLabel());
        
        // sramp:ontologyUpdated
        textMessage = textMessages.get(1);
        assertNotNull(textMessage);
        assertEquals("artificer:ontologyUpdated", textMessage.getJMSType());
        assertTrue(textMessage.getText() != null && textMessage.getText().length() > 0);
        OntologyUpdateEvent updateEvent = mapper.readValue(textMessage.getText(), OntologyUpdateEvent.class);
        assertNotNull(updateEvent);
        assertNotNull(updateEvent.getOldOntology());
        assertNotNull(updateEvent.getUpdatedOntology());
        assertEquals(rdf.getOntology().getID(), updateEvent.getOldOntology().getId());
        assertEquals(rdf.getOntology().getLabel(), updateEvent.getOldOntology().getLabel());
        assertEquals(rdf.getOntology().getID(), updateEvent.getUpdatedOntology().getId());
        assertEquals(persistedRdf.getOntology().getLabel(), updateEvent.getUpdatedOntology().getLabel());

        // sramp:ontologyDeleted
        textMessage = textMessages.get(2);
        assertNotNull(textMessage);
        assertEquals("artificer:ontologyDeleted", textMessage.getJMSType());
        assertTrue(textMessage.getText() != null && textMessage.getText().length() > 0);
        eventOntology = mapper.readValue(textMessage.getText(), ArtificerOntology.class);
        assertNotNull(eventOntology);
        assertEquals(rdf.getOntology().getID(), eventOntology.getId());
        
        connection.close();
    }
    
    private Connection subscribe(final CountDownLatch lock) {
        try {
            return subscribe(WILDFLY_INITIAL_CONTEXT_FACTORY, WILDFLY_PROVIDER_URL, WILDFLY_CONNECTIONFACTORY_JNDI, WILDFLY_TOPIC_JNDI, lock);
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
            return subscribe(EAP_INITIAL_CONTEXT_FACTORY, EAP_PROVIDER_URL, EAP_CONNECTIONFACTORY_JNDI, EAP_TOPIC_JNDI, lock);
        } catch (Exception e) {
          e.printStackTrace();
        }
        fail("Could not create a JMS client.");
        return null;
    }
    
    private Connection subscribe(String contextFactoryName, String providerUrl, String connectionFactoryName,
            String topicName, final CountDownLatch lock) throws Exception {
        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactoryName);
        env.put(Context.PROVIDER_URL, providerUrl);
        env.put(Context.SECURITY_PRINCIPAL, "artificer");
        env.put(Context.SECURITY_CREDENTIALS, PASSWORD);
        Context context = new InitialContext(env);
        
        ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryName);
        Connection connection = connectionFactory.createConnection("artificer", PASSWORD);
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = (Topic) context.lookup(topicName);
        MessageConsumer topicSubscriber = session.createConsumer(topic);
        topicSubscriber.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    textMessages.add((TextMessage) message);
                    lock.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        connection.start();
        return connection;
    }
}
