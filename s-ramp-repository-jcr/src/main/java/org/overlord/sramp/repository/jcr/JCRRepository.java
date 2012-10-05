/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository.jcr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.LoginException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.io.IOUtils;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.AnonymousCredentials;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRRepository {

    private static Logger log = LoggerFactory.getLogger(JCRRepository.class);

    //private static String USER           = "s-ramp";
    //private static char[] PWD            = "s-ramp".toCharArray();
    private static String WORKSPACE_NAME = "default";

    private static Repository repository = null;
    private static RepositoryFactory theFactory = null;

    /**
     * Gets the singleton instance of the JCR Repository.
     * @throws RepositoryException
     */
    private static synchronized Repository getInstance() throws RepositoryException {
        try {
            if (repository==null) {
                Map<String,String> parameters = new HashMap<String,String>();
                URL configUrl = getConfigurationUrl();
                RepositoryConfiguration config = RepositoryConfiguration.read(configUrl);
                Problems problems = config.validate();
                if (problems.hasErrors()) {
                    throw new RepositoryException(problems.toString());
                }
                parameters.put("org.modeshape.jcr.URL",configUrl.toExternalForm());
                for (RepositoryFactory factory : ServiceLoader.load(RepositoryFactory.class)) {
                    theFactory = factory;
                    repository = factory.getRepository(parameters);
                    if (repository != null) break;
                }
                if (repository==null) throw new RepositoryException("ServiceLoader could not instantiate JCR Repository");
                configureNodeTypes();
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        return repository;
    }

    /**
	 * Gets the configuration to use for the JCR repository.
     * @throws Exception
	 */
	private static URL getConfigurationUrl() throws Exception {
		// System properties can be used to set the config URL.  Note that
		// in the future we'll likely add more configuration types here, so that
		// the config URL can be specified in interesting ways (JNDI, properties
		// file on the classpath, etc).
		CompositeConfiguration config = new CompositeConfiguration();
		config.addConfiguration(new SystemConfiguration());

		String configUrlStr = config.getString("sramp.modeshape.config.url", "classpath://" + JCRRepository.class.getName() + "/META-INF/modeshape-configs/persistent-sramp-config.json");
		if (configUrlStr.startsWith("classpath:")) {
			Pattern p = Pattern.compile("classpath:/?/?([^/]*)/(.*)$");
			Matcher matcher = p.matcher(configUrlStr);
			if (matcher.matches()) {
				String className = matcher.group(1);
				String path = "/" + matcher.group(2);
				Class<?> clazz = Class.forName(className);
				URL resourceUrl = clazz.getResource(path);
				if (resourceUrl == null)
					throw new Exception("Failed to find config: " + configUrlStr);
				return resourceUrl;
			}
			throw new Exception("Invalid 'classpath' formatted URL: " + configUrlStr);
		} else {
			return new URL(configUrlStr);
		}
	}

	/**
     * TODO ModeShape requires shutdown to be called. However calling this after every test
     * leads to issues where the repo is down, on initialization of the next test. Not using
     * it leads to a successful build. We need to look into this.
     */
    public static void shutdown(){
        if (theFactory instanceof org.modeshape.jcr.JcrRepositoryFactory) {
            try {
                org.modeshape.jcr.api.RepositoryFactory modeRepo =
                (org.modeshape.jcr.api.RepositoryFactory)theFactory;
                Boolean state = modeRepo.shutdown().get();
                log.info("called shutdown on ModeShape, with resulting state=" + state);
                repository = null;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Convenience method for getting a JCR session from the repo singleton.
     * @throws LoginException
     * @throws NoSuchWorkspaceException
     * @throws RepositoryException
     */
    public static Session getSession() throws LoginException, NoSuchWorkspaceException, RepositoryException {
        //Credentials cred = new SimpleCredentials(USER, PWD);
        AnonymousCredentials cred = new AnonymousCredentials();
        return getInstance().login(cred, WORKSPACE_NAME);
    }

    /**
     * Quietly logs out of the JCR session.
     * @param session
     */
    public static void logoutQuietly(Session session) {
    	if (session != null) {
    		try {
    			session.logout();
    		} catch (Throwable t) {
    			// eat it
    		}
    	}
    }

    /**
     * Called to configure the custom JCR node types.
     */
    private static void configureNodeTypes() throws RepositoryException {
        Session session = null;
        InputStream is = null;
        try {
            session = JCRRepository.getSession();

            // Register some namespaces.
            NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
            namespaceRegistry.registerNamespace(JCRConstants.SRAMP, JCRConstants.SRAMP_NS);
            namespaceRegistry.registerNamespace(JCRConstants.SRAMP_PROPERTIES, JCRConstants.SRAMP_PROPERTIES_NS);

            NodeTypeManager manager = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();

            // Register the ModeShape S-RAMP node types ...
            is = JCRRepository.class.getResourceAsStream("/org/overlord/s-ramp/sramp.cnd");
            manager.registerNodeTypes(is,true);
        } catch (LoginException e) {
            throw e;
        } catch (NoSuchWorkspaceException e) {
            throw e;
        } catch (RepositoryException e) {
            throw e;
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (RuntimeException e) {
            throw e;
        } finally {
        	IOUtils.closeQuietly(is);
        	JCRRepository.logoutQuietly(session);
        }
    }
}
