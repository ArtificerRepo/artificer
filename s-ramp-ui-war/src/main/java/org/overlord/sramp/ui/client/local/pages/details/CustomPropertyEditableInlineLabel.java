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

import javax.enterprise.context.Dependent;

import org.overlord.sramp.ui.client.local.widgets.common.EditableInlineLabel;

/**
 * Widget used to display the value of a property.
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class CustomPropertyEditableInlineLabel extends EditableInlineLabel {

    /**
     * Constructor.
     */
    public CustomPropertyEditableInlineLabel() {
        setSupportsRemove(true);
    }

    /**
     * @see com.google.gwt.user.client.ui.Label#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        if (text == null || text.trim().length() == 0) {
            // TODO i18n
            this.setText("<no value>");
        } else if (text.length() > 64) {
            this.setText(text.substring(0, 64) + "...");
        } else {
            super.setText(text);
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(String value, boolean fireEvents) {
        super.setValue(value, fireEvents);
    }
}
