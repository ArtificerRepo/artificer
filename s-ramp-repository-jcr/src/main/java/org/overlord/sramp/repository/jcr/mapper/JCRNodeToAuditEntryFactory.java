/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.repository.jcr.mapper;

import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType;
import org.overlord.sramp.repository.jcr.JCRConstants;

/**
 * Maps a JCR auditEntry node to an {@link AuditEntry} bean.
 * @author eric.wittmann@redhat.com
 */
public class JCRNodeToAuditEntryFactory {

    /**
     * Maps the jcr node content to an {@link AuditEntry}.
     * @param session
     * @param jcrNode
     */
    public static AuditEntry createAuditEntry(Session session, Node jcrNode) {
        try {
            DatatypeFactory dtFactory = DatatypeFactory.newInstance();

            String uuid = jcrNode.getProperty("audit:uuid").getString();
            String type = jcrNode.getProperty("audit:type").getString();
            String who = jcrNode.getProperty(JCRConstants.JCR_CREATED_BY).getString();
            XMLGregorianCalendar when = dtFactory.newXMLGregorianCalendar((GregorianCalendar)jcrNode.getProperty(JCRConstants.JCR_CREATED).getDate());

            AuditEntry entry = new AuditEntry();
            entry.setUuid(uuid);
            entry.setType(type);
            entry.setWho(who);
            entry.setWhen(when);

            NodeIterator auditItemNodes = jcrNode.getNodes();
            while (auditItemNodes.hasNext()) {
                AuditItemType auditItem = new AuditItemType();
                entry.getAuditItem().add(auditItem);
                Node auditItemNode = auditItemNodes.nextNode();
                PropertyIterator properties = auditItemNode.getProperties();
                while (properties.hasNext()) {
                    Property property = properties.nextProperty();
                    String propName = property.getName();
                    String propValue = property.getString();
                    if (propName.equals("audit:type")) {
                        auditItem.setType(propValue);
                    } else if (propName.equals(JCRConstants.JCR_PRIMARY_TYPE)) {
                        // Skip this one
                    } else {
                        org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType.Property p =
                                new org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType.Property();
                        p.setName(propName);
                        p.setValue(propValue);
                        auditItem.getProperty().add(p);
                    }
                }
            }

            return entry;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
