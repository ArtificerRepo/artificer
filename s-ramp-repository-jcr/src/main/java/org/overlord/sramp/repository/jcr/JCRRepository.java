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

import static org.modeshape.jcr.api.observation.Event.Sequencing.NODE_SEQUENCED;
import static org.overlord.sramp.repository.jcr.JCRConstants.OVERLORD;
import static org.overlord.sramp.repository.jcr.JCRConstants.OVERLORD_NS;
import static org.overlord.sramp.repository.jcr.JCRConstants.OVERLORD_ARTIFACT;
import static org.overlord.sramp.repository.jcr.JCRConstants.SRAMP_PROPERTIES;
import static org.overlord.sramp.repository.jcr.JCRConstants.SRAMP_PROPERTIES_NS;
import static org.overlord.sramp.repository.jcr.JCRConstants.SRAMP_UUID;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;

import org.apache.commons.io.IOUtils;
import org.modeshape.jcr.api.AnonymousCredentials;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;

public class JCRRepository {

    //private static String USER           = "s-ramp";
    //private static char[] PWD            = "s-ramp".toCharArray();
    private static String WORKSPACE_NAME = "default";
    
    private static Repository repository = null;
    private static SequencingListener listener = null;
    
    /**
     * Gets the singleton instance of the JCR Repository.
     * @throws RepositoryException
     */
    private static synchronized Repository getInstance() throws RepositoryException {
        if (repository==null) {
            Map<String,String> parameters = new HashMap<String,String>();
            String configUrl = Repository.class.getClassLoader().getResource("modeshape-config.json").toExternalForm();
            parameters.put("org.modeshape.jcr.URL",configUrl);
            for (RepositoryFactory factory : ServiceLoader.load(RepositoryFactory.class)) {
                repository = factory.getRepository(parameters);
                if (repository != null) break;
            }
            if (repository==null) throw new RepositoryException("ServiceLoader could not instantiate JCR Repository");
            getListener(); //create the listener
            configureNodeTypes();
        }
        return repository;
    }
    
    /**
     * Gets the sequencing listener.
     * @throws UnsupportedRepositoryOperationException
     * @throws LoginException
     * @throws NoSuchWorkspaceException
     * @throws RepositoryException
     */
    public static SequencingListener getListener() throws UnsupportedRepositoryOperationException, LoginException, NoSuchWorkspaceException, RepositoryException {
        if (listener == null) {
            Session session = null;
            try {
                session = JCRRepository.getSession();
	            listener = new SequencingListener();
	            ((Workspace) session.getWorkspace()).getObservationManager().addEventListener(listener,
	                    NODE_SEQUENCED,
	                    null,
	                    true,
	                    null,
	                    null,
	                    false);
            } finally {
				if (session != null)
					session.logout();
            }
        }
        return listener;
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
        Session session = getInstance().login(cred, WORKSPACE_NAME);
        session.setNamespacePrefix(OVERLORD, OVERLORD_NS);
        session.setNamespacePrefix(SRAMP_PROPERTIES, SRAMP_PROPERTIES_NS);
		return session;
    }

    /**
     * Called to configure the custom JCR node types.
     */
    private static void configureNodeTypes() throws RepositoryException {
        Session session = null;
        InputStream is = null;
        try {
            session = JCRRepository.getSession();
            NodeTypeManager manager = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();
            
            if (! manager.hasNodeType(SRAMP_UUID)) {
                // Register the ModeShape S-RAMP node types ...
                is = JCRRepository.class.getResourceAsStream("/org/modeshape/sequencer/sramp/sramp.cnd");
                manager.registerNodeTypes(is,true);
            }
            if (! manager.hasNodeType(OVERLORD_ARTIFACT)) {
                // Register the Overlord node types ...
                is = JCRRepository.class.getResourceAsStream("/org/overlord/s-ramp/overlord.cnd");
                manager.registerNodeTypes(is,true);
            }
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
