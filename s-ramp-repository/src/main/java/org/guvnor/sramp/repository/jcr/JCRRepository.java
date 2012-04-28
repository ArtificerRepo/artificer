package org.guvnor.sramp.repository.jcr;

import static org.modeshape.jcr.api.observation.Event.NODE_SEQUENCED;

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

import org.modeshape.jcr.api.AnonymousCredentials;

public class JCRRepository {

    //private static String USER           = "s-ramp";
    //private static char[] PWD            = "s-ramp".toCharArray();
    private static String WORKSPACE_NAME = "default";
    
    private static Repository repository = null;
    private static SequencingListener listener = null;
    
    private static Repository getInstance() throws RepositoryException {
        if (repository==null) {
            Map<String,String> parameters = new HashMap<String,String>();
            String configUrl = Repository.class.getClassLoader().getResource("modeshape-config.json").toExternalForm();
            parameters.put("org.modeshape.jcr.URL",configUrl);
            for (RepositoryFactory factory : ServiceLoader.load(RepositoryFactory.class)) {
                repository = factory.getRepository(parameters);
                if (repository != null) break;
            }
            if (repository==null) throw new RepositoryException("ServiceLoader could not instantiate JCR Repository");
        }
        getListener(); //create the listener
        return repository;
    }
    
    public static SequencingListener getListener() throws UnsupportedRepositoryOperationException, LoginException, NoSuchWorkspaceException, RepositoryException {
        if (listener == null) {
            listener = new SequencingListener();
            ((Workspace) getSession().getWorkspace()).getObservationManager().addEventListener(listener,
                    NODE_SEQUENCED,
                    null,
                    true,
                    null,
                    null,
                    false);
        }
        return listener;
    }
    
    public static Session getSession() throws LoginException, NoSuchWorkspaceException, RepositoryException {
        //Credentials cred = new SimpleCredentials(USER, PWD);
        AnonymousCredentials cred = new AnonymousCredentials();
        return getInstance().login(cred, WORKSPACE_NAME);
    }
    
    
}
