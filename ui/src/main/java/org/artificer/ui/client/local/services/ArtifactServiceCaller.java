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
package org.artificer.ui.client.local.services;

import org.artificer.ui.client.local.services.callback.DelegatingErrorCallback;
import org.artificer.ui.client.local.services.callback.DelegatingRemoteCallback;
import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;
import org.artificer.ui.client.shared.beans.ArtifactBean;
import org.artificer.ui.client.shared.beans.ArtifactCommentBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsIndexBean;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;
import org.artificer.ui.client.shared.services.IArtifactService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Client-side service for making Caller calls to the remote artifact service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ArtifactServiceCaller {

    @Inject
    private Caller<IArtifactService> remoteArtifactService;

    /**
     * Constructor.
     */
    public ArtifactServiceCaller() {
    }

    /**
     * @see org.artificer.ui.client.shared.services.IArtifactService#get(java.lang.String)
     */
    public void get(String uuid, final IServiceInvocationHandler<ArtifactBean> handler) {
        RemoteCallback<ArtifactBean> successCallback = new DelegatingRemoteCallback<ArtifactBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteArtifactService.call(successCallback, errorCallback).get(uuid);
        } catch (ArtificerUiException e) {
            errorCallback.error(null, e);
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IArtifactService#getDocumentContent(String, String)
     */
    public void getDocumentContent(String uuid, String artifactType,
            final IServiceInvocationHandler<String> handler) {
        RemoteCallback<String> successCallback = new DelegatingRemoteCallback<String>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteArtifactService.call(successCallback, errorCallback).getDocumentContent(uuid, artifactType);
        } catch (ArtificerUiException e) {
            errorCallback.error(null, e);
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IArtifactService#getRelationships(String, String)
     */
    public void getRelationships(String uuid, String artifactType,
            IServiceInvocationHandler<ArtifactRelationshipsIndexBean> handler) {
        RemoteCallback<ArtifactRelationshipsIndexBean> successCallback = new DelegatingRemoteCallback<ArtifactRelationshipsIndexBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteArtifactService.call(successCallback, errorCallback).getRelationships(uuid, artifactType);
        } catch (ArtificerUiException e) {
            errorCallback.error(null, e);
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IArtifactService#update(org.artificer.ui.client.shared.beans.ArtifactBean)
     */
    public void update(ArtifactBean artifact, final IServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteArtifactService.call(successCallback, errorCallback).update(artifact);
        } catch (ArtificerUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void addComment(String uuid, String artifactType, String text,
            final IServiceInvocationHandler<ArtifactCommentBean> handler) {
        RemoteCallback<ArtifactCommentBean> successCallback = new DelegatingRemoteCallback<ArtifactCommentBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteArtifactService.call(successCallback, errorCallback).addComment(uuid, artifactType, text);
        } catch (ArtificerUiException e) {
            errorCallback.error(null, e);
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IArtifactService#delete(org.artificer.ui.client.shared.beans.ArtifactBean)
     */
    public void delete(ArtifactBean artifact, final IServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteArtifactService.call(successCallback, errorCallback).delete(artifact);
        } catch (ArtificerUiException e) {
            errorCallback.error(null, e);
        }
    }

}
