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
package org.overlord.sramp.ui.client.local.services;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.overlord.sramp.ui.client.shared.beans.ArtifactFilterBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactSummaryBean;
import org.overlord.sramp.ui.client.shared.services.IArtifactSearchService;

import com.google.gwt.user.client.Window;

/**
 * Client-side service for making RPC calls to the remote search service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ArtifactSearchClientService {

    @Inject
    private Caller<IArtifactSearchService> remoteSearchService;

    /**
     * Constructor.
     */
    public ArtifactSearchClientService() {
    }

    /**
     * Performs the search using the remote service.  Hides the RPC details from
     * the caller.
     * @param filters
     * @param searchText
     */
    public void search(ArtifactFilterBean filters, String searchText) {
        remoteSearchService.call(new RemoteCallback<List<ArtifactSummaryBean>>() {
            @Override
            public void callback(List<ArtifactSummaryBean> response) {
                Window.alert("Response from server: " + response.size() + " artifacts matched");
            }
        }).search(filters, searchText);
    }

}
