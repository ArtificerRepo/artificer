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
package org.artificer.ui.client.local.pages.artifacts;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import org.artificer.ui.client.local.util.IUploadCompletionHandler;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;
import org.overlord.commons.gwt.client.local.widgets.TemplatedFormPanel;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * A modal dialog used to create artifacts in Artificer.
 *
 * @author Brett Meyer
 */
@Templated("/org/artificer/ui/client/local/site/dialogs/create-dialog.html#create-dialog")
@Dependent
public class CreateArtifactDialog extends ModalDialog {

    @Inject @DataField("create-dialog-form")
    private TemplatedFormPanel form;
    @Inject @DataField("create-dialog-type")
    protected TextBox artifactType;
    @Inject @DataField("create-dialog-name")
    protected TextBox artifactName;
    @Inject @DataField("create-dialog-description")
    protected TextArea artifactDescription;
    @Inject @DataField("create-dialog-submit-button")
    private Button submitButton;
    @Inject
    private Instance<CreateArtifactFormSubmitHandler> formHandlerFactory;

    private CreateArtifactFormSubmitHandler formHandler;
    private IUploadCompletionHandler completionHandler;

    /**
     * Constructor.
     */
    public CreateArtifactDialog() {
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

        artifactType.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                enableSubmitButton();
            }
        });
        artifactType.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                enableSubmitButton();
            }
        });
        artifactName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                enableSubmitButton();
            }
        });
        artifactName.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                enableSubmitButton();
            }
        });
    }

    private void enableSubmitButton() {
        boolean enabled = artifactType.getValue() != null && artifactType.getValue().trim().length() > 0
                && artifactName.getValue() != null && artifactName.getValue().trim().length() > 0;
        submitButton.setEnabled(enabled);
    }

    /**
     * @see org.overlord.commons.gwt.client.local.widgets.ModalDialog#show()
     */
    @Override
    public void show() {
        form.setAction(GWT.getModuleBaseURL() + "services/artifactCreate");
        submitButton.setEnabled(false);
        super.show();
    }

    /**
     * Called when the user clicks the 'submit' (Create) button.
     * @param event
     */
    @EventHandler("create-dialog-submit-button")
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

}
