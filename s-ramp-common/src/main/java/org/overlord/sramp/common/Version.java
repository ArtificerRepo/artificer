package org.overlord.sramp.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * Accessor - used to get the current version of the engine.
 * 
 * @author eric.wittmann@redhat.com
 */
public class Version {

    private static final Version instance = new Version();

    public static final Version get() {
        return instance;
    }

    private String versionString;
    private String versionDate;

    /**
     * Constructor.
     */
    private Version() {
        load();
    }

    /**
     * Loads the version info from version.properties.
     */
    private void load() {
        URL url = Version.class.getResource("version.properties"); //$NON-NLS-1$
        if (url == null) {
            this.versionString = "Unknown"; //$NON-NLS-1$
            this.versionDate = new Date().toString();
        } else {
            InputStream is = null;
            Properties props = new Properties();
            try {
                is = url.openStream();
                props.load(is);
                this.versionString = props.getProperty("version", "Unknown"); //$NON-NLS-1$ //$NON-NLS-2$
                this.versionDate = props.getProperty("date", new Date().toString()); //$NON-NLS-1$
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    /**
     * @return the versionString
     */
    public String getVersionString() {
        return versionString;
    }

    /**
     * @return the versionDate
     */
    public String getVersionDate() {
        return versionDate;
    }

}