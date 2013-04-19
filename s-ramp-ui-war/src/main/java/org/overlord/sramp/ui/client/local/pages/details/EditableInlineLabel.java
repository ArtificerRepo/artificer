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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.overlord.sramp.ui.client.local.util.IMouseInOutWidget;
import org.overlord.sramp.ui.client.local.util.WidgetUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * Widget used to display an editable value in a SPAN.
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class EditableInlineLabel extends InlineLabel implements HasValue<String>, IMouseInOutWidget {

    @Inject
    private Instance<EditableInlineLabelPopover> popoverFactory;
    private EditableInlineLabelPopover popover = null;
    @Inject
    private Instance<EditCustomPropertyDialog> editDialogFactory;
    private boolean supportsRemove;
    private String value;

    /**
     * Constructor.
     */
    public EditableInlineLabel() {
    }

    /**
     * Post construct.
     */
    @PostConstruct
    protected void onPostConstruct() {
        WidgetUtil.initMouseInOutWidget(this);
    }

    /**
     * Called when the mouse enters.
     */
    @Override
    public void onMouseIn() {
        // Guard against possibly getting the event multiple times.
        if (popover == null || !popover.isAttached()) {
            popover = popoverFactory.get();
            popover.setSupportsRemove(supportsRemove);
            if (supportsRemove) {
                popover.getRemoveButton().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        setValue(null, true);
                        popover.close();
                    }
                });
            }
            popover.getEditButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    onEditProperty();
                    popover.close();
                }
            });
            popover.showOver(getElement());
        }
    }

    /**
     * Called when the user clicks the Edit action.
     */
    protected void onEditProperty() {
        EditCustomPropertyDialog dialog = editDialogFactory.get();
        dialog.setValue(getValue());
        dialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                setValue(event.getValue(), true);
            }
        });
        dialog.show();
    }

    /**
     * Called when the mouse leaves.
     */
    @Override
    public void onMouseOut() {
        // Nothing to do.
    }

    /**
     * @param flag indicates whether 'remove' is supported for this property
     */
    public void setSupportsRemove(boolean flag) {
        this.supportsRemove = flag;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(String value, boolean fireEvents) {
        this.setText(value);
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
