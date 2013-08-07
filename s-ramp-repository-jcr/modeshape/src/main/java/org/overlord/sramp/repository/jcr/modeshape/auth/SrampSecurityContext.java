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
package org.overlord.sramp.repository.jcr.modeshape.auth;

import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.security.AuthorizationProvider;
import org.modeshape.jcr.security.SecurityContext;
import org.modeshape.jcr.value.Path;

/**
 * A modeshape security context used in s-ramp.
 * @author eric.wittmann@redhat.com
 */
public class SrampSecurityContext implements SecurityContext, AuthorizationProvider {

    private final SecurityContext delegate;

    /**
     * Constructor.
     * @param delegate
     */
    public SrampSecurityContext(SecurityContext delegate) {
        this.delegate = delegate;
    }

    /**
     * @see org.modeshape.jcr.security.AuthorizationProvider#hasPermission(org.modeshape.jcr.ExecutionContext, java.lang.String, java.lang.String, java.lang.String, org.modeshape.jcr.value.Path, java.lang.String[])
     */
    @Override
    public boolean hasPermission(ExecutionContext context, String repositoryName,
            String repositorySourceName, String workspaceName, Path path, String... actions) {
        // Overlord users can do anything.
        return hasRole("overlorduser"); //$NON-NLS-1$
    }

    /**
     * @see org.modeshape.jcr.security.SecurityContext#isAnonymous()
     */
    @Override
    public boolean isAnonymous() {
        return delegate.isAnonymous();
    }

    /**
     * @see org.modeshape.jcr.security.SecurityContext#getUserName()
     */
    @Override
    public String getUserName() {
        return delegate.getUserName();
    }

    /**
     * @see org.modeshape.jcr.security.SecurityContext#hasRole(java.lang.String)
     */
    @Override
    public boolean hasRole(String roleName) {
        return delegate.hasRole(roleName);
    }

    /**
     * @see org.modeshape.jcr.security.SecurityContext#logout()
     */
    @Override
    public void logout() {
        delegate.logout();
    }

}
