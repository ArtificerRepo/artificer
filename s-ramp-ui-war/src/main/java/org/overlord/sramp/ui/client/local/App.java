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
package org.overlord.sramp.ui.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusLifecycleAdapter;
import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.TransportError;
import org.jboss.errai.bus.client.api.TransportErrorHandler;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.widgets.common.LoggedOutDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main entry point into the S-RAMP browser UI app.
 *
 * @author eric.wittmann@redhat.com
 */
@EntryPoint
@Bundle("messages.json")
public class App {

    // Used by com.google.gwt.logging.Logging
    private static final Logger logger = Logger.getLogger(App.class.getName());

    @Inject
    private RootPanel rootPanel;
    @Inject
    private Navigation navigation;
    @Inject
    private ClientMessageBus bus;
    @Inject
    LoggedOutDialog loggedOutDialog;
    @Inject
    NotificationService notificationService;

    @PostConstruct
    public void buildUI() {
        rootPanel.add(navigation.getContentPanel());
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                GWT.log("Uncaught GWT Error!", e); //$NON-NLS-1$
                notificationService.sendErrorNotification("Uncaught GWT Error!", e); //$NON-NLS-1$
                logger.log(Level.SEVERE, "Uncaught GWT Error!", e);
            }
        });
        bus.addLifecycleListener(new BusLifecycleAdapter() {
            @Override
            public void busAssociating(BusLifecycleEvent e) {
                GWT.log("Bus is associating"); //$NON-NLS-1$
            }
            @Override
            public void busOnline(BusLifecycleEvent e) {
                GWT.log("Bus is now online"); //$NON-NLS-1$
                if (loggedOutDialog.isAttached()) {
                    Window.Location.reload();
                }
            }
            @Override
            public void busDisassociating(BusLifecycleEvent e) {
                GWT.log("Bus is disassociating"); //$NON-NLS-1$
            }
            @Override
            public void busOffline(BusLifecycleEvent e) {
                GWT.log("Bus is now offline"); //$NON-NLS-1$
            }
        });
        bus.addTransportErrorHandler(new TransportErrorHandler() {
            @Override
            public void onError(TransportError error) {
                GWT.log("Transport error: " + error.getStatusCode()); //$NON-NLS-1$
                if (error != null && error.getStatusCode() == 401) {
                    if (!loggedOutDialog.isAttached()) {
                        loggedOutDialog.show();
                    }
                }
            }
        });
    }

}
