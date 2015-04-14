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
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Dialog that allows the user to edit an artifact relationship.
 *
 * @author Brett Meyer
 */
@Templated("/org/artificer/ui/client/local/site/dialogs/edit-relationship-dialog.html#edit-relationship-dialog")
@Dependent
public class EditRelationshipDialog extends ModalDialog implements HasClickHandlers {

    @Inject @DataField("relationship-type")
    protected TextBox relationshipType;
    @Inject @DataField("edit-relationship-save-button")
    protected Button saveButton;

    public String getRelationshipType() {
        return relationshipType.getValue();
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType.setValue(relationshipType);
    }

    /**
     * Called when the user clicks the save button.
     * @param event
     */
    @EventHandler("edit-relationship-save-button")
    protected void onSave(ClickEvent event) {
        hide();
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return saveButton.addClickHandler(handler);
    }
}
