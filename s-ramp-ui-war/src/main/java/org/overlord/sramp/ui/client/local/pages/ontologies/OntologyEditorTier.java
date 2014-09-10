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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.overlord.commons.gwt.client.local.widgets.UnorderedListPanel;
import org.overlord.sramp.ui.client.local.ClientMessages;
import org.overlord.sramp.ui.client.shared.beans.OntologyClassBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;

/**
 * A single tier in the ontology editor.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class OntologyEditorTier extends FlowPanel implements HasValue<List<OntologyClassBean>>, HasSelectionHandlers<OntologyClassBean> {
    
    List<OntologyClassBean> value = null;
    OntologyEditorTierItem selected = null;
    private OntologyValidator validator;

    @Inject
    ClientMessages i18n;
    
    @Inject
    Instance<AddOntologyNodeDialog> addOntologyNodeDialogFactory;
    @Inject
    Instance<OntologyEditorTierItem> itemFactory;
    
    @Inject
    FlowPanel header;
    @Inject
    Button addNode;
    @Inject
    UnorderedListPanel items;

    /**
     * Constructor.
     */
    public OntologyEditorTier() {
    }
    
    /**
     * Called after the widget is created.
     */
    @PostConstruct
    protected void postConstruct() {
        setStyleName("sramp-ontology-tier"); //$NON-NLS-1$
        header.getElement().setClassName("sramp-ontology-tier-header"); //$NON-NLS-1$
        addNode.setText("+");
        addNode.addStyleName("btn-mini"); //$NON-NLS-1$
        addNode.addStyleName("btn"); //$NON-NLS-1$
        addNode.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onAddItem();
            }
        });
        header.add(addNode);
        items.setStyleName("nav"); //$NON-NLS-1$
        items.addStyleName("nav-pills"); //$NON-NLS-1$
        items.addStyleName("nav-stacked"); //$NON-NLS-1$
        
        add(header);
        add(items);
    }

    /**
     * Called when the user clicks the 'Add Node' button.
     */
    protected void onAddItem() {
        AddOntologyNodeDialog dialog = addOntologyNodeDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<OntologyClassBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<OntologyClassBean> event) {
                OntologyClassBean bean = event.getValue();
                if (validator.canAddClass(bean)) {
                    value.add(bean);
                    OntologyEditorTierItem item = createItem(bean);
                    items.add(item);
                    ValueChangeEvent.fire(OntologyEditorTier.this, OntologyEditorTier.this.value);
                }
            }
        });
        dialog.show();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<OntologyClassBean>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<OntologyClassBean> getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<OntologyClassBean> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<OntologyClassBean> value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
        items.clear();
        for (OntologyClassBean ontologyClass : value) {
            OntologyEditorTierItem item = createItem(ontologyClass);
            items.add(item);
        }
    }

    /**
     * Creates an item from the given ontology class bean.
     * @param ontologyClass
     */
    private OntologyEditorTierItem createItem(OntologyClassBean ontologyClass) {
        final OntologyEditorTierItem item = itemFactory.get();
        item.addValueChangeHandler(new ValueChangeHandler<OntologyClassBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<OntologyClassBean> event) {
                // If the event value is null, that means we should remove the item.
                if (event.getValue() == null) {
                    removeItem(item);
                }
                ValueChangeEvent.fire(OntologyEditorTier.this, OntologyEditorTier.this.value);
            }
        });
        item.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectTierItem(item);
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
    protected void removeItem(OntologyEditorTierItem item) {
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
    protected void selectTierItem(OntologyEditorTierItem item) {
        OntologyEditorTierItem oldSelection = selected;
        OntologyEditorTierItem newSelection = item;
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
            OntologyClassBean bean = selected.getValue();
            SelectionEvent.fire(this, bean);
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasSelectionHandlers#addSelectionHandler(com.google.gwt.event.logical.shared.SelectionHandler)
     */
    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<OntologyClassBean> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }

    /**
     * @param validator the validator to set
     */
    public void setValidator(OntologyValidator validator) {
        this.validator = validator;
    }

}
