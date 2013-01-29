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
package org.overlord.sramp.repository.jcr.modeshape;

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

import javax.annotation.Resource;
import javax.jcr.Credentials;
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
import org.overlord.sramp.repository.jcr.JCRConstants;
import org.overlord.sramp.repository.jcr.JCRRepository;
import org.overlord.sramp.repository.jcr.JCRRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModeshapeRepository extends JCRRepository {

	private static Logger log = LoggerFactory.getLogger(ModeshapeRepository.class);

	@Resource(mappedName="java:/jcr/sramp")
	private Repository repository;
	
	private RepositoryFactory theFactory = null;
	private File tempConfigDir;

	/**
	 * Constructor.
	 */
	public ModeshapeRepository() {
	}

	/**
	 * Gets the underlying JCR repository.
	 */
	public Repository getRepo() {
		return repository;
	}

	/**
	 * Starts up the JCR repository.
	 * @throws RepositoryException
	 */
	@Override
	public void startup() throws RepositoryException {
	    if (repository==null) { //skip starting if we use the modeshape service
    		try {
    			Map<String,String> parameters = new HashMap<String,String>();
    			URL configUrl = getModeshapeConfigurationUrl();
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
		configureNodeTypes();
	}

	/**
	 * Gets the configuration to use for the JCR repository.
	 * @throws Exception
	 */
	private URL getModeshapeConfigurationUrl() throws Exception {
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

		msConfig = msConfig.replace("${modeshape.jcr.datadir}", dataDir.getCanonicalPath().replace("\\", "\\\\"));
		msConfig = msConfig.replace("${modeshape.cache.config.url}", tempInfinispanConfigFile.getCanonicalPath().replace("\\", "\\\\"));
		isConfig = isConfig.replace("${modeshape.jcr.datadir}", dataDir.getCanonicalPath().replace("\\", "\\\\"));

		FileUtils.writeStringToFile(tempModeShapeConfigFile, msConfig);
		FileUtils.writeStringToFile(tempInfinispanConfigFile, isConfig);

		return tempModeShapeConfigFile.toURI().toURL().toString();
	}

	

	/**
	 * TODO ModeShape requires shutdown to be called. However calling this after every test
	 * leads to issues where the repo is down, on initialization of the next test. Not using
	 * it leads to a successful build. We need to look into this.
	 */
	public void shutdown() {
		if (theFactory!=null && theFactory instanceof org.modeshape.jcr.JcrRepositoryFactory) {
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
	 * Called to configure the custom JCR node types.
	 */
	private void configureNodeTypes() throws RepositoryException {
		Session session = null;
		InputStream is = null;
		try {
			session = JCRRepositoryFactory.getAnonymousSession();

			// Register some namespaces.
			NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
			namespaceRegistry.registerNamespace(JCRConstants.SRAMP, JCRConstants.SRAMP_NS);
			namespaceRegistry.registerNamespace(JCRConstants.SRAMP_PROPERTIES, JCRConstants.SRAMP_PROPERTIES_NS);
			namespaceRegistry.registerNamespace(JCRConstants.SRAMP_RELATIONSHIPS, JCRConstants.SRAMP_RELATIONSHIPS_NS);

			NodeTypeManager manager = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();

			// Register the ModeShape S-RAMP node types ...
			is = ModeshapeRepository.class.getResourceAsStream("/org/overlord/s-ramp/sramp.cnd");
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
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

    @Override
    public Credentials getAnonymousCredentials() {
        AnonymousCredentials cred = new AnonymousCredentials();
        return cred;
    }
}
