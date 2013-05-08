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

import java.util.Map;

import javax.jcr.Credentials;
import javax.security.auth.login.LoginException;

import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.security.JaasProvider;

/**
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampAuthProvider extends JaasProvider  {

    /**
     * Constructor.
     *
     * @param policyName
     * @throws LoginException
     */
    public SrampAuthProvider(String policyName) throws LoginException {
        super(policyName);
    }

    /**
     * @see org.modeshape.jcr.security.JaasProvider#authenticate(javax.jcr.Credentials, java.lang.String, java.lang.String, org.modeshape.jcr.ExecutionContext, java.util.Map)
     */
    @Override
    public ExecutionContext authenticate(Credentials credentials, String repositoryName,
            String workspaceName, ExecutionContext repositoryContext, Map<String, Object> sessionAttributes) {
        ExecutionContext ctx = super.authenticate(credentials, repositoryName, workspaceName, repositoryContext, sessionAttributes);
        if (ctx != null) {
            return repositoryContext.with(new SrampSecurityContext(ctx.getSecurityContext()));
        }
        return ctx;
    }

}
