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

import org.artificer.ui.client.shared.beans.NotificationBean;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.Timer;

/**
 *
 * @author eric.wittmann@redhat.com
 */
public class Notification {

    private int index;
    private NotificationBean data;
    private NotificationWidget widget;
    private Timer aliveTimer;
    private Timer fadeTimer;
    private Animation autoCloseAnimation;

    /**
     * Constructor.
     * @param data
     */
    public Notification(NotificationBean data) {
        setData(data);
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the data
     */
    public NotificationBean getData() {
        return data;
    }

    /**
     * @return the widget
     */
    public NotificationWidget getWidget() {
        return widget;
    }

    /**
     * @return the aliveTimer
     */
    public Timer getAliveTimer() {
        return aliveTimer;
    }

    /**
     * @return the fadeTimer
     */
    public Timer getFadeTimer() {
        return fadeTimer;
    }

    /**
     * @return the autoCloseAnimation
     */
    public Animation getAutoCloseAnimation() {
        return autoCloseAnimation;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @param data the data to set
     */
    public void setData(NotificationBean data) {
        this.data = data;
    }

    /**
     * @param widget the widget to set
     */
    public void setWidget(NotificationWidget widget) {
        this.widget = widget;
    }

    /**
     * @param aliveTimer the aliveTimer to set
     */
    public void setAliveTimer(Timer aliveTimer) {
        this.aliveTimer = aliveTimer;
    }

    /**
     * @param fadeTimer the fadeTimer to set
     */
    public void setFadeTimer(Timer fadeTimer) {
        this.fadeTimer = fadeTimer;
    }

    /**
     * @param autoCloseAnimation the autoCloseAnimation to set
     */
    public void setAutoCloseAnimation(Animation autoCloseAnimation) {
        this.autoCloseAnimation = autoCloseAnimation;
    }

}
