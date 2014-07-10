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
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;
import org.overlord.sramp.ui.client.shared.beans.OntologyBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Dialog that allows the user to create a new, empty, ontology.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/dialogs/add-ontology-dialog.html#add-ontology-dialog")
@Dependent
public class AddOntologyDialog extends ModalDialog implements HasValueChangeHandlers<OntologyBean> {

    @Inject @DataField
    protected TextBox namespace;
    @Inject @DataField
    protected TextBox id;
    @Inject @DataField
    protected TextBox label;
    @Inject @DataField
    protected TextArea comment;
    @Inject @DataField("add-ontology-submit-button")
    protected Button submitButton;
    
    /**
     * Constructor.
     */
    public AddOntologyDialog() {
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        submitButton.setEnabled(false);
        KeyUpHandler validationHandler1 = new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                submitButton.setEnabled(isValid());
            }
        };
        ValueChangeHandler<String> validationHandler2 = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                submitButton.setEnabled(isValid());
            }
        };
        namespace.addKeyUpHandler(validationHandler1);
        namespace.addValueChangeHandler(validationHandler2);
        id.addKeyUpHandler(validationHandler1);
        id.addValueChangeHandler(validationHandler2);
    }
    
    /**
     * Returns true if the values in the form fields are valid.
     */
    protected boolean isValid() {
        String nsVal = namespace.getValue();
        String idVal = id.getValue();
        boolean validity = false;
        if (nsVal != null && idVal != null && nsVal.trim().length() > 0 && idVal.trim().length() > 0) {
            validity = true;
        }
        return validity;
    }

    /**
     * @see org.overlord.commons.gwt.client.local.widgets.ModalDialog#show()
     */
    @Override
    public void show() {
        super.show();
        namespace.setFocus(true);
    }

    /**
     * Called when the user clicks the submit button.
     * @param event
     */
    @EventHandler("add-ontology-submit-button")
    protected void onSubmit(ClickEvent event) {
        OntologyBean ontology = new OntologyBean();
        ontology.setBase(namespace.getValue());
        ontology.setId(id.getValue());
        ontology.setLabel(label.getValue());
        ontology.setComment(comment.getValue());
        ValueChangeEvent.fire(this, ontology);
        hide();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<OntologyBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
