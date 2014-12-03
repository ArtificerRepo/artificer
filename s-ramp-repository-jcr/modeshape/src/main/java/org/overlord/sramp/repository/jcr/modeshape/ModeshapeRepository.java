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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.overlord.commons.services.ServiceRegistryUtil;
import org.overlord.sramp.common.SrampConfig;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.repository.jcr.JCRConstants;
import org.overlord.sramp.repository.jcr.JCRExtensions;
import org.overlord.sramp.repository.jcr.JCRRepository;
import org.overlord.sramp.repository.jcr.JCRRepositoryFactory;
import org.overlord.sramp.repository.jcr.modeshape.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModeshapeRepository extends JCRRepository {

	private static Logger log = LoggerFactory.getLogger(ModeshapeRepository.class);
	private static String S_RAMP_JNDI = "jcr/sramp";

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
	@Override
    public Repository getRepo() {
		return repository;
	}

	/**
	 * @see org.overlord.sramp.repository.jcr.JCRRepository#doStartup()
	 */
	@Override
	protected void doStartup() throws RepositoryException {
	    URL configUrl = null;
	    try {
	        configUrl = getModeshapeConfigurationUrl();
	    } catch (Exception e) {
	        log.error(e.getMessage(),e);
	    }
	    //Using the Modeshape Service
	    if (configUrl==null) {
	        String srampJndiLocation = SrampConfig.getConfigProperty(SrampConstants.SRAMP_CONFIG_JCR_REPO_JNDI, S_RAMP_JNDI);
            log.info(Messages.i18n.format("CONNECT_TO_MS", srampJndiLocation));
            try {
                InitialContext context = new InitialContext();
                repository = (javax.jcr.Repository) context.lookup(srampJndiLocation);
            } catch (NamingException e) {
                throw new RepositoryException(e.getMessage(),e);
            }
            if (repository==null) {
                throw new RepositoryException(Messages.i18n.format("JNDI_BINDING_NOT_FOUND", srampJndiLocation)); 
            }
	    }
	    //Using Modeshape embedded
	    else {
	        log.info(Messages.i18n.format("STARTING_MS"));
    		try {
    			Map<String,String> parameters = new HashMap<String,String>();
    			RepositoryConfiguration config = RepositoryConfiguration.read(configUrl);
    			Problems problems = config.validate();
    			if (problems.hasErrors()) {
    				throw new RepositoryException(problems.toString());
    			}
    			parameters.put("org.modeshape.jcr.URL",configUrl.toExternalForm());
    			Set<RepositoryFactory> services = ServiceRegistryUtil.getServices(RepositoryFactory.class);
    			if (services.isEmpty())
                    throw new RepositoryException(Messages.i18n.format("FAILED_TO_CREATE_JCR_REPO"));
    			for (RepositoryFactory factory : services) {
                    theFactory = factory;
                    repository = factory.getRepository(parameters);
                    if (repository != null)
                        break;
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
		String configUrl = SrampConfig.getConfigProperty("sramp.modeshape.config.url", null);
		if (configUrl == null) {
			return null;
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
					throw new Exception(Messages.i18n.format("MISSING_CONFIG", configUrl));
				return resourceUrl;
			}
			throw new Exception(Messages.i18n.format("INVALID_CLASSPATH_URL", configUrl));
		} else {
		    try {
                File f = new File(configUrl);
                if (f.isFile()) {
                    return f.toURI().toURL();
                }
            } catch (Exception e) {
                // eat the error and try the next option
            }

			try {
                return new URL(configUrl);
            } catch (Exception e) {
                // eat the error and try the next option
            }

			return null;
		}
	}

	/**
	 * Called to configure the custom JCR node types.
	 */
	private void configureNodeTypes() throws RepositoryException {
		Session session = null;
		InputStream is = null;
		try {
			session = JCRRepositoryFactory.getSession();

			// Register some namespaces.
			NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
			namespaceRegistry.registerNamespace(JCRConstants.SRAMP, JCRConstants.SRAMP_NS);
			namespaceRegistry.registerNamespace(JCRConstants.SRAMP_PROPERTIES, JCRConstants.SRAMP_PROPERTIES_NS);
			namespaceRegistry.registerNamespace(JCRConstants.SRAMP_RELATIONSHIPS, JCRConstants.SRAMP_RELATIONSHIPS_NS);
            namespaceRegistry.registerNamespace(JCRConstants.SRAMP_AUDIT, JCRConstants.SRAMP_AUDIT_NS);
            namespaceRegistry.registerNamespace(JCRConstants.SRAMP_OTHER_ATTRIBUTES, JCRConstants.SRAMP_OTHER_ATTRIBUTES_NS);

			NodeTypeManager manager = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();

			// Register the ModeShape S-RAMP node types ...
			is = ModeshapeRepository.class.getClassLoader().getResourceAsStream("org/overlord/sramp/repository/jcr/modeshape/sramp.cnd");
			if (is == null) {
			    throw new RuntimeException(Messages.i18n.format("CND_NOT_FOUND"));
			}
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

	/**
	 * @see org.overlord.sramp.repository.jcr.JCRRepository#doShutdown()
	 */
	@Override
	protected void doShutdown() {
        if (theFactory!=null && theFactory instanceof org.modeshape.jcr.JcrRepositoryFactory) {
            try {
                org.modeshape.jcr.api.RepositoryFactory modeRepo = (org.modeshape.jcr.api.RepositoryFactory) theFactory;
                Boolean state = modeRepo.shutdown().get();
                log.info(Messages.i18n.format("SHUTDOWN_MS", state));
                repository = null;
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @see org.overlord.sramp.repository.jcr.JCRRepository#getExtensions()
     */
    @Override
    public JCRExtensions getExtensions() {
        return new ModeshapeJCRExtensions();
    }
}
