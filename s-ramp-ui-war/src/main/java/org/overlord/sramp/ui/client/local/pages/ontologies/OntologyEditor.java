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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.overlord.sramp.ui.client.local.ClientMessages;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.services.OntologyServiceCaller;
import org.overlord.sramp.ui.client.local.services.callback.IServiceInvocationHandler;
import org.overlord.sramp.ui.client.shared.beans.NotificationBean;
import org.overlord.sramp.ui.client.shared.beans.OntologyBean;
import org.overlord.sramp.ui.client.shared.beans.OntologyClassBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A widget that allows editing of an S-RAMP ontology.
 * 
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class OntologyEditor extends FlowPanel implements HasValue<OntologyBean> {
    
    private OntologyBean reversionOntology;
    private OntologyBean currentOntology;
    private boolean dirty;
    
    // Services
    @Inject
    NotificationService notificationService;
    @Inject
    ClientMessages i18n;
    @Inject
    OntologyServiceCaller ontologyService;
    
    @Inject 
    OntologyValidator validator;
    @Inject
    Instance<OntologyEditorTier> tierFactory;

    @Inject
    InlineLabel pleaseSelect;
    @Inject
    FlowPanel canvas;
    @Inject
    FlowPanel actions;
    @Inject
    Button saveButton;
    @Inject
    Button revertButton;

    private ArrayList<OntologyEditorTier> tiers = new ArrayList<OntologyEditorTier>();

    /**
     * Constructor.
     */
    public OntologyEditor() {
    }
    
    /**
     * Called after the widget is created.
     */
    @PostConstruct
    protected void postConstruct() {
        saveButton.setText(i18n.format("ontology-editor.save")); //$NON-NLS-1$
        saveButton.setStyleName("btn"); //$NON-NLS-1$
        revertButton.setText(i18n.format("ontology-editor.revert")); //$NON-NLS-1$
        revertButton.setStyleName("btn"); //$NON-NLS-1$
        pleaseSelect.setText(i18n.format("ontology-editor.please-select")); //$NON-NLS-1$
        getElement().setId("ontology-editor"); //$NON-NLS-1$
        getElement().setClassName("span8"); //$NON-NLS-1$
        canvas.getElement().setClassName("sramp-ontology-editor-canvas"); //$NON-NLS-1$
        actions.getElement().setClassName("sramp-ontology-actions"); //$NON-NLS-1$
        actions.add(saveButton);
        actions.add(revertButton);
        add(canvas);
        add(actions);
        add(pleaseSelect);
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onSave(event);
            }
        });
        revertButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onRevert(event);
            }
        });
        clear();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<OntologyBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public OntologyBean getValue() {
        return currentOntology;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(OntologyBean value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(OntologyBean value, boolean fireEvents) {
        currentOntology = value;
        if (value != null) {
            reversionOntology = value.copy();
            validator.setOntology(value);
        } else {
            reversionOntology = null;
        }
        setDirty(false);
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }

        canvas.clear();
        tiers.clear();
        if (value != null) {
            pleaseSelect.setVisible(false);
            canvas.setVisible(true);
            actions.setVisible(true);

            final OntologyEditorTier tier = createTier();
            tier.setValue(value.getRootClasses());
            canvas.add(tier);
            tiers.add(tier);
        } else {
            pleaseSelect.setVisible(true);
            canvas.setVisible(false);
            actions.setVisible(false);
        }
    }

    /**
     * Creates a new tier.
     */
    private OntologyEditorTier createTier() {
        final OntologyEditorTier tier = tierFactory.get();
        tier.setValidator(validator);
        tier.addValueChangeHandler(new ValueChangeHandler<List<OntologyClassBean>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<OntologyClassBean>> event) {
                setDirty(true);
            }
        });
        tier.addSelectionHandler(new SelectionHandler<OntologyClassBean>() {
            @Override
            public void onSelection(SelectionEvent<OntologyClassBean> event) {
                onTierItemSelected(tier, event.getSelectedItem());
            }
        });
        return tier;
    }

    /**
     * Called when the user selects an item in one of the visible tiers.
     * @param tier
     * @param selectedItem
     */
    protected void onTierItemSelected(OntologyEditorTier tier, OntologyClassBean selectedItem) {
        // First, remove all tiers beyond the one affected
        if (tiers.size() > 1) {
            for (int i = tiers.size() - 1; i >= 0; i--) {
                OntologyEditorTier t = tiers.remove(i);
                if (tier == t) {
                    tiers.add(tier);
                    break;
                }
                canvas.remove(t);
            }
        }
        
        // Next, create a new tier for the children of the selected item.
        if (selectedItem != null) {
            List<OntologyClassBean> children = selectedItem.getChildren();
            OntologyEditorTier newTier = createTier();
            newTier.setValue(children);
            tiers.add(newTier);
            canvas.add(newTier);
            newTier.getElement().scrollIntoView();
        }
    }

    /**
     * Returns true if the editor is dirty.
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * Sets the editor's dirty flag.
     * @param dirty
     */
    private void setDirty(boolean dirty) {
        this.dirty = dirty;
        saveButton.setEnabled(dirty);
        revertButton.setEnabled(dirty);
    }

    /**
     * Clears the current ontology from the editor.
     */
    public void clear() {
        currentOntology = null;
        reversionOntology = null;
        setDirty(false);
        canvas.clear();
        pleaseSelect.setVisible(true);
        canvas.setVisible(false);
        actions.setVisible(false);
    }
    
    /**
     * Called when the user clicks the Save button.
     * @param event
     */
    public void onSave(ClickEvent event) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("ontology-editor.saving.title"), //$NON-NLS-1$
                i18n.format("ontology-editor.saving.message")); //$NON-NLS-1$
        ontologyService.update(getValue(), new IServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(), 
                        i18n.format("ontology-editor.saved.title"),  //$NON-NLS-1$
                        i18n.format("ontology-editor.saved.message", getValue().getId())); //$NON-NLS-1$
                setDirty(false);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ontology-editor.saved-error.title"), error); //$NON-NLS-1$
            }
        });
    }

    /**
     * Called when the user clicks the Revert button.
     * @param event
     */
    public void onRevert(ClickEvent event) {
        setValue(reversionOntology);
    }
    
}
