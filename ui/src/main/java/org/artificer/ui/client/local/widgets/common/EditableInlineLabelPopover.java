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

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.artificer.ui.client.local.util.IMouseInOutWidget;
import org.artificer.ui.client.local.util.WidgetUtil;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.Window.ScrollHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * An overlay/popover shown to the user when they mouse-over an editable
 * field in the Artifact Details page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/artificer/ui/client/local/site/dialogs/propval-editor-popover.html#propval-editor-popover")
@Dependent
public class EditableInlineLabelPopover extends Composite implements IMouseInOutWidget {

    @Inject
    private RootPanel rootPanel;
    private HandlerRegistration registration = null;

    @Inject @DataField
    private Anchor edit;
    @Inject @DataField
    private Anchor remove;

    /**
     * Constructor.
     */
    public EditableInlineLabelPopover() {
    }

    /**
     * Whether the "remove" button should be shown.
     * @param flag
     */
    public void setSupportsRemove(boolean flag) {
        if (flag) {
            remove.getElement().removeClassName("hide"); //$NON-NLS-1$
        } else {
            remove.getElement().addClassName("hide"); //$NON-NLS-1$
        }
    }

    /**
     * Whether the "edit" button should be shown.
     * @param flag
     */
    public void setSupportsEdit(boolean flag) {
        if (flag) {
            edit.getElement().removeClassName("hide"); //$NON-NLS-1$
        } else {
            edit.getElement().addClassName("hide"); //$NON-NLS-1$
        }
    }

    @PostConstruct
    protected void onPostConstruct() {
        WidgetUtil.initMouseInOutWidget(this);
    }

    /**
     * Add to the root panel and then position appropriately.
     */
    public void showOver(Element element) {
        rootPanel.add(this);
        getElement().removeClassName("hide"); //$NON-NLS-1$
        positionOver(getElement(), element);
        registration = Window.addWindowScrollHandler(new ScrollHandler() {
            @Override
            public void onWindowScroll(ScrollEvent event) {
                close();
            }
        });
    }

    /**
     * Positions the given popoverElement on top of the given widget element.
     * @param popoverElement
     * @param element
     */
    protected native final void positionOver(Element popoverElement, Element widgetElement) /*-{
        var offset = $wnd.jQuery(widgetElement).offset();
        var height = $wnd.jQuery(widgetElement).height() + 8;
        var width = $wnd.jQuery(widgetElement).width() + 8;
        $wnd.jQuery(popoverElement).find('.overlay').width(width);
        $wnd.jQuery(popoverElement).find('.overlay').height(height);
        $wnd.jQuery(popoverElement).offset({
            top: offset.top - 4,
            left: offset.left - 4
        });
    }-*/;

    /**
     * @see org.artificer.ui.client.local.util.IMouseInOutWidget#onMouseIn()
     */
    @Override
    public void onMouseIn() {
    }

    /**
     * @see org.artificer.ui.client.local.util.IMouseInOutWidget#onMouseOut()
     */
    @Override
    public void onMouseOut() {
        close();
    }

    /**
     * Closes the popover.
     */
    public void close() {
        if (this.registration != null) {
            this.registration.removeHandler();
            this.registration = null;
        }
        rootPanel.remove(this);
    }

    /**
     * @return the edit
     */
    public Anchor getEditButton() {
        return edit;
    }

    /**
     * @return the remove
     */
    public Anchor getRemoveButton() {
        return remove;
    }
}
