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

import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;

/**
 * Widgets that want to support mouse-in/mouse-out must implement
 * this interface.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IMouseInOutWidget {

    /**
     * @return the widget's element
     */
    public Element getElement();

    /**
     * Adds an attach handler to the widget.
     * @param handler
     */
    public HandlerRegistration addAttachHandler(Handler handler);

    /**
     * Called when the mouse enters the widget.
     */
    public void onMouseIn();

    /**
     * Called when the mouse leaves the widget.
     */
    public void onMouseOut();

}