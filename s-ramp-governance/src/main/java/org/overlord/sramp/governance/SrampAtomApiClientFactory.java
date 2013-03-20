/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.governance;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.auth.AuthenticationProvider;

/**
 * Factory used to create instances of the S-RAMP Atom API client.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampAtomApiClientFactory {

    private static Governance governance = new Governance();

    /**
     * Constructor.
     */
    private SrampAtomApiClientFactory() {
    }

    /**
     * Creates a new Atom API client used to make calls to the S-RAMP repository.
     */
    public static SrampAtomApiClient createAtomApiClient() {
        try {
            String endpoint = governance.getSrampUrl().toExternalForm();
            boolean validating = governance.getSrampValidating();
            Class<?> c = governance.getSrampAuthProvider();
            AuthenticationProvider authProvider = null;
            if (c != null)
                authProvider = (AuthenticationProvider) c.newInstance();
            return new SrampAtomApiClient(endpoint, authProvider, validating);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
