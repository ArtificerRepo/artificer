package org.artificer.server.mvn.services;


/**
 * Exception wrapper used in the maven repository servlet
 *
 * @author David Virgil Naranjo
 */
public class MavenRepositoryException extends Exception {

    public MavenRepositoryException(String message) {
        super(message);
    }

    public MavenRepositoryException(String message, Exception e) {
        super(message, e);
    }
}
