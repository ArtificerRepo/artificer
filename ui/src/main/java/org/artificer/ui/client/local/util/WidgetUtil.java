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
package org.artificer.ui.client.local.util;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;

/**
 * Some utils used by UI widgets.
 * @author eric.wittmann@redhat.com
 */
public final class WidgetUtil {

    /**
     * Initializes the given widget by hooking up mouse-in/mouse-out
     * handlers.
     * @param widget
     */
    public static void initMouseInOutWidget(final IMouseInOutWidget widget) {
        widget.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    addMouseHandlers(widget);
                } else {
                    removeMouseHandlers(widget);
                }
            }
        });
    }

    /**
     * Adds the mouse-in/out handlers for this widget.
     * @param element
     */
    public static native final void addMouseHandlers(final IMouseInOutWidget widget) /*-{
        var element = widget.@org.artificer.ui.client.local.util.IMouseInOutWidget::getElement()();
        $wnd.jQuery(element).mouseenter(function() {
            widget.@org.artificer.ui.client.local.util.IMouseInOutWidget::onMouseIn()();
        });
        $wnd.jQuery(element).mouseleave(function() {
            widget.@org.artificer.ui.client.local.util.IMouseInOutWidget::onMouseOut()();
        });
    }-*/;

    /**
     * Removes the mouse-in handler for this widget.
     * @param element
     */
    public static native final void removeMouseHandlers(final IMouseInOutWidget widget) /*-{
        var element = widget.@org.artificer.ui.client.local.util.IMouseInOutWidget::getElement()();
        $wnd.jQuery(element).unbind('mouseenter mouseleave');
    }-*/;

}
