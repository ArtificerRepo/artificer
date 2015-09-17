package org.artificer.shell;

import org.artificer.common.ArtificerException;

/**
 * Thrown by Commands and utilities as a mechanism for early execution exit.
 *
 * @author Brett Meyer
 */
public class ArtificerShellException extends ArtificerException {

    public ArtificerShellException(String message) {
        super(message);
    }
}
