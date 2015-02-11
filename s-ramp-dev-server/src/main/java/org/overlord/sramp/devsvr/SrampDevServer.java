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
package org.overlord.sramp.devsvr;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.commons.dev.server.DevServerEnvironment;
import org.overlord.commons.dev.server.ErraiDevServer;
import org.overlord.commons.dev.server.MultiDefaultServlet;
import org.overlord.commons.dev.server.discovery.ErraiWebAppModuleFromMavenDiscoveryStrategy;
import org.overlord.commons.dev.server.discovery.WebAppModuleFromIDEDiscoveryStrategy;
import org.overlord.commons.gwt.server.filters.GWTCacheControlFilter;
import org.overlord.commons.gwt.server.filters.ResourceCacheControlFilter;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.repository.jcr.JCRRepository;
import org.overlord.sramp.repository.jcr.filter.ServletCredentialsFilter;
import org.overlord.sramp.server.SrampLifeCycle;
import org.overlord.sramp.server.atom.services.SRAMPApplication;
import org.overlord.sramp.server.filters.LocaleFilter;
import org.overlord.sramp.server.filters.MavenRepositoryAuthFilter;
import org.overlord.sramp.server.mvn.services.MavenFacadeServlet;
import org.overlord.sramp.ui.client.shared.beans.ArtifactSummaryBean;
import org.overlord.sramp.ui.server.api.KeycloakBearerTokenAuthenticationProvider;
import org.overlord.sramp.ui.server.servlets.*;

import javax.security.auth.Subject;
import javax.servlet.DispatcherType;
import java.io.InputStream;
import java.security.Principal;
import java.util.EnumSet;

/**
 * A dev server for s-ramp.
 * @author eric.wittmann@redhat.com
 */
public class SrampDevServer extends ErraiDevServer {

    /**
     * Main entry point.
     * @param args
     */
    public static void main(String [] args) throws Exception {
        SrampDevServer devServer = new SrampDevServer(args);
        devServer.enableDebug();
        devServer.go();
    }

    /**
     * Constructor.
     * @param args
     */
    public SrampDevServer(String [] args) {
        super(args);
    }

