/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.atom;

import java.util.HashMap;
import java.util.Map;

public class MediaType extends javax.ws.rs.core.MediaType {

    /** "application/zip" */
    public final static String APPLICATION_ZIP = "application/zip";

    /** "message/http" */
    public final static String MESSAGE_HTTP = "message/http";
    public final static MediaType MESSAGE_HTTP_TYPE = new MediaType("message", "http");

    /** "application/atom+xml;type=entry" */
    public final static String APPLICATION_ATOM_XML_ENTRY = "application/atom+xml;type=entry";
    public final static MediaType APPLICATION_ATOM_XML_ENTRY_TYPE = new MediaType("application", "atom+xml", param("type", "entry"));

    /** "application/atom+xml;type=feed" */
    public final static String APPLICATION_ATOM_XML_FEED = "application/atom+xml;type=feed";
    public final static MediaType APPLICATION_ATOM_XML_FEED_TYPE = new MediaType("application", "atom+xml", param("type", "feed"));

    /** "application/atom+xml;charset=utf-8" */
    public final static String APPLICATION_ATOM_XML_UTF8 = "application/atom+xml;charset=utf-8";
    public final static MediaType APPLICATION_ATOM_XML_UTF8_TYPE = new MediaType("application", "atom+xml", param("charset", "utf-8"));

    /** "multipart/mixed" */
    public final static String MULTIPART_MIXED = "multipart/mixed";
    public final static MediaType MULTIPART_MIXED_TYPE = new MediaType("multipart", "mixed");

    /** "application/sramp-atom-exception" */
    public final static String APPLICATION_SRAMP_ATOM_EXCEPTION = "application/sramp-atom-exception";
    public final static MediaType APPLICATION_SRAMP_ATOM_EXCEPTION_TYPE = new MediaType("application", "sramp-atom-exception");

    /**
     * Creates a parameter map.
     *
	 * @param key
	 * @param value
	 * @return parameter map
	 */
	private static Map<String, String> param(String key, String value) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(key, value);
		return paramMap;
	}

    /**
     * Constructor.
     */
    public MediaType() {
        super();
    }

    /**
     * Constructor.
     * @param type
     * @param subtype
     * @param parameters
     */
    public MediaType(String type, String subtype, Map<String, String> parameters) {
        super(type, subtype, parameters);
    }

	/**
	 * Constructor.
	 * @param type
	 * @param subtype
	 */
    public MediaType(String type, String subtype) {
        super(type, subtype);
    }
    
    public static MediaType getInstance(String mimeType) {
        if (mimeType!=null && mimeType.contains("/")) {
            String[] type = mimeType.split("/");
            return new MediaType(type[0],type[1]);
        } else {
            return new MediaType(mimeType, null);
        }
    }

}
