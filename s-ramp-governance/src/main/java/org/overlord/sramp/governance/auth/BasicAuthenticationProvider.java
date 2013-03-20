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
package org.overlord.sramp.governance.auth;

import org.overlord.sramp.governance.Governance;


/**
 * An authentication provider that uses simple BASIC authentication.
 *
 * @author eric.wittmann@redhat.com
 */
public class BasicAuthenticationProvider extends org.overlord.sramp.client.auth.BasicAuthenticationProvider {

    private static Governance governance = new Governance();

    /**
     * Constructor.
     */
    public BasicAuthenticationProvider() {
        super(governance.getSrampUser(), governance.getSrampPassword());
    }

}
