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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.overlord.sramp.ui.client.local.widgets.common.EditableInlineLabel;

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

    @Inject
    private Instance<EditableInlineLabel> editableLabelFactory;

    private List<String> value;

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
        return value;
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
            this.value = null;
        } else {
            this.value = new ArrayList<String>(value);
            Set<String> classifiers = new TreeSet<String>(value);
            for (final String classifier : classifiers) {
                EditableInlineLabel classifierLabel = editableLabelFactory.get();
                classifierLabel.setValue(classifier);
                classifierLabel.setSupportsEdit(false);
                classifierLabel.setSupportsRemove(true);
                classifierLabel.setStyleName("sramp-meta-data-section-label"); //$NON-NLS-1$
                classifierLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                        if (event.getValue() == null) {
                            List<String> newValue = new ArrayList<String>(ClassifiersPanel.this.value);
                            newValue.remove(classifier);
                            setValue(newValue, true);
                        }
                        // Editing an existing classifier is not currently supported - instead users
                        // must remove and then add.
                    }
                });
                InlineLabel clearFix = new InlineLabel();
                clearFix.setStyleName("clearfix"); //$NON-NLS-1$
                add(classifierLabel);
                add(clearFix);
            }
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, this.value);
        }
    }

}
