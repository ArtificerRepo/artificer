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
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.overlord.sramp.ui.client.shared.beans.ArtifactFilterBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactSummaryBean;
import org.overlord.sramp.ui.client.shared.services.IArtifactSearchService;

/**
 * Client-side service for making RPC calls to the remote search service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ArtifactSearchRpcService {

    @Inject
    private Caller<IArtifactSearchService> remoteSearchService;

    /**
     * Constructor.
     */
    public ArtifactSearchRpcService() {
    }

    /**
     * Performs the search using the remote service.  Hides the RPC details from
     * the caller.
     * @param filters
     * @param searchText
     * @param handler
     */
    public void search(ArtifactFilterBean filters, String searchText,
            final IRpcServiceInvocationHandler<List<ArtifactSummaryBean>> handler) {
        RemoteCallback<List<ArtifactSummaryBean>> successCallback = new DelegatingRemoteCallback<List<ArtifactSummaryBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        remoteSearchService.call(successCallback, errorCallback).search(filters, searchText);
    }

}
