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
package org.artificer.devsvr;

import org.apache.commons.io.IOUtils;
import org.artificer.ui.server.filters.LocaleFilter;
import org.artificer.ui.server.servlets.ArtifactDownloadServlet;
import org.artificer.ui.server.servlets.ArtifactUploadServlet;
import org.artificer.ui.server.servlets.KeyCloakLogoutServlet;
import org.artificer.ui.server.servlets.OntologyDownloadServlet;
import org.artificer.ui.server.servlets.OntologyUploadServlet;
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
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ArtificerClientException;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.repository.jcr.JCRRepository;
import org.artificer.repository.jcr.filter.ServletCredentialsFilter;
import org.artificer.server.ArtificerLifeCycle;
import org.artificer.server.atom.services.ArtificerApplication;
import org.artificer.server.filters.MavenRepositoryAuthFilter;
import org.artificer.server.mvn.services.MavenFacadeServlet;
import org.artificer.ui.client.shared.beans.ArtifactSummaryBean;
import org.artificer.ui.server.api.KeycloakBearerTokenAuthenticationProvider;

import javax.security.auth.Subject;
import javax.servlet.DispatcherType;
import java.io.InputStream;
import java.security.Principal;
import java.util.EnumSet;

/**
 * A dev server for s-ramp.
 * @author eric.wittmann@redhat.com
 */
public class ArtificerDevServer extends ErraiDevServer {

    /**
     * Main entry point.
     * @param args
     */
    public static void main(String [] args) throws Exception {
        ArtificerDevServer devServer = new ArtificerDevServer(args);
        devServer.enableDebug();
        devServer.go();
    }

    /**
     * Constructor.
     * @param args
     */
    public ArtificerDevServer(String[] args) {
        super(args);
    }

