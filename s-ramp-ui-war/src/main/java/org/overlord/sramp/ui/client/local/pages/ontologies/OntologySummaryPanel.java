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

package org.overlord.sramp.ui.client.local.pages.ontologies;

import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.overlord.commons.gwt.client.local.widgets.UnorderedListPanel;
import org.overlord.sramp.ui.client.local.ClientMessages;
import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Ontology summaries panel.
 *
 * @author Brett Meyer
 */
@Dependent
public class OntologySummaryPanel extends FlowPanel implements HasValue<List<OntologySummaryBean>>, HasSelectionHandlers<OntologySummaryBean> {
    
    List<OntologySummaryBean> value = null;
    OntologySummaryPanelItem selected = null;

    @Inject
    ClientMessages i18n;
    
    @Inject
    Instance<OntologySummaryPanelItem> itemFactory;
    
    @Inject
    FlowPanel header;
    @Inject
    UnorderedListPanel items;

    /**
     * Constructor.
     */
    public OntologySummaryPanel() {
    }
    
    /**
     * Called after the widget is created.
     */
    @PostConstruct
    protected void postConstruct() {
        setStyleName("ontologies-canvas"); //$NON-NLS-1$
        header.getElement().setClassName("sramp-ontology-box-header"); //$NON-NLS-1$
        header.getElement().setInnerText(i18n.format("ontologies.ontologies"));
        items.setStyleName("nav"); //$NON-NLS-1$
        items.addStyleName("nav-pills"); //$NON-NLS-1$
        items.addStyleName("nav-stacked"); //$NON-NLS-1$
        
        add(header);
        add(items);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<OntologySummaryBean>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<OntologySummaryBean> getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<OntologySummaryBean> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<OntologySummaryBean> value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
        items.clear();
        for (OntologySummaryBean ontologyClass : value) {
            OntologySummaryPanelItem item = createItem(ontologyClass);
            items.add(item);
        }
    }

    /**
     * Creates an item from the given ontology class bean.
     * @param ontologyClass
     */
    private OntologySummaryPanelItem createItem(OntologySummaryBean ontologyClass) {
        final OntologySummaryPanelItem item = itemFactory.get();
        item.addValueChangeHandler(new ValueChangeHandler<OntologySummaryBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<OntologySummaryBean> event) {
                // If the event value is null, that means we should remove the item.
                if (event.getValue() == null) {
                    removeItem(item);
                }
                ValueChangeEvent.fire(OntologySummaryPanel.this, OntologySummaryPanel.this.value);
            }
        });
        item.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectItem(item);
            }
        });
        item.setValue(ontologyClass);
        return item;
    }

    /**
     * Removes the ontology item/class.  This removes it both from the value {@link List} 
     * as well as from the UI. 
     * @param item
     */
    protected void removeItem(OntologySummaryPanelItem item) {
        // When deleting the currently selected item, make sure to fire a
        // "deselected" event so that any forward tiers are removed from the UI.
        if (item == selected) {
            selected = null;
            SelectionEvent.fire(this, null);
        }
        items.remove(item);
        value.remove(item.getValue());
    }

    /**
     * Selects a specific item in the tier.
     * @param item
     */
    protected void selectItem(OntologySummaryPanelItem item) {
        OntologySummaryPanelItem oldSelection = selected;
        OntologySummaryPanelItem newSelection = item;
        if (oldSelection != null) {
            oldSelection.getElement().removeClassName("active"); //$NON-NLS-1$
            oldSelection.hideActions();
        }
        if (oldSelection == newSelection) {
            selected = null;
            SelectionEvent.fire(this, null);
        } else {
            newSelection.getElement().addClassName("active"); //$NON-NLS-1$
            selected = newSelection;
            OntologySummaryBean bean = selected.getValue();
            SelectionEvent.fire(this, bean);
        }
    }

    /**
     * Restores a selected item by UUID.  Useful when OntologiesPage needs to completely replace the ontologies
     * after a new one has been uploaded.
     * 
     * @param uuid
     */
    public void restoreSelectedItem(String uuid) {
        Iterator<Widget> itr = items.iterator();
        while (itr.hasNext()) {
            OntologySummaryPanelItem item = (OntologySummaryPanelItem) itr.next();
            if (item.getValue().getUuid() != null && item.getValue().getUuid().equals(uuid)) {
                item.getElement().addClassName("active"); //$NON-NLS-1$
                selected = item;
                selected.showActions();
                // no need to re-fire the event
            }
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasSelectionHandlers#addSelectionHandler(com.google.gwt.event.logical.shared.SelectionHandler)
     */
    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<OntologySummaryBean> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }
    
    public OntologySummaryBean getSelectedOntology() {
        return selected == null ? null : selected.getValue();
    }

}
