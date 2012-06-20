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
