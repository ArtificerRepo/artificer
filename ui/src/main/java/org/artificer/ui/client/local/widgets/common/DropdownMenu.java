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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.overlord.commons.gwt.client.local.widgets.UnorderedListPanel;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a simple dropdown using bootstrap CSS.  This class must be injected using
 * Errai to work properly.
 *
 * @author eric.wittmann@redhat.com
 */
// TODO: No longer used!
@Dependent
public class DropdownMenu extends FlowPanel {
    
    private static int idCounter = 1;
    private static int nextId() {
        return idCounter++;
    }
    
    @Inject
    RootPanel rootPanel;
    @Inject
    UnorderedListPanel dropdown;
    @Inject
    FlowPanel glass;
    
    /**
     * Constructor.
     */
    public DropdownMenu() {
    }
    
    /**
     * Called after the widget is created.
     */
    @PostConstruct
    protected void postConstruct() {
        getElement().setId("menu" + nextId()); //$NON-NLS-1$
        getElement().setClassName("dropdown"); //$NON-NLS-1$
        dropdown.getElement().setClassName("dropdown-menu"); //$NON-NLS-1$
        dropdown.getElement().setAttribute("role", "menu"); //$NON-NLS-1$ //$NON-NLS-2$
        add(dropdown);
        glass.getElement().setClassName("menu-glass"); //$NON-NLS-1$
        glass.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        }, ClickEvent.getType());
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
                onActionClicked();
            }
        });
        a.getElement().setAttribute("role", "menuitem"); //$NON-NLS-1$ //$NON-NLS-2$
        dropdown.add(a);
        return a;
    }
    
    /**
     * Called when the user clicks on one of the actions.
     */
    protected void onActionClicked() {
        hide();
    }

    /**
     * Adds a divider.
     */
    public void addDivider() {
        InlineLabel d = new InlineLabel();
        dropdown.add(d);
        dropdown.setLiClass(d, "divider"); //$NON-NLS-1$
    }
    
    /**
     * Shows the menu relative to the mouse position.  Requires a mouse event
     * to figure out where the mouse pointer currently is located.
     * @param event
     */
    public void showAtCurrentMouseLocation(MouseEvent<?> event) {
        rootPanel.add(glass);
        rootPanel.add(this);
        Style style = this.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setTop(event.getY(), Unit.PX);
        style.setLeft(event.getClientX(), Unit.PX);
        dropdown.getElement().getStyle().setDisplay(Display.BLOCK);
    }
    
    /**
     * Shows the menu relative to the location of the given widget.
     * @param widget
     */
    public void showRelativeTo(Widget widget) {
        rootPanel.add(glass);
        rootPanel.add(this);
        Style style = this.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setTop(widget.getElement().getAbsoluteBottom() + 2, Unit.PX);
        style.setLeft(widget.getElement().getAbsoluteLeft(), Unit.PX);
        dropdown.getElement().getStyle().setDisplay(Display.BLOCK);
    }
    
    /**
     * Hides the menu.
     */
    public void hide() {
        rootPanel.remove(this);
        rootPanel.remove(glass);
    }

}
