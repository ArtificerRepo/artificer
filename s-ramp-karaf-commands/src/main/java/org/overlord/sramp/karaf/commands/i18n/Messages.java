package org.overlord.sramp.karaf.commands.i18n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * Messages for the karaf commands project.
 * @author David Virgil Naranjo
 */
public class Messages {
    private static final String BUNDLE_NAME = "org.overlord.sramp.karaf.commands.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Look up a message in the i18n resource message bundle by key, then format the
     * message with the given params and return the result.
     * @param key
     * @param params
     * @return the translated and formatted string
     */
    public static String format(String key, Object ... params) {
        String msg = getString(key);
        try {
            return MessageFormat.format(msg, params);
        } catch (Exception e) {
            return '!' + key + '!';
        }
    }

}
