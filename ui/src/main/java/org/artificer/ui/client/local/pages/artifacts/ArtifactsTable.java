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
package org.artificer.ui.client.local.pages.artifacts;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import org.artificer.ui.client.local.pages.ArtifactDetailsPage;
import org.artificer.ui.client.shared.beans.ArtifactSummaryBean;
import org.artificer.ui.client.shared.beans.Constants;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.SortableTemplatedWidgetTable;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * A table of artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ArtifactsTable extends SortableTemplatedWidgetTable {

    @Inject
    protected TransitionAnchorFactory<ArtifactDetailsPage> toDetailsPageLinkFactory;

    /**
     * Constructor.
     */
    public ArtifactsTable() {
    }

    @Override
    public SortColumn getDefaultSortColumn() {
        SortColumn sortColumn = new SortColumn();
        sortColumn.columnId = Constants.SORT_COLID_NAME;
        sortColumn.ascending = true;
        return sortColumn;
    }

    @Override
    protected void configureColumnSorting() {
        setColumnSortable(0, Constants.SORT_COLID_NAME);
        setColumnSortable(3, Constants.SORT_COLID_MODIFIED_ON);
        sortBy(Constants.SORT_COLID_NAME, true);
    }

    public void addRow(final ArtifactSummaryBean artifactSummaryBean) {
        addRow(artifactSummaryBean, new InlineLabel(""));
    }

    public void addRow(final ArtifactSummaryBean artifactSummaryBean, Widget extraColumnWidget) {
        int rowIdx = this.rowElements.size();
        DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy"); //$NON-NLS-1$

        Anchor name = toDetailsPageLinkFactory.get("uuid", artifactSummaryBean.getUuid()); //$NON-NLS-1$
        name.setText(artifactSummaryBean.getName());
        InlineLabel type = new InlineLabel(artifactSummaryBean.getType());
        InlineLabel derived = new InlineLabel(artifactSummaryBean.isDerived() ? "true" : ""); //$NON-NLS-1$ //$NON-NLS-2$
        InlineLabel modified = new InlineLabel(format.format(artifactSummaryBean.getUpdatedOn()));

        add(rowIdx, 0, name);
        add(rowIdx, 1, type);
        add(rowIdx, 2, derived);
        add(rowIdx, 3, modified);
        add(rowIdx, 4, extraColumnWidget);
    }

}
