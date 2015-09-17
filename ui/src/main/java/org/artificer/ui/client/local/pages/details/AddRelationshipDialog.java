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
package org.artificer.ui.client.local.pages.details;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Dialog that allows the user to create a new artifact relationship.
 *
 * @author Brett Meyer
 */
@Templated("/org/artificer/ui/client/local/site/dialogs/add-relationship-dialog.html#add-relationship-dialog")
@Dependent
public class AddRelationshipDialog extends ModalDialog implements HasValueChangeHandlers<String> {

    @Inject @DataField("relationship-type")
    protected TextBox relationshipType;
    @Inject @DataField("add-relationship-next-button")
    protected Button nextButton;

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        nextButton.setEnabled(false);
        relationshipType.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String val = relationshipType.getValue();
                boolean shouldEnable = val != null && val.trim().length() > 0;
                nextButton.setEnabled(shouldEnable);
            }
        });
        relationshipType.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                boolean shouldEnable = event.getValue() != null && event.getValue().trim().length() > 0;
                nextButton.setEnabled(shouldEnable);
            }
        });
    }

    /**
     * @see org.overlord.commons.gwt.client.local.widgets.ModalDialog#show()
     */
    @Override
    public void show() {
        super.show();
        nextButton.setFocus(true);
    }

    /**
     * Called when the user clicks the next button.
     * @param event
     */
    @EventHandler("add-relationship-next-button")
    protected void onNext(ClickEvent event) {
        ValueChangeEvent.fire(this, relationshipType.getValue());
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
