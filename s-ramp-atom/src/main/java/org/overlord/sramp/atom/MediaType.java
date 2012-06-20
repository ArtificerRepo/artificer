package org.overlord.sramp.atom;

import java.util.Map;

public class MediaType extends javax.ws.rs.core.MediaType {

    /** "application/zip" */
    public final static String APPLICATION_ZIP = "application/zip";
    
    /** "application/atom+xml;type=entry" */
    public final static String APPLICATION_ATOM_XML_ENTRY = "application/atom+xml;type=entry";
    public final static MediaType APPLICATION_ATOM_XML_ENTRY_TYPE = new MediaType("application", "atom+xml;type=entry");

    /** "application/atom+xml;charset=utf-8" */
    public final static String APPLICATION_ATOM_XML_UTF8 = "application/atom+xml;charset=utf-8";
    public final static MediaType APPLICATION_ATOM_XML_UTF8_TYPE = new MediaType("application", "atom+xml;charset=utf-8");

    public MediaType() {
        super();
    }
    public MediaType(String type, String subtype, Map<String, String> parameters) {
        super(type, subtype, parameters);
    }
    public MediaType(String type, String subtype) {
        super(type, subtype);
    }
    
}
