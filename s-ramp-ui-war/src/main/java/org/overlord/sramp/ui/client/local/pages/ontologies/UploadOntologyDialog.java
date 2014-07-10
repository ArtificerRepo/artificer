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
package org.overlord.sramp.ui.client.local.pages.ontologies;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;
import org.overlord.commons.gwt.client.local.widgets.TemplatedFormPanel;
import org.overlord.sramp.ui.client.local.util.IUploadCompletionHandler;
import org.overlord.sramp.ui.client.local.util.UploadResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;

/**
 * A modal dialog used to upload an ontology to S-RAMP.
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/dialogs/upload-ontology-dialog.html#upload-ontology-dialog")
@Dependent
public class UploadOntologyDialog extends ModalDialog {

    @Inject @DataField("upload-ontology-dialog-form")
    private TemplatedFormPanel form;
    @Inject @DataField("upload-ontology-dialog-submit-button")
    private Button submitButton;
    @Inject
    private Instance<UploadOntologyFormSubmitHandler> formHandlerFactory;

    private UploadOntologyFormSubmitHandler formHandler;
    private IUploadCompletionHandler completionHandler;

    /**
     * Constructor.
     */
    public UploadOntologyDialog() {
    }

    /**
     * Post construct.
     */
    @PostConstruct
    protected void onPostConstruct() {
        formHandler = formHandlerFactory.get();
        formHandler.setDialog(this);
        form.addSubmitHandler(formHandler);
        form.addSubmitCompleteHandler(formHandler);
    }

    /**
     * @see org.overlord.commons.gwt.client.local.widgets.ModalDialog#show()
     */
    @Override
    public void show() {
        form.setAction(GWT.getModuleBaseURL() + "services/ontologyUpload"); //$NON-NLS-1$
        super.show();
    }

    /**
     * Called when the user clicks the 'submit' (Import) button.
     * @param event
     */
    @EventHandler("upload-ontology-dialog-submit-button")
    public void onSubmitClick(ClickEvent event) {
        formHandler.setCompletionHandler(this.completionHandler);
        form.submit();
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

    public String getOntologyUploadedUUID() {
        UploadResult result = formHandler.getUploadResult();
        if (result != null) {
            return result.get("uuid"); //$NON-NLS-1$
        }
        return null;
    }

}
