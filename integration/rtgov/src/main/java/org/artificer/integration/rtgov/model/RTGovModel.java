/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.integration.rtgov.model;

import java.util.HashMap;
import java.util.Map;

import org.artificer.common.ArtifactType;

/**
 * Information about the RTGov model implemented in S-RAMP by the RTGov classifiers and expanders.
 * 
 * @author Brett Meyer
 */
public class RTGovModel {
    public static final String TYPE_RTGOV_ACS = "RTGovACS"; //$NON-NLS-1$
    public static final String TYPE_RTGOV_AV = "RTGovAV"; //$NON-NLS-1$
    public static final String TYPE_RTGOV_EPN = "RTGovEPN"; //$NON-NLS-1$
    public static final String TYPE_RTGOV_IP = "RTGovIP"; //$NON-NLS-1$
    public static final String TYPE_RTGOV_ACS_TEMPLATE = "RTGovACSTemplate"; //$NON-NLS-1$
    public static final String TYPE_RTGOV_AV_TEMPLATE = "RTGovAVTemplate"; //$NON-NLS-1$
    public static final String TYPE_RTGOV_EPN_TEMPLATE = "RTGovEPNTemplate"; //$NON-NLS-1$
    public static final String TYPE_RTGOV_IP_TEMPLATE = "RTGovIPTemplate"; //$NON-NLS-1$

    public static final String HINT_RTGOV_ACS = "acs.json"; //$NON-NLS-1$
    public static final String HINT_RTGOV_AV = "av.json"; //$NON-NLS-1$
    public static final String HINT_RTGOV_EPN = "epn.json"; //$NON-NLS-1$
    public static final String HINT_RTGOV_IP = "ip.json"; //$NON-NLS-1$
    public static final String HINT_RTGOV_ACS_TEMPLATE = "acs-template.json"; //$NON-NLS-1$
    public static final String HINT_RTGOV_AV_TEMPLATE = "av-template.json"; //$NON-NLS-1$
    public static final String HINT_RTGOV_EPN_TEMPLATE = "epn-template.json"; //$NON-NLS-1$
    public static final String HINT_RTGOV_IP_TEMPLATE = "ip-template.json"; //$NON-NLS-1$
    
    public static final Map<String, String> HINTS = new HashMap<String, String>();
    
    static {
        HINTS.put(HINT_RTGOV_ACS, TYPE_RTGOV_ACS);
        HINTS.put(HINT_RTGOV_AV, TYPE_RTGOV_AV);
        HINTS.put(HINT_RTGOV_EPN, TYPE_RTGOV_EPN);
        HINTS.put(HINT_RTGOV_IP, TYPE_RTGOV_IP);
        HINTS.put(HINT_RTGOV_ACS_TEMPLATE, TYPE_RTGOV_ACS_TEMPLATE);
        HINTS.put(HINT_RTGOV_AV_TEMPLATE, TYPE_RTGOV_AV_TEMPLATE);
        HINTS.put(HINT_RTGOV_EPN_TEMPLATE, TYPE_RTGOV_EPN_TEMPLATE);
        HINTS.put(HINT_RTGOV_IP_TEMPLATE, TYPE_RTGOV_IP_TEMPLATE);
    }
    
    public static ArtifactType detect(String filename) {
        String type = HINTS.get(filename);
        if (type != null) {
            return ArtifactType.valueOf(filename, true);
        }
        return null;
    }
}
