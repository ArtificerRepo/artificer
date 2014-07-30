package org.overlord.sramp.server.mvn.services;


/**
 * Exception Wrapper used in the maven repository service and servlet
 *
 * @author David Virgil Naranjo
 */
public class MavenRepositoryException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -3247395547761663658L;

    public MavenRepositoryException() {
        super();
    }

    public MavenRepositoryException(String message) {
        super(message);
    }

    public MavenRepositoryException(String message, Exception e) {
        super(message, e);
    }
}
