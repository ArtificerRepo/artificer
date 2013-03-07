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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A table implementation that can have arbitrary widgets in the cells.
 *
 * @author eric.wittmann@redhat.com
 */
public class WidgetTable extends Panel {

    protected List<Widget> children = new ArrayList<Widget>();
    /** Map of widget to td element. */
    protected Map<Widget, Element> wrapperMap = new HashMap<Widget, Element>();
    protected int columnCount;
    protected Element thead;
    protected Element tbody;
    protected List<Element> rowElements = new ArrayList<Element>();
    /** Extra class information for the data column TD's. */
    protected Map<Integer, String> columnClasses = new HashMap<Integer, String>();

    /**
     * Constructor.
     */
    public WidgetTable() {
        setElement(Document.get().createTableElement());
        init();
    }

    /**
     * Init the thead and tbody.
     */
    protected void init() {
        thead = Document.get().createTHeadElement().cast();
        tbody = Document.get().createTBodyElement().cast();
        DOM.appendChild(getElement(), thead);
        DOM.appendChild(getElement(), tbody);
    }

    /**
     * Creates the thead and th elements, with the given labels.
     * @param labels
     */
    public void setColumnLabels(String ... labels) {
        // TODO implement this
        this.columnCount = labels.length;
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#add(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void add(Widget widget) {
        throw new RuntimeException("Method not supported.  Try another variant of add().");
    }

    /**
     * Adds a single widget to the table, at the right row and column.
     *
     * @param rowIndex which row to add to (0 based, excluding thead)
     * @param colIndex which column to add to (0 based)
     * @param widget the widget to add to the table
     */
    public void add(int rowIndex, int colIndex, Widget widget) {
        if (widget == null)
            throw new NullPointerException("Cannot add a null widget.");
        if (colIndex >= this.columnCount || colIndex < 0)
            throw new IllegalArgumentException("Requested column index is out of range.");
        Element tr = ensureRow(rowIndex);
        Element td = ensureCell(tr, colIndex);

        widget.removeFromParent();
        children.add(widget);
        wrapperMap.put(widget, td);
        DOM.appendChild(td, widget.getElement());
        adopt(widget);
    }

    /**
     * Ensures that a row at the given index exists.
     * @param rowIndex
     */
    private Element ensureRow(int rowIndex) {
        NodeList<Node> childNodes = this.tbody.getChildNodes();
        int numTRs = childNodes.getLength();
        if (rowIndex < numTRs) {
            return childNodes.getItem(rowIndex).cast();
        }
        Element tr = null;
        for (int r = numTRs; r <= rowIndex; r++) {
            tr = Document.get().createTRElement().cast();
            DOM.appendChild(this.tbody, tr);
            this.rowElements.add(tr);
        }
        return tr;
    }

    /**
     * Ensure that a td cell exists for the row at the given column
     * index.
     * @param tr the row
     * @param colIndex the column index (0 based)
     * @return the new or already existing td
     */
    private Element ensureCell(Element tr, int colIndex) {
        NodeList<Node> tds = tr.getChildNodes();
        int numTDs = tds.getLength();
        if (colIndex < numTDs) {
            return tds.getItem(colIndex).cast();
        }
        Element td = null;
        for (int c = numTDs; c <= colIndex; c++) {
            td = Document.get().createTDElement().cast();
            if (this.columnClasses.containsKey(colIndex)) {
                td.setAttribute("class", this.columnClasses.get(colIndex));
            }
            DOM.appendChild(tr, td);
        }
        return td;
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#clear()
     */
    @Override
    public void clear() {
        List<Widget> childrenClone = new ArrayList<Widget>(this.children);
        for (Widget widget : childrenClone) {
            this.remove(widget);
        }
        for (Element rowElem : this.rowElements) {
            this.tbody.removeChild(rowElem);
        }
        this.rowElements.clear();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
     */
    @Override
    public Iterator<Widget> iterator() {
        return this.children.iterator();
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#remove(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public boolean remove(Widget w) {
        if (!this.children.contains(w))
            return false;
        orphan(w);
        Element tdWrapper = this.wrapperMap.get(w);
        tdWrapper.getParentElement().removeChild(tdWrapper);
        this.children.remove(w);
        this.wrapperMap.remove(w);
        return true;
    }

    /**
     * @return the number of rows (not including the thead) in the table
     */
    public int getRowCount() {
        return this.rowElements.size();
    }

    /**
     * Add extra class information that will be added to the TD for all data
     * rows (at the given column index).
     * @param columnIndex 0 based column index
     * @param classes additional html class information
     */
    public void setColumnClasses(int columnIndex, String classes) {
        this.columnClasses.put(columnIndex, classes);
    }
}
