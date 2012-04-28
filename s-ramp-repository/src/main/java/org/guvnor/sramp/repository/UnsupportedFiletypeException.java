package org.guvnor.sramp.repository;

public class UnsupportedFiletypeException extends Exception {

    private static final long serialVersionUID = -1205817784608428279L;

    public UnsupportedFiletypeException() {
        super();
    }

    public UnsupportedFiletypeException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public UnsupportedFiletypeException(String arg0) {
        super(arg0);
    }

    public UnsupportedFiletypeException(Throwable arg0) {
        super(arg0);
    }

}
