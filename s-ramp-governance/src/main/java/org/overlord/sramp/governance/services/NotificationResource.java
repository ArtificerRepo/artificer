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
package org.overlord.sramp.governance.services;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.governance.NotificationDestinations;
import org.overlord.sramp.governance.Governance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JAX-RS resource that handles notification specific tasks.
 * 
 */
@Path("/notify")
public class NotificationResource {

    @Resource(mappedName="java:jboss/mail/Default")
    private Session mailSession;
    
    private static Logger logger = LoggerFactory.getLogger(NotificationResource.class);
    private Governance governance = new Governance();
    

    /**
     * Constructor.
     */
    public NotificationResource() {
    }

    /**
     * POST to email a notification about an artifact.
     * 
     * @param environment
     * @param uuid
     * @throws SrampAtomException
     */
    @POST
    @Path("email/{group}/{template}/{environment}/{uuid}")
    @Produces(MediaType.APPLICATION_XML)
    public Response emailNotification(@Context HttpServletRequest request,
            @PathParam("group") String group,
            @PathParam("template") String template,
            @PathParam("environment") String environment,
            @PathParam("uuid") String uuid) throws Exception {
        try {
            if (mailSession==null) {
                // 0. Take out this workaround. Get system properties, // Setup mail server
                Properties properties = new Properties();
                properties.setProperty("mail.smtp.host", "smtp.mailinator.com");
                mailSession = Session.getDefaultInstance(properties);
            }
            
            // 1. get the artifact from the repo
            SrampAtomApiClient client = new SrampAtomApiClient(governance.getSrampUrl().toExternalForm());
            String query = String.format("/s-ramp[@uuid='%s']", uuid);
            QueryResultSet queryResultSet = client.query(query);
            if (queryResultSet.size() == 0) {
                return Response.serverError().status(0).build();
            }
            ArtifactSummary artifactSummary = queryResultSet.iterator().next();

            // 2. get the destinations for this group
            NotificationDestinations destinations = governance.getNotificationDestinations("email").get(group);
            if (destinations==null) {
                logger.error("No emailAddresses could be found for group '"+ group + "'");
                throw new SrampAtomException("No email addresses could be found for group '"+ group + "'");
            }

            // 3. send the email notification
            try {
                MimeMessage m = new MimeMessage(mailSession);
                Address from = new InternetAddress(destinations.getFromAddress());
                Address[] to = new InternetAddress[destinations.getToAddresses().length];
                for (int i=0; i<destinations.getToAddresses().length;i++) {
                    to[i] = new InternetAddress(destinations.getToAddresses()[i]);
                }
                m.setFrom(from);
                m.setRecipients(Message.RecipientType.TO, to);
                
                String subject = "/governance-email-templates/" + template  + ".subject.tmpl";
                URL subjectUrl = Governance.class.getClassLoader().getResource(subject);
                if (subjectUrl!=null) subject=IOUtils.toString(subjectUrl);
                subject = subject.replaceAll("\\$\\{uuid}", uuid);
                subject = subject.replaceAll("\\$\\{name}", artifactSummary.getName());
                subject = subject.replaceAll("\\$\\{environment}", environment);
                m.setSubject(subject);
                
                m.setSentDate(new java.util.Date());
                String content = "/governance-email-templates/" + template + ".body.tmpl";
                URL contentUrl = Governance.class.getClassLoader().getResource(content);
                if (contentUrl!=null) content=IOUtils.toString(contentUrl);
                content = content.replaceAll("\\$\\{uuid}", uuid);
                content = content.replaceAll("\\$\\{name}", artifactSummary.getName());
                content = content.replaceAll("\\$\\{environment}", environment);
                m.setContent(content,"text/plain");
                Transport.send(m);
            } catch (javax.mail.MessagingException e) {
                logger.error(e.getMessage(),e);
            }
            
            InputStream reply = IOUtils.toInputStream("success");
            return Response.ok(reply, MediaType.APPLICATION_OCTET_STREAM).build();
        } catch (Exception e) {
            logger.error("Error sending a notification email. " + e.getMessage(), e);
            throw new SrampAtomException(e);
        }
    }

}
