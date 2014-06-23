/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.ui.client.local.pages.ontologies;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.Dependent;

import org.overlord.commons.gwt.client.local.widgets.TemplatedWidgetTable;
import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasValue;

/**
 * The "Ontologies table" witget. It allows to paint the summary ontology table.
 *
 * @author David Virgil Naranjo
 */
@Dependent
public class OntologiesTable extends TemplatedWidgetTable implements
        HasValue<TreeMap<String, OntologySummaryBean>>, HasSelectionHandlers<OntologySummaryBean> {


    private TreeMap<String, OntologySummaryBean> value;

    OntologySummaryBean selectedOntology = null;

    private final Map<OntologySummaryBean, Integer> rows;

    /**
     * Instantiates a new ontologies table.
     */
    public OntologiesTable() {
        super.setColumnClasses(0, ""); //$NON-NLS-1$
        super.setColumnClasses(1, "icon"); //$NON-NLS-1$
        super.columnCount = 2;
        value = new TreeMap<String, OntologySummaryBean>();
        rows = new HashMap<OntologySummaryBean, Integer>();
    }


    /**
     * Adds a single row to the table.
     *
     * @param ontologyBean
     *            the ontology bean
     */
    public void addRow(final OntologySummaryBean ontologyBean) {
        int rowIdx = this.rowElements.size();


        final Anchor a = new Anchor(OntologiesUtil.createOntologyLabel(ontologyBean));
        a.getElement().setAttribute("data-uuid", ontologyBean.getUuid()); //$NON-NLS-1$
        a.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SelectionEvent.fire(OntologiesTable.this, ontologyBean);
            }
        });
        Anchor downloadAnchor = new Anchor();
        // downloadAnchor.setStyleName("download-link");
        String contentUrl = GWT.getModuleBaseURL()
                + "services/ontologyDownload?uuid=" + ontologyBean.getUuid(); //$NON-NLS-1$
        downloadAnchor.setHref(contentUrl);
        downloadAnchor.setHTML("<div class=\"download-icon\"></div>"); //$NON-NLS-1$

        add(rowIdx, 0, a);
        add(rowIdx, 1, downloadAnchor);
        rows.put(ontologyBean, rowIdx);

    }




    /*
     * (non-Javadoc)
     *
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#
     * addValueChangeHandler
     * (com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<TreeMap<String, OntologySummaryBean>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.gwt.event.logical.shared.HasSelectionHandlers#addSelectionHandler
     * (com.google.gwt.event.logical.shared.SelectionHandler)
     */
    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<OntologySummaryBean> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public TreeMap<String, OntologySummaryBean> getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(TreeMap<String, OntologySummaryBean> value) {
        setValue(value, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object,
     * boolean)
     */
    @Override
    public void setValue(TreeMap<String, OntologySummaryBean> value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
        this.clear();
        for (OntologySummaryBean ontologyClass : value.values()) {
            this.addRow(ontologyClass);
        }
    }

    /**
     * Select item.
     *
     * @param ontology
     *            the ontology
     */
    public void selectItem(OntologySummaryBean ontology) {
        Element row = this.getRow(rows.get(ontology));
        row.addClassName("active"); //$NON-NLS-1$
        this.selectedOntology = ontology;
    }

    /**
     * Select item.
     *
     * @param ontology
     *            the ontology
     */
    public void selectItem(String uuid) {
        for (OntologySummaryBean ontology : rows.keySet()) {
            if (ontology.getUuid() != null && !ontology.getUuid().equals("") //$NON-NLS-1$
                    && ontology.getUuid().equals(uuid)) {
                SelectionEvent.fire(OntologiesTable.this, ontology);
            }
        }

    }

    /**
     * Unselect item.
     */
    public void unselectItem() {
        Element row = this.getRow(rows.get(selectedOntology));
        row.removeClassName("active"); //$NON-NLS-1$
        this.selectedOntology = null;
    }

    /**
     * Gets the selected ontology.
     *
     * @return the selected ontology
     */
    public OntologySummaryBean getSelectedOntology() {
        return selectedOntology;
    }

    /**
     * Sets the selected ontology.
     *
     * @param selectedOntology
     *            the new selected ontology
     */
    public void setSelectedOntology(OntologySummaryBean selectedOntology) {
        this.selectedOntology = selectedOntology;
    }

}
