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
package org.overlord.sramp.ui.client.local.widgets.bootstrap;

import org.overlord.sramp.ui.client.local.widgets.common.UnorderedListPanel;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Models a bootstrap Split Button Dropdown.  For details, see:
 *
 *   http://twitter.github.com/bootstrap/components.html#buttonDropdowns
 *
 * @author eric.wittmann@redhat.com
 */
public class SplitButtonDropdown extends FlowPanel {

    private Button actionButton;
    private UnorderedListPanel actions;

    /**
     * Constructor.
     */
    public SplitButtonDropdown() {
        this(ButtonSizeEnum.normal);
    }

    /**
     * Sets the split button dropdown's label.
     * @param label
     */
    public void setLabel(String label) {
        this.actionButton.setText(label);
    }

    /**
     * Constructor.
     * @param size
     */
    public SplitButtonDropdown(ButtonSizeEnum size) {
        actionButton = new Button();
        actionButton.setText("..."); //$NON-NLS-1$
        actionButton.getElement().setClassName(size.getButtonClasses());

        Button ddBtn = new Button();
        ddBtn.setHTML("<span class=\"caret\"></span>"); //$NON-NLS-1$
        ddBtn.getElement().setClassName(size.getButtonClasses() + " dropdown-toggle"); //$NON-NLS-1$
        ddBtn.getElement().setAttribute("data-toggle", "dropdown"); //$NON-NLS-1$ //$NON-NLS-2$

        actions = new UnorderedListPanel();
        actions.getElement().setClassName("dropdown-menu"); //$NON-NLS-1$

        add(actionButton);
        add(ddBtn);
        add(actions);
    }

    /**
     * Adds an option to the dropdown.
     * @param label
     * @param value
     */
    public Anchor addOption(String label, String value) {
        Anchor a = new Anchor(label);
        a.getElement().setClassName(""); //$NON-NLS-1$
        a.getElement().setAttribute("data-value", value); //$NON-NLS-1$
        actions.add(a);
        return a;
    }

}
