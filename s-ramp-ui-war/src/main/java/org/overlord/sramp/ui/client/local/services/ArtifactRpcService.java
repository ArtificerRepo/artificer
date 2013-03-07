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
import org.overlord.sramp.ui.client.shared.beans.ArtifactBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;
import org.overlord.sramp.ui.client.shared.services.IArtifactService;

/**
 * Client-side service for making RPC calls to the remote artifact service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ArtifactRpcService {

    @Inject
    private Caller<IArtifactService> remoteArtifactService;

    /**
     * Constructor.
     */
    public ArtifactRpcService() {
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#get(java.lang.String)
     */
    public void get(String uuid, final IRpcServiceInvocationHandler<ArtifactBean> handler) {
        RemoteCallback<ArtifactBean> successCallback = new DelegatingRemoteCallback<ArtifactBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteArtifactService.call(successCallback, errorCallback).get(uuid);
        } catch (SrampUiException e) {
            errorCallback.error(null, e);
        }
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#getDocumentContent(String, String)
     */
    public void getDocumentContent(String uuid, String artifactType,
            final IRpcServiceInvocationHandler<String> handler) {
        RemoteCallback<String> successCallback = new DelegatingRemoteCallback<String>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteArtifactService.call(successCallback, errorCallback).getDocumentContent(uuid, artifactType);
        } catch (SrampUiException e) {
            errorCallback.error(null, e);
        }
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#update(org.overlord.sramp.ui.client.shared.beans.ArtifactBean)
     */
    public void update(ArtifactBean artifact, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteArtifactService.call(successCallback, errorCallback).update(artifact);
        } catch (SrampUiException e) {
            errorCallback.error(null, e);
        }
    }

}