    /**
     * @see org.overlord.commons.dev.server.ErraiDevServer#getErraiModuleId()
     */
    @Override
    protected String getErraiModuleId() {
        return "artificer-ui";
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#preConfig()
     */
    @Override
    protected void preConfig() {
        // Use an in-memory config for s-ramp
        System.setProperty("artificer.modeshape.config.url", "classpath://" + JCRRepository.class.getName()
                + "/META-INF/modeshape-configs/inmemory-artificer-config.json");

        // Authentication provider
        System.setProperty("artificer-ui.atom-api.authentication.provider", KeycloakBearerTokenAuthenticationProvider.class.getName());

        // Don't do any resource caching!
        System.setProperty("overlord.resource-caching.disabled", "true");

        System.setProperty("artificer.config.events.jms.topics", "artificer/events/topic");
        System.setProperty("artificer.config.events.jms.embedded-activemq-port", "61616");
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#createDevEnvironment()
     */
    @Override
    protected DevServerEnvironment createDevEnvironment() {
        return new ArtificerDevServerEnvironment(args);
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#addModules(org.overlord.commons.dev.server.DevServerEnvironment)
     */
    @Override
    protected void addModules(DevServerEnvironment environment) {
        environment.addModule("artificer-ui",
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
        ServletContextHandler artificerUI = new ServletContextHandler(ServletContextHandler.SESSIONS);
        artificerUI.setSecurityHandler(createSecurityHandler(true));
        artificerUI.setContextPath("/artificer-ui");
        artificerUI.setWelcomeFiles(new String[]{"index.html"});
        artificerUI.setResourceBase(environment.getModuleDir("artificer-ui").getCanonicalPath());
        artificerUI.addFilter(GWTCacheControlFilter.class, "/app/*", EnumSet.of(DispatcherType.REQUEST));
        artificerUI.addFilter(GWTCacheControlFilter.class, "/rest/*", EnumSet.of(DispatcherType.REQUEST));
        artificerUI.addFilter(GWTCacheControlFilter.class, "/", EnumSet.of(DispatcherType.REQUEST));
        artificerUI.addFilter(GWTCacheControlFilter.class, "*.html", EnumSet.of(DispatcherType.REQUEST));
        artificerUI.addFilter(ResourceCacheControlFilter.class, "/css/*", EnumSet.of(DispatcherType.REQUEST));
        artificerUI.addFilter(ResourceCacheControlFilter.class, "/images/*", EnumSet.of(DispatcherType.REQUEST));
        artificerUI.addFilter(ResourceCacheControlFilter.class, "/js/*", EnumSet.of(DispatcherType.REQUEST));
        artificerUI.addFilter(LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        // Servlets
        artificerUI.addServlet(new ServletHolder(ArtifactUploadServlet.class), "/app/services/artifactUpload");
        artificerUI.addServlet(new ServletHolder(ArtifactDownloadServlet.class), "/app/services/artifactDownload");
        artificerUI.addServlet(new ServletHolder(OntologyUploadServlet.class), "/app/services/ontologyUpload");
        artificerUI.addServlet(new ServletHolder(OntologyDownloadServlet.class), "/app/services/ontologyDownload");
        artificerUI.addServlet(new ServletHolder(KeyCloakLogoutServlet.class), "/app/services/logout");
        ServletHolder resteasyUIServlet = new ServletHolder(new HttpServletDispatcher());
        resteasyUIServlet.setInitParameter("javax.ws.rs.Application", JettyArtificerApplication.class.getName());
        resteasyUIServlet.setInitParameter("resteasy.servlet.mapping.prefix", "/rest");
        artificerUI.addServlet(resteasyUIServlet, "/rest/*");
        // File resources
        ServletHolder resources = new ServletHolder(new MultiDefaultServlet());
        resources.setInitParameter("resourceBase", "/");
        resources.setInitParameter("resourceBases", environment.getModuleDir("artificer-ui").getCanonicalPath());
        resources.setInitParameter("dirAllowed", "true");
        resources.setInitParameter("pathInfoOnly", "false");
        String[] fileTypes = new String[] { "html", "js", "css", "png", "gif" };
        for (String fileType : fileTypes) {
            artificerUI.addServlet(resources, "*." + fileType);
        }

        /* *************
         * S-RAMP server
         * ************* */
        ServletContextHandler artificerServer = new ServletContextHandler(ServletContextHandler.SESSIONS);
        artificerServer.setSecurityHandler(createSecurityHandler(false));
        artificerServer.setContextPath("/artificer-server");
        artificerServer.addEventListener(new ArtificerLifeCycle());
        ServletHolder resteasyServerServlet = new ServletHolder(new HttpServletDispatcher());
        resteasyServerServlet.setInitParameter("javax.ws.rs.Application", ArtificerApplication.class.getName());
        artificerServer.addServlet(resteasyServerServlet, "/s-ramp/*");
        //maven repository servlet:
        ServletHolder mvnServlet = new ServletHolder(new MavenFacadeServlet());
        artificerServer.addServlet(mvnServlet, "/maven/repository/*");
        artificerServer.addServlet(mvnServlet, "/maven/repository");
        // TODO enable JSP support to test the repository listing

        artificerServer.addFilter(BasicAuthFilter.class, "/s-ramp/*", EnumSet.of(DispatcherType.REQUEST))
                .setInitParameter("allowedIssuers", "/artificer-ui,/dtgov,/dtgov-ui");
        artificerServer.addFilter(MavenRepositoryAuthFilter.class, "/maven/repository/*", EnumSet.of(DispatcherType.REQUEST))
                .setInitParameter("allowedIssuers", "/artificer-ui,/dtgov,/dtgov-ui");
        artificerServer.addFilter(org.artificer.server.filters.LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        artificerServer.addFilter(ServletCredentialsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        // Add to handlers
        handlers.addHandler(artificerUI);
        handlers.addHandler(artificerServer);
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

        ArtificerAtomApiClient client = new ArtificerAtomApiClient("http://localhost:"+serverPort()+"/artificer-server", "seeder", "seeder", true);

        String seedType = System.getProperty("artificer-dev-server.seed-type", "none");
        if ("switchyard".equals(seedType)) {
            doSwitchYardSeed(client);
        } else if ("standard".equals(seedType)) {
            doStandardSeed(client);
        } else {
            // no seeding
        }

        System.out.println("----------  DONE  ---------------");
        System.out.println("Now try:  \n  http://localhost:"+serverPort()+"/artificer-ui/index.html");
        System.out.println("---------------------------------");
    }

    /**
     * @param client
     * @throws org.artificer.atom.err.ArtificerAtomException
     * @throws org.artificer.client.ArtificerClientException
     */
    private void doStandardSeed(ArtificerAtomApiClient client) throws ArtificerClientException, ArtificerAtomException {
        InputStream is = null;

        // Ontology #1
        try {
            is = ArtificerDevServer.class.getResourceAsStream("colors.owl.xml");
            client.uploadOntology(is);
            System.out.println("Ontology 1 added");
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Ontology #2
        try {
            is = ArtificerDevServer.class.getResourceAsStream("regional.owl.xml");
            client.uploadOntology(is);
            System.out.println("Ontology 2 added");
        } finally {
            IOUtils.closeQuietly(is);
        }

        // PDF Document
        try {
            is = ArtificerDevServer.class.getResourceAsStream("sample.pdf");
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
            is = ArtificerDevServer.class.getResourceAsStream("order.xml");
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XmlDocument(), is, "order.xml");
            artifact.getClassifiedBy().add("http://www.example.org/colors.owl#Blue");
            ArtificerModelUtils.setCustomProperty(artifact, "foo", "bar");
            ArtificerModelUtils.setCustomProperty(artifact, "angle", "obtuse");
            client.updateArtifactMetaData(artifact);
            System.out.println("XML file added");
        } finally {
            IOUtils.closeQuietly(is);
        }

        // WSDL Document
        try {
            is = ArtificerDevServer.class.getResourceAsStream("deriver.wsdl");
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
    private void doSwitchYardSeed(ArtificerAtomApiClient client) throws Exception {
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
