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

package org.artificer.devsvr;

import org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter;
import org.overlord.commons.auth.filters.SimplePrincipal;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class BasicAuthFilter extends SamlBearerTokenAuthFilter {

    @Override
    protected SimplePrincipal doBasicLogin(String username, String password, HttpServletRequest request)
    throws IOException {
        if (!username.equals(password)) {
            return null;
        }
        SimplePrincipal principal = new SimplePrincipal(username);
        principal.addRole("overlorduser");
        principal.addRole("admin.sramp");
        return principal;
    }
}
