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

package org.artificer.ui.client.local.widgets.common;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import org.overlord.commons.gwt.client.local.widgets.UnorderedListPanel;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Models a simple dropdown using bootstrap CSS.  This class must be injected using
 * Errai to work properly.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class DropdownMenu extends FlowPanel {
    
    private static int idCounter = 1;
    private static int nextId() {
        return idCounter++;
    }

    @Inject
    Button labelButton;
    @Inject
    Button caretButton;
    @Inject
    UnorderedListPanel dropdown;

    public void setLabel(String label) {
        labelButton.setText(label);
    }
    
    /**
     * Called after the widget is created.
     */
    @PostConstruct
    protected void postConstruct() {
        getElement().setId("menu" + nextId());
        getElement().setClassName("btn-group");

        labelButton.getElement().setClassName("btn");
        add(labelButton);

        caretButton.getElement().setClassName("btn dropdown-toggle");
        caretButton.getElement().setAttribute("data-toggle", "dropdown");
        caretButton.setHTML("<span class=\"caret\"></span>");
        add(caretButton);

        dropdown.getElement().setClassName("dropdown-menu");
        add(dropdown);
    }
    
    /**
     * @return the menu's html id
     */
    public String getMenuId() {
        return getElement().getId();
    }
    
    /**
     * Adds an action to the menu.
     * @param label
     */
    public Anchor addMenuAction(String label) {
        Anchor a = new Anchor(label);
        a.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        dropdown.add(a);
        return a;
    }

    /**
     * Adds a divider.
     */
    public void addDivider() {
        InlineLabel d = new InlineLabel();
        dropdown.add(d);
        dropdown.setLiClass(d, "divider");
    }

    /**
     * Hides the menu.
     */
    public void hide() {

    }

}
