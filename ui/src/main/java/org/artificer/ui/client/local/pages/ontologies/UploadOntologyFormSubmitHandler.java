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
package org.artificer.ui.client.local.pages.ontologies;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.local.services.NotificationService;
import org.artificer.ui.client.local.util.IUploadCompletionHandler;
import org.artificer.ui.client.local.util.UploadResult;
import org.artificer.ui.client.shared.beans.NotificationBean;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;

import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

/**
 * The form submit handler used by the {@link UploadOntologyDialog}.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class UploadOntologyFormSubmitHandler implements SubmitHandler, SubmitCompleteHandler {

    @Inject
    protected ClientMessages i18n;
    @Inject
    private NotificationService notificationService;

    private ModalDialog dialog;
    private NotificationBean notification;
    private IUploadCompletionHandler completionHandler;

    private UploadResult uploadResult;
    /**
     * Constructor.
     */
    public UploadOntologyFormSubmitHandler() {
    }

    /**
     * @param dialog
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
        uploadResult = null;
        notification = notificationService.startProgressNotification(
                i18n.format("upload-ontology-submit.uploading.title"), //$NON-NLS-1$
                i18n.format("upload-ontology-submit.uploading.msg")); //$NON-NLS-1$
    }


    /**
     * @see com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler#onSubmitComplete(com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent)
     */
    @Override
    public void onSubmitComplete(SubmitCompleteEvent event) {
        dialog.destroy();

        UploadResult results = UploadResult.fromResult(event.getResults());
        uploadResult = results;
        if (results.isError()) {
            if (results.getError() != null) {
                notificationService.completeProgressNotification(
                        notification.getUuid(),
                        i18n.format("upload-ontology-submit.upload-error.title"), //$NON-NLS-1$
                        results.getError());
            } else {
                notificationService.completeProgressNotification(
                        notification.getUuid(),
                        i18n.format("upload-ontology-submit.upload-error.title"), //$NON-NLS-1$
                        i18n.format("upload-ontology-submit.upload-error.msg")); //$NON-NLS-1$
            }
        } else {
            notificationService.completeProgressNotification(
                    notification.getUuid(),
                    i18n.format("upload-ontology-submit.upload-complete.title"), //$NON-NLS-1$
                    i18n.format("upload-ontology-submit.upload-complete.msg")); //$NON-NLS-1$
            if (completionHandler != null) {
                completionHandler.onImportComplete();
            }
        }
    }

    /**
     * @return the completionHandler
     */
    public IUploadCompletionHandler getCompletionHandler() {
        return completionHandler;
    }

    /**
     * @param completionHandler the completionHandler to set
     */
    public void setCompletionHandler(IUploadCompletionHandler completionHandler) {
        this.completionHandler = completionHandler;
    }

    public UploadResult getUploadResult() {
        return uploadResult;
    }

    public void setUploadResult(UploadResult uploadResult) {
        this.uploadResult = uploadResult;
    }

}
