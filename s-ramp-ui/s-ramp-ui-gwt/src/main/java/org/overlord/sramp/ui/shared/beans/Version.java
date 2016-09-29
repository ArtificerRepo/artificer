/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.ui.shared.beans;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * The current version of the application.
 *
 * @author eric.wittmann@redhat.com
 */
public class Version implements Serializable {

	private static final long serialVersionUID = -3356069827138368989L;

	/**
	 * Gets the current version information for s-ramp-ui.
	 */
	public static final Version getCurrentVersion() {
		Version version = new Version();
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = Version.class.getResourceAsStream("/META-INF/config/" + Version.class.getName() + ".properties");
			Properties props = new Properties();
			props.load(resourceAsStream);
			String srampuiVersion = props.getProperty("srampui.version");
			Date buildDate = parseBuildDate(props.getProperty("build.date"));
			version.setVersion(srampuiVersion);
			version.setDate(buildDate);
		} catch (IOException e) {
			// Should never happen.
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
		}
		return version;
	}

	/**
     * Parses the build date string into a {@link Date}.
     *
     * @param dateString
     */
    private static Date parseBuildDate(String dateString) {
    	if (dateString.equals("${maven.build.timestamp}"))
    		return new Date();
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	try {
	        return format.parse(dateString);
        } catch (ParseException e) {
        }
    	format = new SimpleDateFormat("yyyyMMdd-HHmm");
    	try {
    		return format.parse(dateString);
    	} catch (ParseException e) {
        }
    	// Bail out - we couldn't parse the stupid date in the version file.
    	return new Date();
    }

	private String version;
	private Date date;

	/**
	 * Public constructor (needed for serialization).
	 */
	public Version() {
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

}
