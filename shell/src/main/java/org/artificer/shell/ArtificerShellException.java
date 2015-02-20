package org.artificer.shell;

/**
 * Thrown by Commands and utilities as a mechanism for early execution exit.
 *
 * @author Brett Meyer
 */
public class ArtificerShellException extends Exception {

    public ArtificerShellException(String message) {
        super(message);
    }
}
