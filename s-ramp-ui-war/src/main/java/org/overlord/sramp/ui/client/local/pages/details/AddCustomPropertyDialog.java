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

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;

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
 * Dialog that allows the user to enter a new custom property.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/dialogs/add-property-dialog.html#add-property-dialog")
@Dependent
public class AddCustomPropertyDialog extends ModalDialog implements HasValueChangeHandlers<Map.Entry<String, String>> {

    @Inject @DataField
    protected TextBox name;
    @Inject @DataField
    protected TextArea value;
    @Inject @DataField("add-property-submit-button")
    protected Button submitButton;

    /**
     * Constructor.
     */
    public AddCustomPropertyDialog() {
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        submitButton.setEnabled(false);
        name.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String val = name.getValue();
                boolean shouldEnable = val != null && val.trim().length() > 0;
                submitButton.setEnabled(shouldEnable);
            }
        });
        name.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                boolean shouldEnable = event.getValue() != null && event.getValue().trim().length() > 0;
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
        name.setFocus(true);
    }

    /**
     * Called when the user clicks the submit button.
     * @param event
     */
    @EventHandler("add-property-submit-button")
    protected void onSubmit(ClickEvent event) {
        final String key = name.getValue();
        final String val = value.getValue();
        ValueChangeEvent.fire(this, new Map.Entry<String, String>() {
            @Override
            public String setValue(String value) {
                return null;
            }
            @Override
            public String getValue() {
                return val;
            }

            @Override
            public String getKey() {
                return key;
            }
        });
        hide();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Entry<String, String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
