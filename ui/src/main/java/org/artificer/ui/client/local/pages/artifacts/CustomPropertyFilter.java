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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.artificer.ui.client.local.widgets.common.EditableInlineLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A single custom property filter in the UI (Artifacts page).
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/artificer/ui/client/local/site/artifacts.html#filter-custom-property")
@Dependent
public class CustomPropertyFilter extends Composite implements HasValue<String> {

    @Inject @DataField("filter-custom-property-label")
    private EditableInlineLabel label;
    @Inject @DataField("filter-custom-property-input")
    private TextBox input;

    private String propertyName;

    /**
     * Constructor.
     */
    public CustomPropertyFilter() {
    }

    /**
     * Post construct.
     */
    @PostConstruct
    protected void onPostConstruct() {
        label.setSupportsEdit(false);
        label.setSupportsRemove(true);
        label.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (event.getValue() == null) {
                    GWT.log("Firing null value change event!"); //$NON-NLS-1$
                    ValueChangeEvent.fire(CustomPropertyFilter.this, null);
                }
            }
        });
        input.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                ValueChangeEvent.fire(CustomPropertyFilter.this, event.getValue());
            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        return input.getValue();
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
        input.setValue(value, fireEvents);
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @param propertyName the propertyName to set
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        this.label.setValue(propertyName + ":"); //$NON-NLS-1$
    }

}
