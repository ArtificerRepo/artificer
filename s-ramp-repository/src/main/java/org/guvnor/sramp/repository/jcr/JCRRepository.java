package org.guvnor.sramp.repository.jcr;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

public class JCRRepository {

    private static String USER           = "s-ramp";
    private static char[] PWD            = "s-ramp".toCharArray();
    private static String WORKSPACE_NAME = "s-ramp";
    
    private static Repository repository = null;
    
    public static Repository getInstance() throws RepositoryException {
        if (repository==null) {
            Map<String,String> parameters = new HashMap<String,String>();
            for (RepositoryFactory factory : ServiceLoader.load(RepositoryFactory.class)) {
                repository = factory.getRepository(parameters);
                if (repository != null) break;
            }
            if (repository==null) throw new RepositoryException("Could not instantiate JCR Repository");
        }
        return repository;
    }
    
    public static Session getSession() throws LoginException, NoSuchWorkspaceException, RepositoryException {
        Credentials cred = new SimpleCredentials(USER, PWD);
        return getInstance().login(cred, WORKSPACE_NAME);
    }
}
