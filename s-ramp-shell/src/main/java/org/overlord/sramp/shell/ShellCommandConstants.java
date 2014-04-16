package org.overlord.sramp.shell;

/**
 * Constants class that stores all the shell namespace names and all the command
 * names.
 * 
 * @author David Virgil Naranjo
 */
public interface ShellCommandConstants {

    public final static String SEPARATOR = ":";

    // Namespaces constants
    public final static String NAMESPACE_SRAMP = "s-ramp";
    public final static String NAMESPACE_ARCHIVE = "archive";
    public final static String NAMESPACE_ONTOLOGY = "ontology";
    public final static String NAMESPACE_AUDIT = "audit";
    public final static String NAMESPACE_MAVEN = "maven";

    public interface Sramp {
        // COMMANDS
        public final static String COMMAND_CONNECT = "connect";
        public final static String COMMAND_DISCONNECT = "disconnect";
        public final static String COMMAND_STATUS = "status";
        public final static String COMMAND_QUERY = "query";
        public final static String COMMAND_GET_METADATA = "getMetaData";
        public final static String COMMAND_GET_CONTENT = "getContent";
        public final static String COMMAND_UPLOAD = "upload";
        public final static String COMMAND_UPDATE_METADATA = "updateMetaData";
        public final static String COMMAND_UPDATE_CONTENT = "updateContent";
        public final static String COMMAND_PROPERTY = "property";
        public final static String COMMAND_CLASSIFICATION = "classification";
        public final static String COMMAND_SHOW_METADATA = "showMetaData";
        public final static String COMMAND_REFRESH_METADATA = "refreshMetaData";
        public final static String COMMAND_DELETE = "delete";
        public final static String COMMAND_CREATE = "create";
        public final static String COMMAND_HELP = "help";
        public final static String COMMAND_EXIT = "exit";
        public final static String COMMAND_QUIT = "quit";

        // CORE COMMAND NAMES:
        public final static String S_RAMP_COMMAND_CONNECT = NAMESPACE_SRAMP + SEPARATOR + COMMAND_CONNECT;
        public final static String S_RAMP_COMMAND_DISCONNECT = NAMESPACE_SRAMP + SEPARATOR
                + COMMAND_DISCONNECT;
        public final static String S_RAMP_COMMAND_STATUS = NAMESPACE_SRAMP + SEPARATOR + COMMAND_STATUS;
        public final static String S_RAMP_COMMAND_QUERY = NAMESPACE_SRAMP + SEPARATOR + COMMAND_QUERY;
        public final static String S_RAMP_COMMAND_GET_METADATA = NAMESPACE_SRAMP + SEPARATOR
                + COMMAND_GET_METADATA;
        public final static String S_RAMP_COMMAND_GET_CONTENT = NAMESPACE_SRAMP + SEPARATOR
                + COMMAND_GET_CONTENT;
        public final static String S_RAMP_COMMAND_UPLOAD = NAMESPACE_SRAMP + SEPARATOR + COMMAND_UPLOAD;
        public final static String S_RAMP_COMMAND_UPDATE_METADATA = NAMESPACE_SRAMP + SEPARATOR
                + COMMAND_UPDATE_METADATA;
        public final static String S_RAMP_COMMAND_UPDATE_CONTENT = NAMESPACE_SRAMP + SEPARATOR
                + COMMAND_UPDATE_CONTENT;
        public final static String S_RAMP_COMMAND_PROPERTY = NAMESPACE_SRAMP + SEPARATOR + COMMAND_PROPERTY;
        public final static String S_RAMP_COMMAND_CLASSIFICATION = NAMESPACE_SRAMP + SEPARATOR
                + COMMAND_CLASSIFICATION;
        public final static String S_RAMP_COMMAND_SHOW_METADATA = NAMESPACE_SRAMP + SEPARATOR
                + COMMAND_SHOW_METADATA;
        public final static String S_RAMP_COMMAND_REFRESH_METADATA = NAMESPACE_SRAMP + SEPARATOR
                + COMMAND_REFRESH_METADATA;
        public final static String S_RAMP_COMMAND_DELETE = NAMESPACE_SRAMP + SEPARATOR + COMMAND_DELETE;
        public final static String S_RAMP_COMMAND_CREATE = NAMESPACE_SRAMP + SEPARATOR + COMMAND_CREATE;
        public final static String S_RAMP_COMMAND_HELP = NAMESPACE_SRAMP + SEPARATOR + COMMAND_HELP;
        public final static String S_RAMP_COMMAND_QUIT = NAMESPACE_SRAMP + SEPARATOR + COMMAND_QUIT;
        public final static String S_RAMP_COMMAND_EXIT = NAMESPACE_SRAMP + SEPARATOR + COMMAND_EXIT;

    }

