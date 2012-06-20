package org.overlord.sramp.repository;

public class RepositoryException extends Exception {

    private static final long serialVersionUID = -1205817784608428279L;

    public RepositoryException() {
        super();
    }

    public RepositoryException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public RepositoryException(String arg0) {
        super(arg0);
    }

    public RepositoryException(Throwable arg0) {
        super(arg0);
    }

}
