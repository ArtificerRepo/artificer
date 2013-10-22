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

import java.util.HashMap;
import java.util.Map;

import org.overlord.sramp.ui.client.local.events.TableSortEvent;
import org.overlord.sramp.ui.client.local.events.TableSortEvent.Handler;
import org.overlord.sramp.ui.client.local.events.TableSortEvent.HasTableSortHandlers;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Extends the templated widget table to add support for column sorting.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class SortableTemplatedWidgetTable extends TemplatedWidgetTable implements HasTableSortHandlers {

    private String currentSortColumnId;
    private Map<String, SortableTableHeader> columnIdMap = new HashMap<String, SortableTableHeader>();
    private SortColumn deferredSort;

    /**
     * Constructor.
     */
    public SortableTemplatedWidgetTable() {
    }

    /**
     * @see org.overlord.sramp.ui.client.local.widgets.common.TemplatedWidgetTable#doAttachInit()
     */
    @Override
    protected void doAttachInit() {
        super.doAttachInit();
        configureColumnSorting();
        if (deferredSort != null) {
            sortBy(deferredSort.columnId, deferredSort.ascending);
            deferredSort = null;
        }
    }

    /**
     * Subclasses have an opportunity here to configure which columns can be used to sort.
     */
    protected void configureColumnSorting() {
    }

    /**
     * @see org.overlord.sramp.ui.client.local.events.TableSortEvent.HasTableSortHandlers#addTableSortHandler(org.overlord.sramp.ui.client.local.events.TableSortEvent.Handler)
     */
    @Override
    public HandlerRegistration addTableSortHandler(Handler handler) {
        return addHandler(handler, TableSortEvent.getType());
    }

    /**
     * Sets a column in the table to be sortable.  This will convert the content in the
     * "th" to be something the user can click on.  When the user clicks on it, the
     * internal state of the table will be altered *and* an event will be fired.
     * @param columnIndex
     * @param columnId
     */
    public void setColumnSortable(int columnIndex, final String columnId) {
        Element thElement = null;
        NodeList<com.google.gwt.dom.client.Element> elementsByTagName = this.thead.getElementsByTagName("th"); //$NON-NLS-1$
        if (columnIndex <= elementsByTagName.getLength()) {
            thElement = elementsByTagName.getItem(columnIndex).cast();
        }
        if (thElement == null) {
            return;
        }

        String columnLabel = thElement.getInnerText();
        thElement.setInnerText(""); //$NON-NLS-1$

        SortableTableHeader widget = new SortableTableHeader(columnLabel, columnId);
        widget.removeFromParent();
        DOM.appendChild(thElement, widget.getElement());
        adopt(widget);
        widget.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onColumnHeaderClick(columnId);
            }
        });
        columnIdMap.put(columnId, widget);
    }

    /**
     * Sets the current sort-by column.  Does not trigger an event.
     * @param columnId
     * @param ascending
     */
    public void sortBy(String columnId, boolean ascending) {
        sortBy(columnId, ascending, false);
    }

    /**
     * Sets the current sort-by column.
     * @param columnId
     * @param ascending
     * @param fireEvent
     */
    public void sortBy(String columnId, boolean ascending, boolean fireEvent) {
        if (!this.isAttached()) {
            deferredSort = new SortColumn();
            deferredSort.columnId = columnId;
            deferredSort.ascending = ascending;
        } else {
            SortableTableHeader newActiveHeader = columnIdMap.get(columnId);
            if (columnIdMap.containsKey(currentSortColumnId)) {
                SortableTableHeader oldActiveHeader = columnIdMap.get(currentSortColumnId);
                oldActiveHeader.setActive(false);
                oldActiveHeader.refreshHtml();
            }
            newActiveHeader.setAscending(ascending);
            newActiveHeader.setActive(true);
            newActiveHeader.refreshHtml();

            currentSortColumnId = columnId;
            if (fireEvent)
                TableSortEvent.fire(this, columnId, ascending);
        }
    }

    /**
     * Called when the user clicks on one of the column headers.
     * @param columnId
     */
    protected void onColumnHeaderClick(String columnId) {
        boolean isAscending = true;
        SortableTableHeader newActiveHeader = columnIdMap.get(columnId);
        if (newActiveHeader.isActive()) {
            isAscending = !newActiveHeader.isAscending();
        }
        sortBy(columnId, isAscending, true);
    }

    /**
     * @return the current sort column
     */
    public SortColumn getCurrentSortColumn() {
        if (deferredSort != null) {
            return deferredSort;
        }
        SortableTableHeader currentSortHeader = this.columnIdMap.get(currentSortColumnId);
        if (currentSortHeader == null) {
            return getDefaultSortColumn();
        }
        SortColumn rval = new SortColumn();
        rval.columnId = currentSortHeader.getColumnId();
        rval.ascending = currentSortHeader.isAscending();
        return rval;
    }

    /**
     * @return the default sort column
     */
    public abstract SortColumn getDefaultSortColumn();

    /**
     * @author eric.wittmann@redhat.com
     */
    public static class SortColumn {
        public String columnId;
        public boolean ascending;
    }

}
