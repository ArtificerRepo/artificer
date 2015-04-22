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
package org.artificer.repository.test.jcr;

import org.modeshape.jcr.security.SecurityContext;

/**
 * A modeshape security context used for testing.
 *
 * @author eric.wittmann@redhat.com
 */
public class MockSecurityContext implements SecurityContext {

    public static String currentUser = "junituser";

    /**
     * Constructor.
     */
    public MockSecurityContext() {
    }

    /**
     * @see org.modeshape.jcr.security.SecurityContext#isAnonymous()
     */
    @Override
    public boolean isAnonymous() {
        return false;
    }

    /**
     * @see org.modeshape.jcr.security.SecurityContext#getUserName()
     */
    @Override
    public String getUserName() {
        return currentUser;
    }

    /**
     * @see org.modeshape.jcr.security.SecurityContext#hasRole(String)
     */
    @Override
    public boolean hasRole(String roleName) {
        return true;
    }

    /**
     * @see org.modeshape.jcr.security.SecurityContext#logout()
     */
    @Override
    public void logout() {
    }
}