    public interface Audit {
        // COMMANDS
        public final static String COMMAND_SHOW_AUDIT_TRAIL = "showAuditTrail";

        // AUDIT COMMAND NAMES
        public final static String AUDIT_COMMAND_SHOW_AUDIT_TRAIL = NAMESPACE_AUDIT + SEPARATOR
                + COMMAND_SHOW_AUDIT_TRAIL;
    }

    public interface Maven {
        // COMMANDS
        public final static String COMMAND_DEPLOY = "deploy";
        // MAVEN COMMAND NAMES
        public final static String MAVEN_COMMAND_DEPLOY = NAMESPACE_MAVEN + SEPARATOR + COMMAND_DEPLOY;
    }

    public interface Ontology {
        // COMMANDS
        public final static String COMMAND_GET = "get";
        public final static String COMMAND_UPDATE = "update";
        public final static String COMMAND_DEPLOY = "deploy";
        public final static String COMMAND_UPLOAD = "upload";
        public final static String COMMAND_DELETE = "delete";
        public final static String COMMAND_LIST = "list";

        // ONTOLOGY COMMAND NAMES
        public final static String ONTOLOGY_COMMAND_UPLOAD = NAMESPACE_ONTOLOGY + SEPARATOR + COMMAND_UPLOAD;
        public final static String ONTOLOGY_COMMAND_LIST = NAMESPACE_ONTOLOGY + SEPARATOR + COMMAND_LIST;
        public final static String ONTOLOGY_COMMAND_DELETE = NAMESPACE_ONTOLOGY + SEPARATOR + COMMAND_DELETE;
        public final static String ONTOLOGY_COMMAND_GET = NAMESPACE_ONTOLOGY + SEPARATOR + COMMAND_GET;
        public final static String ONTOLOGY_COMMAND_UPDATE = NAMESPACE_ONTOLOGY + SEPARATOR + COMMAND_UPDATE;
    }

    public interface Archive {
        // COMMANDS
        public final static String COMMAND_NEW = "new";
        public final static String COMMAND_OPEN = "open";
        public final static String COMMAND_CLOSE = "close";
        public final static String COMMAND_LIST = "list";
        public final static String COMMAND_ADD_ENTRY = "addEntry";
        public final static String COMMAND_UPDATE_ENTRY = "updateEntry";
        public final static String COMMAND_UPLOAD = "upload";
        public final static String COMMAND_LIST_ENTRY = "listEntry";
        public final static String COMMAND_REMOVE_ENTRY = "removeEntry";
        public final static String COMMAND_PACK = "pack";

        // ARCHIVE COMMAND NAMES
        public final static String ARCHIVE_COMMAND_NEW = NAMESPACE_ARCHIVE + SEPARATOR + COMMAND_NEW;
        public final static String ARCHIVE_COMMAND_OPEN = NAMESPACE_ARCHIVE + SEPARATOR + COMMAND_OPEN;
        public final static String ARCHIVE_COMMAND_CLOSE = NAMESPACE_ARCHIVE + SEPARATOR + COMMAND_CLOSE;
        public final static String ARCHIVE_COMMAND_LIST = NAMESPACE_ARCHIVE + SEPARATOR + COMMAND_LIST;
        public final static String ARCHIVE_COMMAND_ADD_ENTRY = NAMESPACE_ARCHIVE + SEPARATOR
                + COMMAND_ADD_ENTRY;
        public final static String ARCHIVE_COMMAND_UPDATE_ENTRY = NAMESPACE_ARCHIVE + SEPARATOR
                + COMMAND_UPDATE_ENTRY;
        public final static String ARCHIVE_COMMAND_REMOVE_ENTRY = NAMESPACE_ARCHIVE + SEPARATOR
                + COMMAND_REMOVE_ENTRY;
        public final static String ARCHIVE_COMMAND_LIST_ENTRY = NAMESPACE_ARCHIVE + SEPARATOR
                + COMMAND_LIST_ENTRY;
        public final static String ARCHIVE_COMMAND_PACK = NAMESPACE_ARCHIVE + SEPARATOR + COMMAND_PACK;;
        public final static String ARCHIVE_COMMAND_UPLOAD = NAMESPACE_ARCHIVE + SEPARATOR + COMMAND_UPLOAD;

    }




    //COMMANDS












}