    /**
     * @see org.overlord.commons.dev.server.ErraiDevServer#getErraiModuleId()
     */
    @Override
    protected String getErraiModuleId() {
        return "s-ramp-ui";
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#preConfig()
     */
    @Override
    protected void preConfig() {
        // Use an in-memory config for s-ramp
        System.setProperty("sramp.modeshape.config.url", "classpath://" + JCRRepository.class.getName()
                + "/META-INF/modeshape-configs/inmemory-sramp-config.json");

        // Authentication provider
        System.setProperty("s-ramp-ui.atom-api.authentication.provider", KeycloakBearerTokenAuthenticationProvider.class.getName());

        // Don't do any resource caching!
        System.setProperty("overlord.resource-caching.disabled", "true");

        System.setProperty("sramp.config.events.jms.topics", "sramp/events/topic");
        System.setProperty("sramp.config.events.jms.embedded-activemq-port", "61616");
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#createDevEnvironment()
     */
    @Override
    protected DevServerEnvironment createDevEnvironment() {
        return new SrampDevServerEnvironment(args);
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#addModules(org.overlord.commons.dev.server.DevServerEnvironment)
     */
    @Override
    protected void addModules(DevServerEnvironment environment) {
        environment.addModule("s-ramp-ui",
                new WebAppModuleFromIDEDiscoveryStrategy(ArtifactSummaryBean.class),
                new ErraiWebAppModuleFromMavenDiscoveryStrategy(ArtifactSummaryBean.class));
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#addModulesToJetty(org.overlord.commons.dev.server.DevServerEnvironment, org.eclipse.jetty.server.handler.ContextHandlerCollection)
     */
    @Override
    protected void addModulesToJetty(DevServerEnvironment environment, ContextHandlerCollection handlers) throws Exception {
        super.addModulesToJetty(environment, handlers);

        /* *********
         * S-RAMP UI
         * ********* */
        ServletContextHandler srampUI = new ServletContextHandler(ServletContextHandler.SESSIONS);
        srampUI.setSecurityHandler(createSecurityHandler(true));
        srampUI.setContextPath("/s-ramp-ui");
        srampUI.setWelcomeFiles(new String[]{"index.html"});
        srampUI.setResourceBase(environment.getModuleDir("s-ramp-ui").getCanonicalPath());
        srampUI.addFilter(GWTCacheControlFilter.class, "/app/*", EnumSet.of(DispatcherType.REQUEST));
        srampUI.addFilter(GWTCacheControlFilter.class, "/rest/*", EnumSet.of(DispatcherType.REQUEST));
        srampUI.addFilter(GWTCacheControlFilter.class, "/", EnumSet.of(DispatcherType.REQUEST));
        srampUI.addFilter(GWTCacheControlFilter.class, "*.html", EnumSet.of(DispatcherType.REQUEST));
        srampUI.addFilter(ResourceCacheControlFilter.class, "/css/*", EnumSet.of(DispatcherType.REQUEST));
        srampUI.addFilter(ResourceCacheControlFilter.class, "/images/*", EnumSet.of(DispatcherType.REQUEST));
        srampUI.addFilter(ResourceCacheControlFilter.class, "/js/*", EnumSet.of(DispatcherType.REQUEST));
        srampUI.addFilter(org.overlord.sramp.ui.server.filters.LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        // Servlets
        srampUI.addServlet(new ServletHolder(ArtifactUploadServlet.class), "/app/services/artifactUpload");
        srampUI.addServlet(new ServletHolder(ArtifactDownloadServlet.class), "/app/services/artifactDownload");
        srampUI.addServlet(new ServletHolder(OntologyUploadServlet.class), "/app/services/ontologyUpload");
        srampUI.addServlet(new ServletHolder(OntologyDownloadServlet.class), "/app/services/ontologyDownload");
        srampUI.addServlet(new ServletHolder(KeyCloakLogoutServlet.class), "/app/services/logout");
        ServletHolder resteasyUIServlet = new ServletHolder(new HttpServletDispatcher());
        resteasyUIServlet.setInitParameter("javax.ws.rs.Application", JettySRAMPApplication.class.getName());
        resteasyUIServlet.setInitParameter("resteasy.servlet.mapping.prefix", "/rest");
        srampUI.addServlet(resteasyUIServlet, "/rest/*");
        // File resources
        ServletHolder resources = new ServletHolder(new MultiDefaultServlet());
        resources.setInitParameter("resourceBase", "/");
        resources.setInitParameter("resourceBases", environment.getModuleDir("s-ramp-ui").getCanonicalPath());
        resources.setInitParameter("dirAllowed", "true");
        resources.setInitParameter("pathInfoOnly", "false");
        String[] fileTypes = new String[] { "html", "js", "css", "png", "gif" };
        for (String fileType : fileTypes) {
            srampUI.addServlet(resources, "*." + fileType);
        }

        /* *************
         * S-RAMP server
         * ************* */
        ServletContextHandler srampServer = new ServletContextHandler(ServletContextHandler.SESSIONS);
        srampServer.setSecurityHandler(createSecurityHandler(false));
        srampServer.setContextPath("/s-ramp-server");
        srampServer.addEventListener(new SrampLifeCycle());
        ServletHolder resteasyServerServlet = new ServletHolder(new HttpServletDispatcher());
        resteasyServerServlet.setInitParameter("javax.ws.rs.Application", SRAMPApplication.class.getName());
        srampServer.addServlet(resteasyServerServlet, "/s-ramp/*");
        //maven repository servlet:
        ServletHolder mvnServlet = new ServletHolder(new MavenFacadeServlet());
        srampServer.addServlet(mvnServlet, "/maven/repository/*");
        srampServer.addServlet(mvnServlet, "/maven/repository");
        // TODO enable JSP support to test the repository listing

        srampServer.addFilter(BasicAuthFilter.class, "/s-ramp/*", EnumSet.of(DispatcherType.REQUEST))
                .setInitParameter("allowedIssuers", "/s-ramp-ui,/dtgov,/dtgov-ui");
        srampServer.addFilter(MavenRepositoryAuthFilter.class, "/maven/repository/*", EnumSet.of(DispatcherType.REQUEST))
                .setInitParameter("allowedIssuers", "/s-ramp-ui,/dtgov,/dtgov-ui");
        srampServer.addFilter(LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        srampServer.addFilter(ServletCredentialsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        // Add to handlers
        handlers.addHandler(srampUI);
        handlers.addHandler(srampServer);
    }

    /**
     * @return a security handler
     */
    private SecurityHandler createSecurityHandler(boolean forUI) {
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"user"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setSessionRenewedOnAuthentication(false);
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("artificer");
        if (forUI) {
            csh.addConstraintMapping(cm);
        }
        csh.setLoginService(new HashLoginService() {
            @Override
            public UserIdentity login(String username, Object credentials) {
                Credential credential = (credentials instanceof Credential) ? (Credential) credentials
                        : Credential.getCredential(credentials.toString());
                Principal userPrincipal = new KnownUser(username, credential);
                Subject subject = new Subject();
                subject.getPrincipals().add(userPrincipal);
                subject.getPrivateCredentials().add(credential);
                String[] roles = new String[] { "user","readonly","readwrite","admin" };
                for (String role : roles) {
                    subject.getPrincipals().add(new RolePrincipal(role));
                }
                subject.setReadOnly();
                return _identityService.newUserIdentity(subject, userPrincipal, roles);
            }
        });

        return csh;
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#postStart(org.overlord.commons.dev.server.DevServerEnvironment)
     */
    @Override
    protected void postStart(DevServerEnvironment environment) throws Exception {
        System.out.println("----------  Seeding the Repository  ---------------");

        SrampAtomApiClient client = new SrampAtomApiClient("http://localhost:"+serverPort()+"/s-ramp-server", "seeder", "seeder", true);

        String seedType = System.getProperty("s-ramp-dev-server.seed-type", "none");
        if ("switchyard".equals(seedType)) {
            doSwitchYardSeed(client);
        } else if ("standard".equals(seedType)) {
            doStandardSeed(client);
        } else {
            // no seeding
        }

        System.out.println("----------  DONE  ---------------");
        System.out.println("Now try:  \n  http://localhost:"+serverPort()+"/s-ramp-ui/index.html");
        System.out.println("---------------------------------");
    }

    /**
     * @param client
     * @throws SrampAtomException
     * @throws SrampClientException
     */
    private void doStandardSeed(SrampAtomApiClient client) throws SrampClientException, SrampAtomException {
        InputStream is = null;

        // Ontology #1
        try {
            is = SrampDevServer.class.getResourceAsStream("colors.owl.xml");
            client.uploadOntology(is);
            System.out.println("Ontology 1 added");
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Ontology #2
        try {
            is = SrampDevServer.class.getResourceAsStream("regional.owl.xml");
            client.uploadOntology(is);
            System.out.println("Ontology 2 added");
        } finally {
            IOUtils.closeQuietly(is);
        }

        // PDF Document
        try {
            is = SrampDevServer.class.getResourceAsStream("sample.pdf");
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.Document(), is, "sample.pdf");
            artifact.setDescription("This is just a sample PDF file that is included in the dev server so that we have some content when we start up.");
            artifact.setVersion("1.0");
            client.updateArtifactMetaData(artifact);
            System.out.println("PDF added");
        } finally {
            IOUtils.closeQuietly(is);
        }

        // XML Document
        try {
            is = SrampDevServer.class.getResourceAsStream("order.xml");
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XmlDocument(), is, "order.xml");
            artifact.getClassifiedBy().add("http://www.example.org/colors.owl#Blue");
            SrampModelUtils.setCustomProperty(artifact, "foo", "bar");
            SrampModelUtils.setCustomProperty(artifact, "angle", "obtuse");
            client.updateArtifactMetaData(artifact);
            System.out.println("XML file added");
        } finally {
            IOUtils.closeQuietly(is);
        }

        // WSDL Document
        try {
            is = SrampDevServer.class.getResourceAsStream("deriver.wsdl");
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.WsdlDocument(), is, "deriver.wsdl");
            artifact.getClassifiedBy().add("http://www.example.org/colors.owl#Red");
            artifact.getClassifiedBy().add("http://www.example.org/regional.owl#Asia");
            client.updateArtifactMetaData(artifact);
            System.out.println("WSDL added");
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * @param client
     */
    private void doSwitchYardSeed(SrampAtomApiClient client) throws Exception {
        // Upload the artifacts jar
        InputStream artifactsIS = this.getClass().getResourceAsStream("artifacts.jar");
        try {
            client.uploadArtifact(artifactsIS, "artifacts.jar");
            System.out.println("Added SwitchYard app (artifacts.jar)");
        } finally {
            IOUtils.closeQuietly(artifactsIS);
        }

        // Upload the order consumer jar
        InputStream orderConsumerIS = this.getClass().getResourceAsStream("order-consumer.jar");
        try {
            client.uploadArtifact(orderConsumerIS, "order-consumer.jar");
            System.out.println("Added SwitchYard app (order-consumer.jar)");
        } finally {
            IOUtils.closeQuietly(artifactsIS);
        }

        // Upload the order service jar
        InputStream orderServiceIS = this.getClass().getResourceAsStream("order-service.jar");
        try {
            client.uploadArtifact(orderServiceIS, "order-service.jar");
            System.out.println("Added SwitchYard app (order-service.jar)");
        } finally {
            IOUtils.closeQuietly(artifactsIS);
        }

    }

}
