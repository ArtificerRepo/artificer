/*
 * Copyright 2012 JBoss Inc
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

/**
 * Some Governance constants.
 *
 */
public interface GovernanceConstants {

    //Configuration constants
    public static final String GOVERNANCE_FILE_NAME     = "governance.file.name";
    public static final String GOVERNANCE_FILE_REFRESH  = "governance.file.refresh";
    public static final String SRAMP_REPO_URL           = "sramp.repo.url";
    public static final String SRAMP_REPO_AUTH_PROVIDER = "sramp.repo.auth.provider";
    public static final String SRAMP_REPO_USER          = "sramp.repo.user";
    public static final String SRAMP_REPO_PASSWORD      = "sramp.repo.password";
    public static final String SRAMP_REPO_VALIDATING    = "sramp.repo.validating";
    public static final String SRAMP_REPO_SAML_ISSUER   = "sramp.repo.auth.saml.issuer";
    public static final String SRAMP_REPO_SAML_SERVICE  = "sramp.repo.auth.saml.service";
    public static final String GOVERNANCE_URL           = "governance.url";
    public static final String GOVERNANCE_QUERY_INTERVAL= "governance.query.interval";
    public static final String GOVERNANCE_ACCEPTABLE_LAG= "governance.acceptable.lagtime";
    public static final String GOVERNANCE_JNDI_EMAIL_REF= "governance.jndi.email.reference";
    public static final String GOVERNANCE_EMAIL_DOMAIN  = "governance.email.domain";
    public static final String GOVERNANCE_EMAIL_FROM    = "governance.email.from";

    //JBPM connection info
    public static final String GOVERNANCE_JBPM_USER     = "governance.jbpm.user";
    public static final String GOVERNANCE_JBPM_PASSWORD = "governance.jbpm.password";
    public static final String GOVERNANCE_JBPM_URL      = "governance.jbpm.url";

    //governance resource configuration
    public static final String GOVERNANCE_TARGETS       = "governance.targets";
    public static final String GOVERNANCE_QUERIES       = "governance.queries";
    public static final String GOVERNANCE               = "governance.";
}
