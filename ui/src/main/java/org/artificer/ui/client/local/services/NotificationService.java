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
package org.artificer.ui.client.local.services;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.artificer.ui.client.shared.beans.NotificationBean;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.artificer.ui.client.local.animations.FadeOutAnimation;
import org.artificer.ui.client.local.animations.MoveAnimation;
import org.artificer.ui.client.local.events.MouseInEvent;
import org.artificer.ui.client.local.events.MouseOutEvent;
import org.artificer.ui.client.local.services.notification.Notification;
import org.artificer.ui.client.local.services.notification.NotificationConstants;
import org.artificer.ui.client.local.services.notification.NotificationWidget;
import org.artificer.ui.client.shared.beans.NotificationType;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A lightweight notification service (client-side).
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class NotificationService {

    @Inject
    private MessageBus bus;
    @Inject
    private RequestDispatcher dispatcher;
    @Inject
    private RootPanel rootPanel;
    @Inject
    private Instance<NotificationWidget> notificationWidgetFactory;

    private List<Notification> activeNotifications = new ArrayList<Notification>();
    private int notificationCounter = 0;

    /**
     * Constructor.
     */
    public NotificationService() {
    }

    /**
     * Called when the service is constructed.
     */
    @PostConstruct
    private void onPostConstruct() {
        bus.subscribe("NotificationService", new MessageCallback() { //$NON-NLS-1$
            @Override
            public void callback(Message message) {
                NotificationBean notification = message.getValue(NotificationBean.class);
                doNotify(notification);
            }
        });
    }

    /**
     * Sends a simple notification to the user.
     * @param title
     * @param message
     */
    public final void sendNotification(String title, String message) {
        NotificationBean bean = new NotificationBean();
        bean.setUuid(String.valueOf(notificationCounter++));
        bean.setType(NotificationType.notification);
        bean.setTitle(title);
        bean.setMessage(message);
        sendNotification(bean);
    }

    /**
     * Sends an error notification to the user.
     * @param title
     * @param message
     * @param exception
     */
    public final void sendErrorNotification(String title, String message, ArtificerUiException exception) {
        NotificationBean bean = new NotificationBean();
        bean.setUuid(String.valueOf(notificationCounter++));
        bean.setType(NotificationType.error);
        bean.setTitle(title);
        bean.setMessage(message);
        bean.setException(exception);
        sendNotification(bean);
    }

    /**
     * Sends an warning notification to the user.
     * @param title
     * @param message
     * @param exception
     */
    public final void sendWarningNotification(String title, String message) {
        NotificationBean bean = new NotificationBean();
        bean.setUuid(String.valueOf(notificationCounter++));
        bean.setType(NotificationType.warning);
        bean.setTitle(title);
        bean.setMessage(message);
        sendNotification(bean);
    }

    /**
     * Sends an error notification to the user.
     * @param title
     * @param exception
     */
    public final void sendErrorNotification(String title, ArtificerUiException exception) {
        sendErrorNotification(title, exception.getMessage(), exception);
    }

    /**
     * Sends an error notification to the user.
     * @param title
     * @param exception
     */
    public final void sendErrorNotification(String title, Throwable exception) {
        if (exception instanceof ArtificerUiException) {
            sendErrorNotification(title, (ArtificerUiException) exception);
        } else {
            sendErrorNotification(title, exception.getMessage(), null);
        }
    }

    /**
     * Starts a progress style notification.  This displays the message to the user, along
     * with displaying a spinner indicating that a background task is running.
     * @param title
     * @param message
     * @param exception
     */
    public final NotificationBean startProgressNotification(String title, String message) {
        NotificationBean bean = new NotificationBean();
        bean.setUuid(String.valueOf(notificationCounter++));
        bean.setType(NotificationType.progress);
        bean.setTitle(title);
        bean.setMessage(message);
        sendNotification(bean);
        return bean;
    }

    /**
     * Completes an in-progress (progress-style) notification.
     * @param title
     * @param message
     * @param exception
     */
    public final void completeProgressNotification(String uuid, String title, String message) {
        NotificationBean bean = new NotificationBean();
        bean.setUuid(uuid);
        bean.setType(NotificationType.progressCompleted);
        bean.setTitle(title);
        bean.setMessage(message);
        sendNotification(bean);
    }

    /**
     * Completes an in-progress (progress-style) notification.
     * @param title
     * @param widget
     * @param exception
     */
    public final void completeProgressNotification(String uuid, String title, Widget widget) {
        NotificationBean bean = new NotificationBean();
        bean.setUuid(uuid);
        bean.setType(NotificationType.progressCompleted);
        bean.setTitle(title);
        bean.setMessageWidget(widget);
        sendNotification(bean);
    }

    /**
     * Completes an in-progress (progress-style) notification.
     * @param title
     * @param message
     * @param exception
     */
    public final void completeProgressNotification(String uuid, String title, ArtificerUiException error) {
        NotificationBean bean = new NotificationBean();
        bean.setUuid(uuid);
        bean.setType(NotificationType.progressErrored);
        bean.setTitle(title);
        bean.setException(error);
        sendNotification(bean);
    }

    /**
     * Completes an in-progress (progress-style) notification.
     * @param uuid
     * @param title
     * @param error
     */
    public void completeProgressNotification(String uuid, String title, Throwable error) {
        if (error instanceof ArtificerUiException) {
            completeProgressNotification(uuid, title, (ArtificerUiException) error);
        } else {
            completeProgressNotification(uuid, title, new ArtificerUiException(error));
        }
    }

    /**
     * Sends a notification (local/client only).
     * @param notification
     */
    protected void sendNotification(NotificationBean notification) {
        MessageBuilder.createMessage()
            .toSubject("NotificationService") //$NON-NLS-1$
            .signalling()
            .withValue(notification)
            .noErrorHandling()
            .sendNowWith(dispatcher);
    }

    /**
     * Notifies the user.
     * @param notification
     */
    private void doNotify(NotificationBean notificationBean) {
        if (notificationBean.getType() == NotificationType.progressCompleted) {
            onProgressComplete(notificationBean);
        } else if (notificationBean.getType() == NotificationType.progressErrored) {
            onProgressError(notificationBean);
        } else {
            final Notification notification = createNotification(notificationBean);
            createNotificationWidget(notification);
            createNotificationTimers(notification);
            createNotificationAnimations(notification);

            positionAndShowNotificationDialog(notification);

            // Schedule the notification to go away automatically.
            if (notificationBean.getType() == NotificationType.notification || notificationBean.getType() == NotificationType.warning)
                notification.getAliveTimer().schedule(5000);
        }
    }

    /**
     * Creates any timers needed to control the notification.
     * @param notification
     */
    private void createNotificationTimers(final Notification notification) {
        // Create the timer that will control when the notification automatically goes away.
        Timer aliveTimer = new Timer() {
            @Override
            public void run() {
                notification.getAutoCloseAnimation().run(1000);
            }
        };
        notification.setAliveTimer(aliveTimer);
    }

    /**
     * Creates any animations needed by the notification.
     * @param notification
     */
    private void createNotificationAnimations(final Notification notification) {
        // Create the animation used to make the notification go away (when the time comes)
        FadeOutAnimation fadeOut = new FadeOutAnimation(notification.getWidget()) {
            @Override
            protected void doOnComplete() {
                rootPanel.remove(notification.getWidget());
                onNotificationClosed(notification);
            }
        };
        notification.setAutoCloseAnimation(fadeOut);
    }

    /**
     * Called when a notification is closed, either by the user clicking on the close button
     * or the alive timer fires.
     * @param notification
     */
    protected void onNotificationClosed(final Notification notification) {
        activeNotifications.remove(notification);
        rootPanel.remove(notification.getWidget());
        notification.setWidget(null);
        repositionNotifications();
    }

    /**
     * Repositions all of the notifications.
     */
    private void repositionNotifications() {
        int bottom = NotificationConstants.MARGIN;
        for (int notificationIndex = 0; notificationIndex < activeNotifications.size(); notificationIndex++) {
            Notification notification = activeNotifications.get(notificationIndex);
            // Only move a notification if it needs it (if its current index is different
            // from the index we think it needs to be)
            if (notification.getIndex() != notificationIndex) {
                moveNotificationTo(notification.getWidget(), bottom);
                notification.setIndex(notificationIndex);
            }
            // Update the desired position for the next notification widget
            bottom += notification.getWidget().getOffsetHeight() + NotificationConstants.MARGIN;
        }
    }

    /**
     * Repositions the notifications when one is possibly resized.
     */
    private void resizeNotification(int startingAtIndex) {
        int bottom = NotificationConstants.MARGIN;
        for (int notificationIndex = 0; notificationIndex < activeNotifications.size(); notificationIndex++) {
            Notification notification = activeNotifications.get(notificationIndex);
            // Only move a notification if it needs it (if its current index is different
            // from the index we think it needs to be)
            if (notification.getIndex() >= startingAtIndex) {
                moveNotificationTo(notification.getWidget(), bottom);
                notification.setIndex(notificationIndex);
            }
            // Update the desired position for the next notification widget
            bottom += notification.getWidget().getOffsetHeight() + NotificationConstants.MARGIN;
        }
    }

    /**
     * Moves a notificaton from its current position to the new position provided.
     * @param widget
     * @param bottom
     */
    private void moveNotificationTo(NotificationWidget widget, int bottom) {
        int fromBottom = new Integer(widget.getElement().getStyle().getBottom().split("px")[0]).intValue(); //$NON-NLS-1$
        int toBottom = bottom;
        MoveAnimation animation = new MoveAnimation(widget, "bottom", fromBottom, toBottom); //$NON-NLS-1$
        animation.run(200);
    }

    /**
     * Creates the UI widget for the notification.
     * @param notification
     */
    private void createNotificationWidget(final Notification notification) {
        NotificationWidget widget = this.notificationWidgetFactory.get();
        String additionalClass = "growl-dialog-" + notification.getData().getType(); //$NON-NLS-1$
        widget.addStyleName(additionalClass);
        widget.setNotificationTitle(notification.getData().getTitle());
        if (notification.getData().getMessageWidget() != null) {
            widget.setNotificationMessage((Widget) notification.getData().getMessageWidget());
        } else if (notification.getData().getType() == NotificationType.error) {
            FlowPanel errorDetails = new FlowPanel();
            if (notification.getData().getMessage() != null) {
                errorDetails.add(new InlineLabel(notification.getData().getMessage()));
            }
            if (notification.getData().getException() != null) {
                // TODO handle exceptions - need to create an exception dialog
            }
            widget.setNotificationMessage(errorDetails);
        } else {
            widget.setNotificationMessage(notification.getData().getMessage(), notification.getData().getType());
        }
        widget.getCloseButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                notification.getAliveTimer().cancel();
                onNotificationClosed(notification);
            }
        });
        widget.addMouseInHandler(new MouseInEvent.Handler() {
            @Override
            public void onMouseIn(MouseInEvent event) {
                notification.getAliveTimer().cancel();
                notification.getAutoCloseAnimation().cancel();
            }
        });
        widget.addMouseOutHandler(new MouseOutEvent.Handler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (notification.getData().getType() == NotificationType.notification) {
                    notification.getAliveTimer().schedule(5000);
                }
            }
        });
        notification.setWidget(widget);
    }

    /**
     * Positions the notification widget.
     * @param notification
     */
    private void positionAndShowNotificationDialog(Notification notification) {
        NotificationWidget widget = notification.getWidget();
        int notificationIndex = notification.getIndex();
        NotificationWidget relativeTo = null;
        if (notificationIndex > 0) {
            relativeTo = activeNotifications.get(notificationIndex-1).getWidget();
        }

        // Show the widget first, but make it invisible (so GWT can do its absolute positioning mojo)
        widget.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        rootPanel.add(widget);

        // Calculate the notification widget's position, either because this is the only one
        // or relative to the one below it.
        int bottom = NotificationConstants.MARGIN;
        int right = NotificationConstants.MARGIN;
        if (relativeTo != null) {
            String relativeTo_bottomStyle = relativeTo.getElement().getStyle().getBottom();
            int relativeTo_bottom = new Integer(relativeTo_bottomStyle.split("px")[0]).intValue(); //$NON-NLS-1$
            bottom = relativeTo_bottom + relativeTo.getOffsetHeight() + NotificationConstants.MARGIN;
        }

        // Now pin the notification to the right using fixed positioning
        widget.getElement().getStyle().setPosition(Position.FIXED);
        widget.getElement().getStyle().setBottom(Window.getClientHeight() + 100, Unit.PX);
        widget.getElement().getStyle().setRight(right, Unit.PX);
        widget.getElement().getStyle().setProperty("left", null); //$NON-NLS-1$
        widget.getElement().getStyle().setProperty("top", null); //$NON-NLS-1$
        widget.getElement().getStyle().setVisibility(Visibility.VISIBLE);
        
        moveNotificationTo(notification.getWidget(), bottom);
    }

    /**
     * Creates a new notification.
     * @param bean
     */
    private Notification createNotification(NotificationBean bean) {
        Notification notification = new Notification(bean);
        int notificationIndex = this.activeNotifications.size();
        notification.setIndex(notificationIndex);
        this.activeNotifications.add(notification);
        return notification;
    }

    /**
     * Gets a notification by notificationId.
     * @param notificationId
     */
    private Notification getNotification(String notificationId) {
        Notification notification = null;
        for (Notification n : this.activeNotifications) {
            if (n.getData().getUuid().equals(notificationId))
                notification = n;
        }
        return notification;
    }

    /**
     * Called when a progress style notification should be completed.
     * @param notificationBean
     */
    private void onProgressComplete(NotificationBean notificationBean) {
        Notification notification = getNotification(notificationBean.getUuid());
        if (notification != null) {
            notification.getData().setTitle(notificationBean.getTitle());
            notification.getData().setMessage(notificationBean.getMessage());
            notification.getData().setMessageWidget(notificationBean.getMessageWidget());
            notification.getData().setType(NotificationType.notification);
            notification.getWidget().setNotificationTitle(notificationBean.getTitle());
            if (notificationBean.getMessageWidget() != null) {
                notification.getWidget().setNotificationMessage((Widget) notificationBean.getMessageWidget());
            } else {
                notification.getWidget().setNotificationMessage(notificationBean.getMessage(), NotificationType.notification);
            }
            notification.getWidget().removeStyleName("growl-dialog-progress"); //$NON-NLS-1$
            notification.getWidget().addStyleName("growl-dialog-notification"); //$NON-NLS-1$
            notification.getAliveTimer().schedule(5000);

            resizeNotification(notification.getIndex() + 1);
        }
    }

    /**
     * Called when a progress style notification has error'd out.
     * @param notificationBean
     */
    private void onProgressError(NotificationBean notificationBean) {
        Notification notification = getNotification(notificationBean.getUuid());
        if (notification != null) {
            notification.getData().setTitle(notificationBean.getTitle());
            notification.getData().setMessage(notificationBean.getMessage());
            notification.getData().setMessageWidget(notificationBean.getMessageWidget());
            notification.getData().setType(NotificationType.error);
            notification.getData().setException(notificationBean.getException());
            notification.getWidget().setNotificationTitle(notificationBean.getTitle());
            if (notificationBean.getMessageWidget() != null) {
                notification.getWidget().setNotificationMessage((Widget) notificationBean.getMessageWidget());
            } else {
                FlowPanel errorDetails = new FlowPanel();
                if (notification.getData().getMessage() != null) {
                    errorDetails.add(new InlineLabel(notification.getData().getMessage()));
                }
                if (notification.getData().getException() != null) {
                    // TODO handle exceptions - need to create an exception dialog
                    errorDetails.add(new InlineLabel(notification.getData().getException().getMessage()));
                }
                notification.getWidget().setNotificationMessage(errorDetails);
            }
            notification.getWidget().removeStyleName("growl-dialog-progress"); //$NON-NLS-1$
            notification.getWidget().addStyleName("growl-dialog-error"); //$NON-NLS-1$

            resizeNotification(notification.getIndex() + 1);
        }
    }

}
