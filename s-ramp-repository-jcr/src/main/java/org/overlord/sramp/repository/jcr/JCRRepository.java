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

import javax.jcr.LoginException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.infinispan.schematic.document.ParsingException;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.AnonymousCredentials;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;

public class JCRRepository {

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
                URL configUrl = Repository.class.getClassLoader().getResource("modeshape-sramp-config.json");
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
                else {
                    
                }
                configureNodeTypes();
            }
        } catch (ParsingException e) {
            new RepositoryException(e);
        }
        
        return repository;
    }
    
    /**
     * TODO ModeShape requires shutdown to be called. However calling this after every test
     * leads to issues where the repo is down, on initialization of the next test. Not using 
     * it leads to a successful build. We need to look into this.
     */
    public static void shutdown(){
        if (theFactory instanceof org.modeshape.jcr.api.RepositoryFactory) {
            try {
                ((org.modeshape.jcr.api.RepositoryFactory)theFactory).shutdown().get();
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
            if ( session != null ) session.logout();
        }
    }
}
