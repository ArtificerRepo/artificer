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
package org.artificer.ui.client.local.services.notification;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.artificer.ui.client.local.events.MouseInEvent;
import org.artificer.ui.client.local.events.MouseOutEvent;
import org.artificer.ui.client.shared.beans.NotificationType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The UI for a single notification.  This is a little growl window that
 * appears stacked on the bottom-right of the browser window, regardless of
 * the page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/artificer/ui/client/local/site/dialogs/growl-dialog.html#growl-dialog")
@Dependent
public class NotificationWidget extends Composite implements MouseInEvent.HasMouseInHandlers, MouseOutEvent.HasMouseOutHandlers {

    @Inject @DataField("growl-dialog-header-title")
    private Label title;
    @Inject @DataField("growl-dialog-header-closeButton")
    private Button closeButton;
    @Inject @DataField("growl-dialog-body")
    private FlowPanel body;

    private HandlerRegistration nativePreviewHandlerRegistration;
    private boolean mouseIn = false;

    /**
     * Constructor.
     */
    public NotificationWidget() {
    }

    /**
     * Called after the widget is constructed.
     */
    @PostConstruct
    protected void onPostConstruct() {
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    nativePreviewHandlerRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {
                        @Override
                        public void onPreviewNativeEvent(NativePreviewEvent event) {
                            NotificationWidget.this.onPreviewNativeEvent(event);
                        }
                    });
                } else {
                    nativePreviewHandlerRegistration.removeHandler();
                    nativePreviewHandlerRegistration = null;
                }
            }
        });

    }

    /**
     * Sets the notification title.
     * @param title
     */
    public void setNotificationTitle(String title) {
        this.title.setText(title);
    }

    /**
     * Sets the notification message.
     * @param message
     * @param notificationType
     */
    public void setNotificationMessage(String message, NotificationType notificationType) {
        this.body.clear();
        InlineLabel msg = new InlineLabel(message);
        if (notificationType == NotificationType.notification) {
            this.body.add(msg);
        } else if (notificationType == NotificationType.warning) {
                this.body.add(msg);
        } else if (notificationType == NotificationType.error) {
            this.body.add(msg);
        } else if (notificationType == NotificationType.progress) {
            msg.addStyleName("spinner"); //$NON-NLS-1$
            this.body.add(msg);
        }
    }

    /**
     * Sets the notification message.
     * @param message
     */
    public void setNotificationMessage(Widget message) {
        this.body.clear();
        this.body.add(message);
    }

    /**
     * Positions the notifiaction widget.
     * @param bottom
     * @param right
     */
    public void positionBottom(int bottom, int right) {
        this.getElement().getStyle().setBottom(bottom, Unit.PX);
        this.getElement().getStyle().setRight(right, Unit.PX);
    }

    /**
     * @return the closeButton
     */
    public Button getCloseButton() {
        return closeButton;
    }

    /**
     * Processes a native event so that we can track mouse movements in and
     * out of this widget.
     * @param event
     */
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        if (event.getTypeInt() == Event.ONMOUSEMOVE) {
            int x = event.getNativeEvent().getClientX();
            int y = event.getNativeEvent().getClientY();
            handleMouseMove(x, y);
        }
    }

    /**
     * Called every time the mouse moves.  This method tries to determine proper mouse-in and
     * mouse-out events.
     * @param clientX
     * @param clientY
     */
    private void handleMouseMove(int clientX, int clientY) {
        if (isMouseInMe(clientX, clientY)) {
            if (this.mouseIn) {
                // do nothing
            } else {
                this.mouseIn = true;
                onMouseIn();
            }
        } else {
            if (this.mouseIn) {
                onMouseOut();
                this.mouseIn = false;
            }
        }
    }

    /**
     * Returns true if the given screen coordinates lie within the boundary of the growl dialog.
     * @param clientX
     * @param clientY
     */
    private boolean isMouseInMe(int clientX, int clientY) {
        try {
            String bottomStyle = getElement().getStyle().getBottom();
            int bottom = new Integer(bottomStyle.split("px")[0]).intValue(); //$NON-NLS-1$
            bottom = Window.getClientHeight() - bottom;

            int top = bottom - getOffsetHeight();
            int left = Window.getClientWidth() - NotificationConstants.WIDTH - NotificationConstants.MARGIN;
            int right = left + NotificationConstants.WIDTH;

            return clientX >= left && clientX <= right && clientY >= top && clientY <= bottom;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Called when the mouse enters the dialog.
     */
    protected void onMouseIn() {
        addStyleName("growl-dialog-hover"); //$NON-NLS-1$
        MouseInEvent.fire(this);
    }

    /**
     * Called when the mouse leaves the dialog.
     */
    protected void onMouseOut() {
        removeStyleName("growl-dialog-hover"); //$NON-NLS-1$
        MouseOutEvent.fire(this);
    }

    /**
     * @see org.artificer.ui.client.local.events.MouseOutEvent.HasMouseOutHandlers#addMouseOutHandler(org.artificer.ui.client.local.events.MouseOutEvent.Handler)
     */
    @Override
    public HandlerRegistration addMouseOutHandler(
            MouseOutEvent.Handler handler) {
        return addHandler(handler, MouseOutEvent.getType());
    }

    /**
     * @see org.artificer.ui.client.local.events.MouseInEvent.HasMouseInHandlers#addMouseInHandler(org.artificer.ui.client.local.events.MouseInEvent.Handler)
     */
    @Override
    public HandlerRegistration addMouseInHandler(
            MouseInEvent.Handler handler) {
        return addHandler(handler, MouseInEvent.getType());
    }

}
