package org.overlord.sramp.karaf.commands;

/**
 * Constants class used to store the common properties used in the s-ramp karaf
 * commands.
 *
 * @author David Virgil Naranjo
 */
public interface ConfigureConstants {
    public static final String SRAMP_PROPERTIES_FILE_NAME = "sramp.properties"; //$NON-NLS-1$

    public static final String SRAMP_EVENTS_JMS_USER = "sramp.config.events.jms.user";
    public static final String SRAMP_EVENTS_JMS_PASSWORD = "sramp.config.events.jms.password";

    public static final String SRAMP_EVENTS_JMS_DEFAULT_USER = "srampjms";

}
