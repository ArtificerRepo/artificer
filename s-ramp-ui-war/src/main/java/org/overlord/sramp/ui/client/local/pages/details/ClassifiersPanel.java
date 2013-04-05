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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A panel that holds all of the artifact's classifiers.
 *
 * @author eric.wittmann@redhat.com
 */
public class ClassifiersPanel extends FlowPanel implements HasValue<List<String>> {

    /**
     * Constructor.
     */
    public ClassifiersPanel() {
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<String> getValue() {
        // TODO Implement getValue()
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<String> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<String> value, boolean fireEvents) {
        clear();
        if (value == null || value.isEmpty()) {
            // Put something here?  "No Properties found..." ?
        } else {
            Set<String> classifiers = new TreeSet<String>(value);
            for (String classifier : classifiers) {
                InlineLabel classifierLabel = new InlineLabel(classifier);
                classifierLabel.setStyleName("sramp-meta-data-section-label");
                InlineLabel clearFix = new InlineLabel();
                clearFix.setStyleName("clearfix");
                add(classifierLabel);
                add(clearFix);
            }
        }
    }

}
