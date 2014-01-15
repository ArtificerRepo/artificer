/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.ui.client.local.pages;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.ClientMessages;
import org.overlord.sramp.ui.client.local.pages.ontologies.AddOntologyDialog;
import org.overlord.sramp.ui.client.local.pages.ontologies.OntologyEditor;
import org.overlord.sramp.ui.client.local.pages.ontologies.UploadOntologyDialog;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.services.OntologyRpcService;
import org.overlord.sramp.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.overlord.sramp.ui.client.local.util.IUploadCompletionHandler;
import org.overlord.sramp.ui.client.local.widgets.common.UnorderedListPanel;
import org.overlord.sramp.ui.client.shared.beans.NotificationBean;
import org.overlord.sramp.ui.client.shared.beans.OntologyBean;
import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

/**
 * The "Ontologies" page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/ontologies.html#page")
@Page(path="ontologies")
@Dependent
public class OntologiesPage extends AbstractPage {

    // Breadcrumbs
    @Inject @DataField("back-to-dashboard")
    TransitionAnchor<DashboardPage> backToDashboard;
    
    TreeMap<String, OntologySummaryBean> ontologyMap = new TreeMap<String, OntologySummaryBean>();
    
    // Services
    @Inject
    NotificationService notificationService;
    @Inject
    ClientMessages i18n;
    @Inject
    OntologyRpcService ontologyService;
    
    // Dialog factories
    @Inject
    Instance<AddOntologyDialog> addOngologyDialogFactory;
    @Inject
    Instance<UploadOntologyDialog> uploadOntologyDialogFactory;

    // Widgets
    @Inject @DataField
    UnorderedListPanel ontologies;
    @Inject @DataField("btn-new-ontology")
    Button newOntologyButton;
    @Inject @DataField("btn-upload-ontology")
    Button uploadOntologyButton;
    @Inject @DataField("ontology-editor")
    OntologyEditor editor;
    
    Widget selectedOntologyWidget = null;
    
    /**
     * Constructor.
     */
    public OntologiesPage() {
    }
    
    @PostConstruct
    protected void postConstruct() {
    }
    
    /**
     * @see org.overlord.sramp.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        ontologyService.list(true, new IRpcServiceInvocationHandler<List<OntologySummaryBean>>() {
            @Override
            public void onReturn(List<OntologySummaryBean> data) {
                ontologyMap.clear();
                for (OntologySummaryBean ontology : data) {
                    ontologyMap.put(createOntologyLabel(ontology), ontology);
                }
                updateOntologyList();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ontologies-page.error-getting-ontologies"), error); //$NON-NLS-1$
            }
        });
    }

    /**
     * @param data
     */
    protected void updateOntologyList() {
        ontologies.clear();
        selectedOntologyWidget = null;
        for (Entry<String, OntologySummaryBean> entry : ontologyMap.entrySet()) {
            final OntologySummaryBean ontology = entry.getValue();
            final Anchor a = new Anchor(createOntologyLabel(ontology));
            a.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    onOntologyClicked(ontology, a);
                }
            });
            a.getElement().setAttribute("data-uuid", ontology.getUuid()); //$NON-NLS-1$
            ontologies.add(a);
        }
    }

    /**
     * Creates a label to use in the lsit of ontologies.
     * @param ontology
     */
    private String createOntologyLabel(final OntologySummaryBean ontology) {
        String label = ontology.getBase();
        if (ontology.getLabel() != null && ontology.getLabel().trim().length() > 0) {
            label += " (" + ontology.getLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return label;
    }

    /**
     * Called when the user clicks one of the ontologies in the list.
     * @param ontology
     * @param widget
     */
    protected void onOntologyClicked(OntologySummaryBean ontology, Widget widget) {
        if (widget == selectedOntologyWidget) {
            editor.setValue(null);
            widget.getElement().getParentElement().removeClassName("active"); //$NON-NLS-1$
            selectedOntologyWidget = null;
            return;
        }
        
        if (selectedOntologyWidget != null) {
            if (editor.isDirty()) {
                Window.alert("ontologies-page.editor-is-dirty"); //$NON-NLS-1$
                return;
            }
            selectedOntologyWidget.getElement().getParentElement().removeClassName("active"); //$NON-NLS-1$
            selectedOntologyWidget = null;
        }
        widget.getElement().getParentElement().addClassName("active"); //$NON-NLS-1$
        selectedOntologyWidget = widget;
        editor.clear();
        ontologyService.get(ontology.getUuid(), true, new IRpcServiceInvocationHandler<OntologyBean>() {
            @Override
            public void onReturn(OntologyBean data) {
                editor.setValue(data);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ontologies-page.error-loading-ontology"), error); //$NON-NLS-1$
                editor.clear();
            }
        });
    }

    /**
     * Event handler that fires when the user clicks the New Ontology button.
     * @param event
     */
    @EventHandler("btn-new-ontology")
    public void onAddOntologyClick(ClickEvent event) {
        AddOntologyDialog dialog = addOngologyDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<OntologyBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<OntologyBean> event) {
                onNewOntology(event.getValue());
            }
        });
        dialog.show();
    }

    /**
     * Event handler that fires when the user clicks the Upload Ontology button.
     * @param event
     */
    @EventHandler("btn-upload-ontology")
    public void onUploadOntologyClick(ClickEvent event) {
        UploadOntologyDialog dialog = uploadOntologyDialogFactory.get();
        dialog.setCompletionHandler(new IUploadCompletionHandler() {
            @Override
            public void onImportComplete() {
                if (isAttached()) {
                    editor.setValue(null);
                    onPageShowing();
                }
            }
        });
        dialog.show();
    }

    /**
     * Called when the user creates a new ontology.
     * @param value
     */
    protected void onNewOntology(final OntologyBean value) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("new-ontology.creating.title"), //$NON-NLS-1$
                i18n.format("new-ontology.creating.message")); //$NON-NLS-1$
        ontologyService.add(value, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(), 
                        i18n.format("new-ontology.created.title"), //$NON-NLS-1$
                        i18n.format("new-ontology.created.message", value.getId())); //$NON-NLS-1$
                editor.setValue(null);
                onPageShowing();
                // TODO select the ontology automatically here?
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(), 
                        i18n.format("new-ontology.created-error.title"), //$NON-NLS-1$
                        error);
            }
        });
    }

}
