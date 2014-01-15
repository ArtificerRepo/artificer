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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.ClientMessages;
import org.overlord.sramp.ui.client.local.widgets.common.DropdownMenu;
import org.overlord.sramp.ui.client.shared.beans.OntologyClassBean;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A single item in an ontology tier.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/ontologies.html#sramp-ontology-tier-item")
@Dependent
public class OntologyEditorTierItem extends Composite implements HasValue<OntologyClassBean>, HasClickHandlers {
    
    private OntologyClassBean value;
    
    @Inject
    ClientMessages i18n;
    
    @Inject @DataField
    InlineLabel label;
    @Inject @DataField
    Button actions;
    @Inject
    Instance<DropdownMenu> menuFactory;
    @Inject
    Instance<EditOntologyNodeDialog> dialogFactory;

    /**
     * Constructor.
     */
    public OntologyEditorTierItem() {
    }
    
    /**
     * Called after consrtuction.
     */
    @PostConstruct
    protected void postConstruct() {
        label.setText(""); //$NON-NLS-1$
        sinkEvents(Event.ONCLICK);
    }
    
    /**
     * @see com.google.gwt.user.client.ui.Composite#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONCLICK) {
            EventTarget target = event.getEventTarget();
            Element elem = (Element) target.cast();
            if (elem.getNodeName().toLowerCase().equals("button")) { //$NON-NLS-1$
                onActionButton();
                event.preventDefault();
            } else {
                super.onBrowserEvent(event);
            }
        }
    }

    /**
     * Called when the user clicks on the action button.
     */
    private void onActionButton() {
        DropdownMenu menu = menuFactory.get();
        Anchor editAction = menu.addMenuAction(i18n.format("ontology-editor.edit")); //$NON-NLS-1$
        editAction.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onEdit();
            }
        });
        Anchor removeAction = menu.addMenuAction(i18n.format("ontology-editor.remove")); //$NON-NLS-1$
        removeAction.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onRemove();
            }
        });
        menu.showRelativeTo(actions);
    }

    /**
     * Called when the user clicks the Remove action.
     */
    protected void onRemove() {
        ValueChangeEvent.fire(this, null);
    }

    /**
     * Called when the user clicks the Edit action.
     */
    protected void onEdit() {
        EditOntologyNodeDialog dialog = dialogFactory.get();
        dialog.init(getValue());
        dialog.addValueChangeHandler(new ValueChangeHandler<OntologyClassBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<OntologyClassBean> event) {
                OntologyClassBean updatedValue = event.getValue();
                OntologyClassBean currentValue = getValue();
                currentValue.setLabel(updatedValue.getLabel());
                currentValue.setComment(updatedValue.getComment());
                label.setText(createLabel(currentValue));
                ValueChangeEvent.fire(OntologyEditorTierItem.this, currentValue);
            }
        });
        dialog.show();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<OntologyClassBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public OntologyClassBean getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(OntologyClassBean value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(OntologyClassBean value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
        this.label.setText(createLabel(value));
    }

    /**
     * Creates the label for the item.
     * @param value
     */
    private static String createLabel(OntologyClassBean value) {
        String id = value.getId();
        String label = value.getLabel();
        String rval = id;
        if (label != null && label.trim().length() > 0 && !label.equals(id)) {
            rval += " (" + label + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return rval;
    }

}
