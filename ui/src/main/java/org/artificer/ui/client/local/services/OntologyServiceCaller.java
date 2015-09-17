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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.artificer.ui.client.local.services.callback.DelegatingErrorCallback;
import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;
import org.artificer.ui.client.shared.services.IOntologyService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.artificer.ui.client.local.services.callback.DelegatingRemoteCallback;
import org.artificer.ui.client.shared.beans.OntologyBean;
import org.artificer.ui.client.shared.beans.OntologyResultSetBean;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;

/**
 * Client-side service for making Caller calls to the remote ontology service.  This service
 * also caches the ontologies with the expectation that they rarely change.  Consumers
 * should call the clearCache() method to force a re-fetch of data.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class OntologyServiceCaller {

    @Inject
    private Caller<IOntologyService> remoteOntologyService;

    private OntologyResultSetBean summaryCache = null;
    private Map<String, OntologyBean> ontologyCache = new HashMap<String, OntologyBean>();

    /**
     * Constructor.
     */
    public OntologyServiceCaller() {
    }

    /**
     * Invalidates/clears the cache.
     */
    public void clearCache() {
        summaryCache = null;
        ontologyCache.clear();
    }

    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#list()
     */
    public void list(boolean forceRefresh, final IServiceInvocationHandler<OntologyResultSetBean> handler) {
        if (summaryCache != null && !forceRefresh) {
            Scheduler.get().scheduleDeferred(new Command() {
                public void execute () {
                    handler.onReturn(summaryCache);
                }
            });
        } else {
            RemoteCallback<OntologyResultSetBean> successCallback = new DelegatingRemoteCallback<OntologyResultSetBean>(handler) {
                @Override
                public void callback(OntologyResultSetBean response) {
                    summaryCache = response;
                    super.callback(response);
                }
            };
            ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
            try {
                remoteOntologyService.call(successCallback, errorCallback).list();
            } catch (ArtificerUiException e) {
                errorCallback.error(null, e);
            }
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#get(java.lang.String)
     */
    public void get(final String uuid, boolean forceRefresh, final IServiceInvocationHandler<OntologyBean> handler) {
        if (!forceRefresh && ontologyCache.containsKey(uuid)) {
            Scheduler.get().scheduleDeferred(new Command() {
                public void execute () {
                    handler.onReturn(ontologyCache.get(uuid));
                }
            });
        } else {
            RemoteCallback<OntologyBean> successCallback = new DelegatingRemoteCallback<OntologyBean>(handler) {
                @Override
                public void callback(OntologyBean ontology) {
                    ontologyCache.put(ontology.getUuid(), ontology);
                    super.callback(ontology);
                }
            };
            ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
            try {
                remoteOntologyService.call(successCallback, errorCallback).get(uuid);
            } catch (ArtificerUiException e) {
                errorCallback.error(null, e);
            }
        }
    }

    /**
     * Gets all of the ontologies in a single async shot.
     * @param handler
     */
    public void getAll(final boolean forceRefresh, final IServiceInvocationHandler<List<OntologyBean>> handler) {
        list(forceRefresh, new IServiceInvocationHandler<OntologyResultSetBean>() {
            int ontologyIndex = 0;
            List<OntologyBean> allOntologies = new ArrayList<OntologyBean>();
            @Override
            public void onReturn(final OntologyResultSetBean summaries) {
                if (summaries.getOntologies().isEmpty()) {
                    handler.onReturn(allOntologies);
                } else {
                    final IServiceInvocationHandler<OntologyBean> handy = new IServiceInvocationHandler<OntologyBean>() {
                        @Override
                        public void onReturn(OntologyBean ontology) {
                            // add the ontology to the rval
                            allOntologies.add(ontology);
                            // get the next ontology in the list (unless we've got them all)
                            ontologyIndex++;
                            if (ontologyIndex >= summaries.getOntologies().size()) {
                                handler.onReturn(allOntologies);
                            } else {
                                get(summaries.getOntologies().get(ontologyIndex).getUuid(), forceRefresh, this);
                            }
                        }
                        @Override
                        public void onError(Throwable error) {
                            handler.onError(error);
                        }
                    };
                    get(summaries.getOntologies().get(ontologyIndex).getUuid(), forceRefresh, handy);
                }
            }
            @Override
            public void onError(Throwable error) {
                handler.onError(error);
            }
        });
    }

    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#update(OntologyBean)
     */
    public void update(OntologyBean ontology, final IServiceInvocationHandler<Void> handler) {
        ontologyCache.remove(ontology.getUuid());
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteOntologyService.call(successCallback, errorCallback).update(ontology);
        } catch (ArtificerUiException e) {
            errorCallback.error(null, e);
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#add(OntologyBean)
     */
    public void add(OntologyBean ontology, final IServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteOntologyService.call(successCallback, errorCallback).add(ontology);
        } catch (ArtificerUiException e) {
            errorCallback.error(null, e);
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IOntologyService#delete(OntologyBean)
     */
    public void delete(String uuid, final IServiceInvocationHandler<Void> handler) {
        ontologyCache.remove(uuid);
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
            remoteOntologyService.call(successCallback, errorCallback).delete(uuid);
        } catch (ArtificerUiException e) {
            errorCallback.error(null, e);
        }
    }

}
