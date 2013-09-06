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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

/**
 * A panel that holds all of the artifact's custom properties.
 *
 * @author eric.wittmann@redhat.com
 */
public class CustomPropertiesPanel extends FlowPanel implements HasValue<Map<String, String>> {

    @Inject
    private Instance<CustomPropertyEditableInlineLabel> propValueLabelFactory;
    private HashMap<String, String> value;

    /**
     * Constructor.
     */
    public CustomPropertiesPanel() {
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Map<String, String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public Map<String, String> getValue() {
        // TODO Implement getValue()
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Map<String, String> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(Map<String, String> value, boolean fireEvents) {
        this.value = new HashMap<String, String>(value);
        clear();
        if (value == null || value.isEmpty()) {
            // Put something here?  "No Properties found..." ?
        } else {
            Set<String> keys = new TreeSet<String>(value.keySet());
            for (final String propName : keys) {
                String propValue = value.get(propName);
                Label propLabel = new Label(propName + ":"); //$NON-NLS-1$
                propLabel.setStyleName("sramp-meta-data-section-label"); //$NON-NLS-1$
                CustomPropertyEditableInlineLabel propValueWidget = propValueLabelFactory.get();
                propValueWidget.addValueChangeHandler(new ValueChangeHandler<String>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                        Map<String, String> newValue = new HashMap<String, String>(CustomPropertiesPanel.this.value);
                        String val = event.getValue();
                        if (val == null) {
                            newValue.remove(propName);
                        } else {
                            newValue.put(propName, val);
                        }
                        setValue(newValue, true);
                    }
                });
                propValueWidget.setValue(propValue);
                propValueWidget.setStyleName("sramp-meta-data-section-value"); //$NON-NLS-1$
                Label clearFix = new Label();
                clearFix.setStyleName("clearfix"); //$NON-NLS-1$
                add(propLabel);
                add(propValueWidget);
                add(clearFix);
            }
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, this.value);
        }
    }

}
