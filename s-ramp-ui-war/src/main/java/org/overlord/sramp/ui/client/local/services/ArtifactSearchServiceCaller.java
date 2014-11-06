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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.overlord.sramp.ui.client.local.services.callback.DelegatingErrorCallback;
import org.overlord.sramp.ui.client.local.services.callback.DelegatingRemoteCallback;
import org.overlord.sramp.ui.client.local.services.callback.IServiceInvocationHandler;
import org.overlord.sramp.ui.client.shared.beans.ArtifactResultSetBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactSearchBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;
import org.overlord.sramp.ui.client.shared.services.IArtifactSearchService;

/**
 * Client-side service for making Caller calls to the remote search service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ArtifactSearchServiceCaller {

    @Inject
    private Caller<IArtifactSearchService> remoteSearchService;

    /**
     * Constructor.
     */
    public ArtifactSearchServiceCaller() {
    }

    /**
     * Performs the search using the remote service.  Hides the details from
     * the caller.
     * 
     * @param searchBean
     */
    public void search(ArtifactSearchBean searchBean, final IServiceInvocationHandler<ArtifactResultSetBean> handler) {
        // TODO only allow one search at a time.  If another search comes in before the previous one
        // finished, cancel the previous one.  In other words, only return the results of the *last*
        // search performed.
        RemoteCallback<ArtifactResultSetBean> successCallback = new DelegatingRemoteCallback<ArtifactResultSetBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteSearchService.call(successCallback, errorCallback).search(searchBean);
        } catch (SrampUiException e) {
            errorCallback.error(null, e);
        }
    }

}
