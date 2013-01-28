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
package org.overlord.sramp.ui.client.local.pages.artifacts;

import org.overlord.sramp.ui.client.local.widgets.common.TemplatedWidgetTable;
import org.overlord.sramp.ui.client.shared.beans.ArtifactSummaryBean;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A table of artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactsTable extends TemplatedWidgetTable {

    /**
     * Constructor.
     */
    public ArtifactsTable() {
    }

    /**
     * Adds a single row to the table.
     * @param artifactSummaryBean
     */
    public void addRow(ArtifactSummaryBean artifactSummaryBean) {
        int rowIdx = this.rowElements.size();
        DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy");

        InlineLabel name = new InlineLabel(artifactSummaryBean.getName());
        InlineLabel type = new InlineLabel(artifactSummaryBean.getType());
        InlineLabel derived = new InlineLabel(artifactSummaryBean.isDerived() ? "true" : "");
        InlineLabel modified = new InlineLabel(format.format(artifactSummaryBean.getUpdatedOn()));
        InlineLabel fav = new InlineLabel("");
        InlineLabel actions = new InlineLabel("");

        add(rowIdx, 0, name);
        add(rowIdx, 1, type);
        add(rowIdx, 2, derived);
        add(rowIdx, 3, modified);
        add(rowIdx, 4, fav);
        add(rowIdx, 5, actions);
    }

}
