package org.overlord.sramp.karaf.commands;

/**
 * Constants class used to store the common properties used in the s-ramp karaf
 * commands.
 *
 * @author David Virgil Naranjo
 */
public interface ConfigureConstants {
    public static final String SRAMP_PROPERTIES_FILE_NAME = "sramp.properties"; //$NON-NLS-1$

    public static final String SRAMP_EVENTS_JMS_USER = "sramp.config.events.jms.user"; //$NON-NLS-1$
    public static final String SRAMP_EVENTS_JMS_PASSWORD = "sramp.config.events.jms.password"; //$NON-NLS-1$

    public static final String SRAMP_EVENTS_JMS_DEFAULT_USER = "srampjms"; //$NON-NLS-1$

    // FABRIC CONSTANTS

    // Ui headers:

    public static final String SRAMP_HEADER_HREF = "overlord.headerui.apps.s-ramp-ui.href";
    public static final String SRAMP_HEADER_HREF_VALUE = "/s-ramp-ui/";
    public static final String SRAMP_HEADER_LABEL = "overlord.headerui.apps.s-ramp-ui.label";
    public static final String SRAMP_HEADER_LABEL_VALUE = "Repository";
    public static final String SRAMP_HEADER_PRIMARY_BRAND = "overlord.headerui.apps.s-ramp-ui.primary-brand";
    public static final String SRAMP_HEADER_PRIMARY_BRAND_VALUE = "JBoss Overlord";
    public static final String SRAMP_HEADER_SECOND_BRAND = "overlord.headerui.apps.s-ramp-ui.secondary-brand";
    public static final String SRAMP_HEADER_SECOND_BRAND_VALUE = "S-RAMP Repository";

}

