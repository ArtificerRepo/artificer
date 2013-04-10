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
package org.overlord.sramp.ui.client.local.pages.artifacts;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.ModalDialog;
import org.overlord.sramp.ui.client.shared.beans.NotificationBean;

import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

/**
 * The form submit handler used by the {@link ImportArtifactDialog}.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ImportArtifactFormSubmitHandler implements SubmitHandler, SubmitCompleteHandler {

    @Inject
    private NotificationService notificationService;

    private ModalDialog dialog;
    private NotificationBean notification;

    /**
     * Constructor.
     */
    public ImportArtifactFormSubmitHandler() {
    }

    /**
     * @param importArtifactDialog
     */
    public void setDialog(ModalDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * @see com.google.gwt.user.client.ui.FormPanel.SubmitHandler#onSubmit(com.google.gwt.user.client.ui.FormPanel.SubmitEvent)
     */
    @Override
    public void onSubmit(SubmitEvent event) {
        dialog.hide(false);
        // TODO i18n
        notification = notificationService.start(
                "Importing Artifact(s)",
                "Please wait while your import is processed...");
        System.out.println("New UUID: " + notification.getUuid());
    }

    /**
     * @see com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler#onSubmitComplete(com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent)
     */
    @Override
    public void onSubmitComplete(SubmitCompleteEvent event) {
        dialog.destroy();
        // TODO i18n
        System.out.println("Completing: " + notification);
        System.out.println("      UUID: " + notification.getUuid());
        notificationService.complete(
                notification.getUuid(),
                "Importing Artifact(s) [Complete]",
                "Thank you for waiting - your import has completed successfully.");
    }

}
