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
package org.overlord.sramp.ui.client.local.pages.artifacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;

/**
 * A widget used to filter by classifiers.  This is a container for multiple
 * {@link ClassifierFilter} but also manages the lifecycle of pulling the
 * ontology data from the server.
 *
 * @author eric.wittmann@redhat.com
 */
public class CustomPropertyFilters extends FlowPanel implements HasValue<Map<String, String>>, ValueChangeHandler<String> {

    @Inject
    private Instance<CustomPropertyFilter> customPropertyFilterFactory;
    private List<CustomPropertyFilter> filters = new ArrayList<CustomPropertyFilter>();

    /**
     * Constructor.
     */
    public CustomPropertyFilters() {
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
        Map<String, String> value = new HashMap<String, String>();
        for (CustomPropertyFilter filter : this.filters) {
            String propName = filter.getPropertyName();
            String propVal = filter.getValue();
            value.put(propName, propVal);
        }
        return value;
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
        if (value == null) {
            value = new HashMap<String, String>();
        }

        // Now create filter controls for each custom property filter value.
        clear();
        this.filters.clear();
        for (Entry<String, String> entry : value.entrySet()) {
            String propName = entry.getKey();
            String propVal = entry.getValue();
            CustomPropertyFilter filter = this.customPropertyFilterFactory.get();
            filter.setPropertyName(propName);
            filter.setValue(propVal);
            filter.addValueChangeHandler(this);
            this.add(filter);
            this.filters.add(filter);
        }
    }

    /**
     * Called to add another custom property filter.
     * @param propertyName
     */
    public void addPropertyFilter(String propertyName) {
        CustomPropertyFilter filter = this.customPropertyFilterFactory.get();
        filter.setPropertyName(propertyName);
        filter.addValueChangeHandler(this);
        this.add(filter);
        this.filters.add(filter);
        ValueChangeEvent.fire(this, getValue());
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        // One of the filters was removed
        if (event.getValue() == null) {
            CustomPropertyFilter filter = (CustomPropertyFilter) event.getSource();
            remove(filter);
            filters.remove(filter);
        }
        ValueChangeEvent.fire(this, getValue());
    }
}
