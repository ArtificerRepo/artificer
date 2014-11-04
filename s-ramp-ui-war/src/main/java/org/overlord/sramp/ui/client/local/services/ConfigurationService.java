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
package org.overlord.sramp.ui.client.local.services;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.InitBallot;

import com.google.gwt.core.client.GWT;

/**
 * @author Brett Meyer
 */
@ApplicationScoped
public class ConfigurationService {
    
    @Inject
    InitBallot<ConfigurationService> ballot;
    
    @PostConstruct
    private void postConstruct() {
        RestClient.setJacksonMarshallingActive(true);
        // getModuleBaseURL returns something like http://[host]:[port]/s-ramp-ui/app.  Unless we want the REST
        // endpoints also on /app, strip it.
        String baseUrl = GWT.getModuleBaseURL();
        baseUrl = baseUrl.replace("/app", "");
        baseUrl += "rest/";
        RestClient.setApplicationRoot(baseUrl);

        ballot.voteForInit();
    }
}
