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
package org.artificer.ui.client.local.pages.details;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.local.widgets.common.EditableInlineLabel;

/**
 * Widget used to display the artifact description.
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class DescriptionInlineLabel extends EditableInlineLabel {

    @Inject
    protected ClientMessages i18n;

    /**
     * Constructor.
     */
    public DescriptionInlineLabel() {
        setSupportsRemove(true);
    }

    /**
     * @see com.google.gwt.user.client.ui.Label#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        if (text == null || text.trim().length() == 0) {
            this.setText(i18n.format("no-value")); //$NON-NLS-1$
        } else {
            if (text.contains("\n")) { //$NON-NLS-1$
                String html = text.replace("\n", "<br />"); //$NON-NLS-1$ //$NON-NLS-2$
                getElement().setInnerHTML(html);
            } else {
                super.setText(text);
            }
        }
    }
}
