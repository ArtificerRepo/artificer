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

package org.artificer.ui.client.local.pages.ontologies;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.local.services.NotificationService;
import org.artificer.ui.client.local.services.OntologyServiceCaller;
import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;
import org.artificer.ui.client.shared.beans.NotificationBean;
import org.artificer.ui.client.shared.beans.OntologySummaryBean;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A single item in the Ontology summary panel
 *
 * @author Brett meyer
 */
@Templated("/org/artificer/ui/client/local/site/ontologies.html#sramp-ontology-item")
@Dependent
public class OntologySummaryPanelItem extends Composite implements HasValue<OntologySummaryBean>, HasClickHandlers {
    
    private OntologySummaryBean value;
    
    @Inject
    ClientMessages i18n;
    @Inject
    OntologyServiceCaller ontologyService;
    @Inject
    NotificationService notificationService;
    
    @Inject @DataField
    InlineLabel label;
    @Inject @DataField
    FlowPanel actions;
    @Inject
    Button downloadButton;
    @Inject
    Button deleteButton;

    /**
     * Constructor.
     */
    public OntologySummaryPanelItem() {
    }
    
    /**
     * Called after consrtuction.
     */
    @PostConstruct
    protected void postConstruct() {
        label.setText(""); //$NON-NLS-1$
        sinkEvents(Event.ONCLICK);

        downloadButton.setText(i18n.format("ontology-editor.download"));
        downloadButton.addStyleName("btn-mini"); //$NON-NLS-1$
        downloadButton.addStyleName("btn"); //$NON-NLS-1$
        downloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDownload();
            }
        });
        deleteButton.setText(i18n.format("ontology-editor.delete"));
        deleteButton.addStyleName("btn-mini"); //$NON-NLS-1$
        deleteButton.addStyleName("btn"); //$NON-NLS-1$
        deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDelete();
            }
        });
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
                // an action button was clicked
                event.preventDefault();
            } else {
                showActions();
                super.onBrowserEvent(event);
            }
        }
    }

    /**
     * Called when the user clicks on this item
     */
    public void showActions() {
        actions.add(downloadButton);
        actions.add(deleteButton);
    }
    
    /**
     * Called when a different item is selected in the parent list.
     */
    public void hideActions() {
        actions.clear();
    }
    
    protected void onDownload() {
        String contentUrl = GWT.getModuleBaseURL() + "services/ontologyDownload?uuid=" + value.getUuid(); //$NON-NLS-1$
        Window.open(contentUrl, "_blank", "enabled");
    }

    /**
     * Called when the user clicks the Delete action.
     */
    protected void onDelete() {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("ontology-deleting.title"), //$NON-NLS-1$
                i18n.format("ontology-deleting.message")); //$NON-NLS-1$
        ontologyService.delete(getValue().getUuid(), new IServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(), 
                        i18n.format("ontology-deleted.title"),  //$NON-NLS-1$
                        i18n.format("ontology-deleted.message", getValue().getId())); //$NON-NLS-1$
                ValueChangeEvent.fire(OntologySummaryPanelItem.this, null);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ontology-deleted-error.title"), error); //$NON-NLS-1$
            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<OntologySummaryBean> handler) {
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
    public OntologySummaryBean getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(OntologySummaryBean value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(OntologySummaryBean value, boolean fireEvents) {
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
    private static String createLabel(OntologySummaryBean bean) {
        String label = bean.getBase();
        if (bean.getLabel() != null && bean.getLabel().trim().length() > 0) {
            label += " (" + bean.getLabel().trim() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return label;
    }

}
