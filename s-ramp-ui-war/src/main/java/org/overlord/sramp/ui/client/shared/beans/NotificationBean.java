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
package org.overlord.sramp.ui.client.shared.beans;

import java.io.Serializable;
import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;

/**
 * A notification - can be sent either from the client or the server.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class NotificationBean implements Serializable {

    private static final long serialVersionUID = NotificationBean.class.hashCode();

    private String uuid;
    private NotificationType type;
    private final Date date = new Date();
    private String title;
    private String message;
    private transient Object messageWidget;
    private SrampUiException exception;

    /**
     * Constructor.
     */
    public NotificationBean() {
    }

    /**
     * @return the type
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param type the type to set
     */
    public void setType(NotificationType type) {
        this.type = type;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the exception
     */
    public SrampUiException getException() {
        return exception;
    }

    /**
     * @param exception the exception to set
     */
    public void setException(SrampUiException exception) {
        this.exception = exception;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the message widget
     */
    public Object getMessageWidget() {
        return messageWidget;
    }

    /**
     * @param widget the message widget to set
     */
    public void setMessageWidget(Object widget) {
        this.messageWidget = widget;
    }

}
