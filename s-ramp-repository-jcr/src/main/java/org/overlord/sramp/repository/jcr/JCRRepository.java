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

import java.io.File;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.AnonymousCredentials;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRRepository {

    private static Logger log = LoggerFactory.getLogger(JCRRepository.class);
    private static String WORKSPACE_NAME = "default";

    private static JCRRepository instance;
    public static synchronized JCRRepository getInstance() throws RepositoryException {
    	if (instance == null) {
    		instance = new JCRRepository();
    		instance.startup();
    	}
    	return instance;
    }
    public static synchronized void destroy() {
		try {
			getInstance().shutdown();
		} catch (RepositoryException e) {
			log.error("Failed to shut down ModeShape.", e);
		}
    	instance = null;
    }

    private Repository repository = null;
    private RepositoryFactory theFactory = null;
    private File tempConfigDir;

    /**
	 * Constructor.
	 */
	public JCRRepository() {
	}

	/**
	 * Gets the underlying JCR repository.
	 */
	public Repository get() {
		return repository;
	}

    /**
     * Starts up the JCR repository.
     * @throws RepositoryException
     */
    private void startup() throws RepositoryException {
        try {
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
			if (repository == null) {
				throw new RepositoryException("ServiceLoader could not instantiate JCR Repository");
			}
			configureNodeTypes();
        } catch (RepositoryException e) {
        	throw e;
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
			if (this.tempConfigDir != null && this.tempConfigDir.isDirectory()) {
				FileUtils.deleteQuietly(tempConfigDir);
			}
        }
    }

    /**
	 * Gets the configuration to use for the JCR repository.
     * @throws Exception
	 */
	private URL getConfigurationUrl() throws Exception {
		// System properties can be used to set the config URL.  Note that
		// in the future we'll likely add more configuration types here, so that
		// the config URL can be specified in interesting ways (JNDI, properties
		// file on the classpath, etc).
		CompositeConfiguration config = new CompositeConfiguration();
		config.addConfiguration(new SystemConfiguration());

		String configUrl = config.getString("sramp.modeshape.config.url", null);
		if (configUrl == null) {
			configUrl = generateConfig();
		}
		if (configUrl.startsWith("classpath:")) {
			Pattern p = Pattern.compile("classpath:/?/?([^/]*)/(.*)$");
			Matcher matcher = p.matcher(configUrl);
			if (matcher.matches()) {
				String className = matcher.group(1);
				String path = "/" + matcher.group(2);
				Class<?> clazz = Class.forName(className);
				URL resourceUrl = clazz.getResource(path);
				if (resourceUrl == null)
					throw new Exception("Failed to find config: " + configUrl);
				return resourceUrl;
			}
			throw new Exception("Invalid 'classpath' formatted URL: " + configUrl);
		} else {
			return new URL(configUrl);
		}
	}

	/**
	 * Generates the configuration files used to create and initialize modeshape.
	 */
	private String generateConfig() throws Exception {
		this.tempConfigDir = File.createTempFile("s-ramp-modeshape-config", "dir");
		if (this.tempConfigDir.isFile()) {
			this.tempConfigDir.delete();
		}
		this.tempConfigDir.mkdirs();

		URL msConfigUrl = getClass().getResource("/META-INF/modeshape-configs/persistent-sramp-config.json");
		URL isConfigUrl = getClass().getResource("/META-INF/modeshape-configs/infinispan-configuration.xml");

		String msConfig = IOUtils.toString(msConfigUrl);
		String isConfig = IOUtils.toString(isConfigUrl);

		File dataDir = determineRuntimeDataDir();
		File tempModeShapeConfigFile = new File(tempConfigDir, "modeshape-config.json");
		File tempInfinispanConfigFile = new File(tempConfigDir, "infinispan-configuration.xml");

		msConfig = msConfig.replace("${modeshape.jcr.datadir}", dataDir.getCanonicalPath());
		msConfig = msConfig.replace("${modeshape.cache.config.url}", tempInfinispanConfigFile.getCanonicalPath());
		isConfig = isConfig.replace("${modeshape.jcr.datadir}", dataDir.getCanonicalPath());

		FileUtils.writeStringToFile(tempModeShapeConfigFile, msConfig);
		FileUtils.writeStringToFile(tempInfinispanConfigFile, isConfig);

		return tempModeShapeConfigFile.toURI().toURL().toString();
	}

	/**
	 * Figures out what the current data directory is.  The data directory will be
	 * different depending on where we're running.  In an application server this
	 * code should stive to detect the app server specific data dir.
	 */
	private File determineRuntimeDataDir() {
		// Our property takes precedent if present
		String rootDataDir = System.getProperty("s-ramp.jcr.data.dir");
		// Check for JBoss
		if (rootDataDir == null) {
			rootDataDir = System.getProperty("jboss.server.data.dir");
		}
		// Default to "data/"
		if (rootDataDir == null) {
			rootDataDir = "data";
		}

		File root = new File(rootDataDir);
		File srampDataDir = new File(root, "s-ramp");
		if (!srampDataDir.exists()) {
			srampDataDir.mkdirs();
		}

		return srampDataDir;
	}

	/**
     * TODO ModeShape requires shutdown to be called. However calling this after every test
     * leads to issues where the repo is down, on initialization of the next test. Not using
     * it leads to a successful build. We need to look into this.
     */
	private void shutdown() {
		if (theFactory instanceof org.modeshape.jcr.JcrRepositoryFactory) {
			try {
				org.modeshape.jcr.api.RepositoryFactory modeRepo = (org.modeshape.jcr.api.RepositoryFactory) theFactory;
				Boolean state = modeRepo.shutdown().get();
				log.info("called shutdown on ModeShape, with resulting state=" + state);
				repository = null;
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			} catch (ExecutionException e) {
				log.error(e.getMessage(), e);
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
        return getInstance().get().login(cred, WORKSPACE_NAME);
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
    private void configureNodeTypes() throws RepositoryException {
        Session session = null;
        InputStream is = null;
        try {
            session = JCRRepository.getSession();

            // Register some namespaces.
            NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
            namespaceRegistry.registerNamespace(JCRConstants.SRAMP, JCRConstants.SRAMP_NS);
            namespaceRegistry.registerNamespace(JCRConstants.SRAMP_PROPERTIES, JCRConstants.SRAMP_PROPERTIES_NS);
            namespaceRegistry.registerNamespace(JCRConstants.SRAMP_RELATIONSHIPS, JCRConstants.SRAMP_RELATIONSHIPS_NS);

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
