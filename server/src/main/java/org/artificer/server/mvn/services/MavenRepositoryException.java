package org.artificer.server.mvn.services;


import org.artificer.common.error.ArtificerServerException;

/**
 * Exception wrapper used in the maven repository servlet
 *
 * @author David Virgil Naranjo
 */
public class MavenRepositoryException extends ArtificerServerException {

    public MavenRepositoryException(String message) {
        super(message);
    }

    public MavenRepositoryException(String message, Exception e) {
        super(message, e);
    }
}
