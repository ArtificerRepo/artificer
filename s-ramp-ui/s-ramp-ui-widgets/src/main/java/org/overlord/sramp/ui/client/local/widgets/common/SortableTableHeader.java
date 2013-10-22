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

import com.google.gwt.user.client.ui.Anchor;

/**
 * Widget that is used as the child/content of the "th" for a sortable widget table.
 * @author eric.wittmann@redhat.com
 */
public class SortableTableHeader extends Anchor {

    private static final String CHEVRON_ASCENDING = "&#x22C0;"; //$NON-NLS-1$
    private static final String CHEVRON_DESCENDING = "&#x22C1;"; //$NON-NLS-1$

    private boolean active = false;
    private boolean ascending = true;
    private String label;
    private String columnId;

    /**
     * Constructor.
     * @param columnId
     */
    public SortableTableHeader(String label, String columnId) {
        this.setLabel(label);
        this.setColumnId(columnId);
        this.refreshHtml();
    }

    /**
     * Updates the HTML content of the widget.
     */
    public void refreshHtml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<span class=\"sortable-column-header\">"); //$NON-NLS-1$
        builder.append(getLabel());
        builder.append("</span>"); //$NON-NLS-1$
        if (this.isActive()) {
            builder.append(" "); //$NON-NLS-1$
            builder.append("<span class=\"sortable-column-icon pull-right\">"); //$NON-NLS-1$
            builder.append(this.isAscending() ? CHEVRON_ASCENDING : CHEVRON_DESCENDING);
            builder.append("</span>"); //$NON-NLS-1$
        }
        this.setHTML(builder.toString());
    }

    /**
     * @return the columnId
     */
    public String getColumnId() {
        return columnId;
    }

    /**
     * @param columnId the columnId to set
     */
    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * @param ascending the ascending to set
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

}
