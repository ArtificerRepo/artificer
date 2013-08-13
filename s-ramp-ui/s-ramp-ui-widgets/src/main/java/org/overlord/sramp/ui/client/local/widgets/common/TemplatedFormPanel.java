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
package org.overlord.sramp.ui.client.local.widgets.common;

import javax.inject.Inject;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasText;

/**
 * Extends the {@link FormPanel} and implements {@link HasText} as a marker interface
 * so that this class can be {@link Inject}'d into an Errai UI template properly.
 *
 * @author eric.wittmann@redhat.com
 */
public class TemplatedFormPanel extends FormPanel implements HasText {

    /**
     * Constructor.
     */
    public TemplatedFormPanel() {
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#getText()
     */
    @Override
    public String getText() {
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
    }

}
