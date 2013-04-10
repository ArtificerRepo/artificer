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

import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.sramp.ui.client.local.pages.ArtifactDetailsPage;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.ModalDialog;
import org.overlord.sramp.ui.client.shared.beans.NotificationBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;
import org.overlord.sramp.ui.server.servlets.ArtifactUploadServlet;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The form submit handler used by the {@link ImportArtifactDialog}.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ImportArtifactFormSubmitHandler implements SubmitHandler, SubmitCompleteHandler {

    @Inject
    private NotificationService notificationService;
    @Inject
    private TransitionAnchorFactory<ArtifactDetailsPage> toDetailsFactory;

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
    }

    /**
     * @see com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler#onSubmitComplete(com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent)
     */
    @Override
    public void onSubmitComplete(SubmitCompleteEvent event) {
        dialog.destroy();

        ImportResult results = ImportResult.fromResult(event.getResults());
        if (results.isError()) {
            // TODO i18n
            notificationService.complete(
                    notification.getUuid(),
                    "Importing Artifact(s) [!Error!]",
                    "Uh oh, something went wrong with your import!  Please contact your system administrator.");
        } else {
            // TODO i18n
            Widget ty = new InlineLabel("Thank you for waiting - your import has completed successfully.  ");
            TransitionAnchor<ArtifactDetailsPage> clickHere = toDetailsFactory.get("uuid", results.getUuid());
            clickHere.setText("Click here");
            Widget postAmble = new InlineLabel(" to view the details of the imported artifact.");
            FlowPanel body = new FlowPanel();
            body.add(ty);
            body.add(clickHere);
            body.add(postAmble);
            notificationService.complete(
                    notification.getUuid(),
                    "Importing Artifact(s) [Complete]",
                    body);
        }
    }

    /**
     * The {@link ArtifactUploadServlet} returns a JSON map as the response.
     * @author eric.wittmann@redhat.com
     */
    private static class ImportResult extends JavaScriptObject {

        /**
         * Constructor.
         */
        protected ImportResult() {
        }

        /**
         * Convert the string returned by the {@link ArtifactUploadServlet} into JSON and
         * then from there into an {@link ImportResult} bean.
         * @param resultData
         */
        public static final ImportResult fromResult(String resultData) {
            int startIdx = resultData.indexOf('(');
            int endIdx = resultData.lastIndexOf(')') + 1;
            if (resultData.endsWith(")"))
                resultData = resultData.substring(startIdx);
            else
                resultData = resultData.substring(startIdx, endIdx);
            return fromJSON(resultData);
        }

        /**
         * Gets a value from the map.
         * @param key
         */
        public final native String get(String key) /*-{
            if (this[key])
                return this[key];
            else
                return null;
        }-*/;

        /**
         * @return the uuid
         */
        public final String getUuid() {
            return get("uuid");
        }

        /**
         * @return the model
         */
        public final String getModel() {
            return get("model");
        }

        /**
         * @return the type
         */
        public final String getType() {
            return get("type");
        }

        /**
         * Returns true if the response is an error response.
         */
        public final boolean isError() {
            return "true".equals(get("exception"));
        }

        /**
         * Gets the error.
         */
        public final SrampUiException getError() {
            String errorMessage = get("exception-message");
//            String errorStack = get("exception-stack");
            SrampUiException error = new SrampUiException(errorMessage);
//            error.setRootStackTrace(errorStack);
            return error;
        }

        /**
         * Convert a string of json data into a useful bean.
         * @param jsonData
         */
        public static final native ImportResult fromJSON(String jsonData) /*-{ return eval(jsonData); }-*/;

    }
}
