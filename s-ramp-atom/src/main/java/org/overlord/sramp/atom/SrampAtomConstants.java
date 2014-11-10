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

import java.net.URI;

public class SrampAtomConstants {

    public static final URI X_S_RAMP_MODEL_URN = URIHelper.create("urn:x-s-ramp:2013:model"); //$NON-NLS-1$
    public static final String X_S_RAMP_QUERY_RESULTS = "urn:x-s-ramp:2013:query:results"; //$NON-NLS-1$
    public static final URI X_S_RAMP_QUERY_RESULTS_URN = URIHelper.create(X_S_RAMP_QUERY_RESULTS);
    public static final String X_S_RAMP_TYPE = "urn:x-s-ramp:2013:type"; //$NON-NLS-1$
    public static final URI X_S_RAMP_TYPE_URN = URIHelper.create(X_S_RAMP_TYPE);
    public static final String X_S_RAMP_KIND = "urn:x-s-ramp:2013:kind"; //$NON-NLS-1$
    public static final URI X_S_RAMP_KIND_URN = URIHelper.create(X_S_RAMP_KIND);

    private static class URIHelper {
        static URI create(String uriStr) {
            try {
                return new URI(uriStr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
