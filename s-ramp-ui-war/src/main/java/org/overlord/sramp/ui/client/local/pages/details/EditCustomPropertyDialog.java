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
package org.overlord.sramp.ui.client.local.pages.details;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Dialog that allows the user to edit a property value.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/dialogs/edit-property-dialog.html#edit-property-dialog")
@Dependent
public class EditCustomPropertyDialog extends ModalDialog implements HasValueChangeHandlers<String> {

    @DataField
    protected HeadingElement title = Document.get().createHElement(4);
    @DataField
    protected LabelElement label = Document.get().createLabelElement();

    @Inject @DataField
    protected TextArea value;
    @Inject @DataField("edit-property-submit-button")
    protected Button submitButton;
    protected String originalValue;

    /**
     * Constructor.
     */
    public EditCustomPropertyDialog() {
    }

    /**
     * @param label
     */
    public void setLabel(String label) {
        this.label.setInnerText(label + ":"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        this.title.setInnerText(title);
    }

    /**
     * Sets the initial value.
     * @param value
     */
    public void setValue(String value) {
        this.originalValue = value;
        this.value.setValue(value);
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        submitButton.setEnabled(false);
        value.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String val = value.getValue();
                boolean shouldEnable = val != null &&
                        val.trim().length() > 0 &&
                        !val.equals(originalValue);
                submitButton.setEnabled(shouldEnable);
            }
        });
        value.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String val = event.getValue();
                boolean shouldEnable = val != null &&
                        val.trim().length() > 0 &&
                        !val.equals(originalValue);
                submitButton.setEnabled(shouldEnable);
            }
        });
    }

    /**
     * @see org.overlord.commons.gwt.client.local.widgets.ModalDialog#show()
     */
    @Override
    public void show() {
        super.show();
        value.setFocus(true);
        value.selectAll();
    }

    /**
     * Called when the user clicks the submit button.
     * @param event
     */
    @EventHandler("edit-property-submit-button")
    protected void onSubmit(ClickEvent event) {
        final String val = value.getValue();
        ValueChangeEvent.fire(this, val);
        hide();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
