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
package org.overlord.sramp.ui.client.local.pages;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.ClientMessages;
import org.overlord.sramp.ui.client.local.pages.ontologies.AddOntologyDialog;
import org.overlord.sramp.ui.client.local.pages.ontologies.OntologyEditor;
import org.overlord.sramp.ui.client.local.pages.ontologies.OntologySummaryPanel;
import org.overlord.sramp.ui.client.local.pages.ontologies.UploadOntologyDialog;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.services.OntologyServiceCaller;
import org.overlord.sramp.ui.client.local.services.callback.IServiceInvocationHandler;
import org.overlord.sramp.ui.client.local.util.IUploadCompletionHandler;
import org.overlord.sramp.ui.client.shared.beans.*;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

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

    // Services
    @Inject
    NotificationService notificationService;
    @Inject
    ClientMessages i18n;
    @Inject
    OntologyServiceCaller ontologyService;

    // Dialog factories
    @Inject
    Instance<AddOntologyDialog> addOngologyDialogFactory;
    @Inject
    Instance<UploadOntologyDialog> uploadOntologyDialogFactory;

    // Widgets
    @Inject @DataField("ontologies-canvas")
    OntologySummaryPanel ontologySummaryPanel;
    @Inject @DataField("btn-new-ontology")
    Button newOntologyButton;
    @Inject @DataField("btn-upload-ontology")
    Button uploadOntologyButton;
    @Inject @DataField("ontology-editor")
    OntologyEditor editor;

    /**
     * Constructor.
     */
    public OntologiesPage() {
    }

    /**
     * Post construct.
     */
    @PostConstruct
    protected void postConstruct() {
        ontologySummaryPanel.addSelectionHandler(new SelectionHandler<OntologySummaryBean>() {

            @Override
            public void onSelection(SelectionEvent<OntologySummaryBean> event) {
                OntologySummaryBean bean = event.getSelectedItem();
                onOntologyClicked(bean);
            }
        });
    }

    private void doOntologyList() {
        ontologyService.list(true, new IServiceInvocationHandler<OntologyResultSetBean>() {
            @Override
            public void onReturn(OntologyResultSetBean data) {
                String selectedUuid = ontologySummaryPanel.getSelectedOntology() == null
                        ? null : ontologySummaryPanel.getSelectedOntology().getUuid();

                ontologySummaryPanel.setValue(data.getOntologies());

                // If something was selected, re-select it.  This is mostly to handle add/upload ontologies
                // refreshing the panel.
                if (selectedUuid != null) {
                    ontologySummaryPanel.restoreSelectedItem(selectedUuid);
                }
            }

            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ontologies-page.error-getting-ontologies"), error); //$NON-NLS-1$
            }
        });
    }

    @PageShown
    public void onPageShown() {
        doOntologyList();
    }

    /**
     * Event handler that fires when the user clicks the New Ontology button.
     *
     * @param event
     *            the event
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
     *
     * @param event
     *            the event
     */
    @EventHandler("btn-upload-ontology")
    public void onUploadOntologyClick(ClickEvent event) {
        final UploadOntologyDialog dialog = uploadOntologyDialogFactory.get();
        dialog.setCompletionHandler(new IUploadCompletionHandler() {
            @Override
            public void onImportComplete() {
                doOntologyList();
            }
        });
        dialog.show();
    }

    /**
     * Called when the user creates a new ontology.
     *
     * @param value
     *            the value
     */
    protected void onNewOntology(final OntologyBean value) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("new-ontology.creating.title"), //$NON-NLS-1$
                i18n.format("new-ontology.creating.message")); //$NON-NLS-1$
        ontologyService.add(value, new IServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("new-ontology.created.title"), //$NON-NLS-1$
                        i18n.format("new-ontology.created.message", value.getId())); //$NON-NLS-1$
                doOntologyList();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("new-ontology.created-error.title"), //$NON-NLS-1$
                        error);
            }
        });
    }

    /**
     * Called when the user clicks one of the ontologies in the list.
     *
     * @param ontology
     *            the ontology
     */
    protected void onOntologyClicked(OntologySummaryBean ontology) {
        if (ontologySummaryPanel.getSelectedOntology() != null) {
            if (editor.isDirty()) {
                Window.alert(i18n.format("ontologies-page.editor-is-dirty")); //$NON-NLS-1$
                return;
            }
            reloadOntologyEditor(ontology.getUuid());
        } else {
            editor.setValue(null);
        }
    }

    /**
     * Reload the ontology editor.
     *
     * @param uuid
     *            the uuid
     */
    private void reloadOntologyEditor(String uuid) {
        this.editor.clear();
        this.ontologyService.get(uuid, true, new IServiceInvocationHandler<OntologyBean>() {
            @Override
            public void onReturn(OntologyBean data) {
                editor.setValue(data);
            }

            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(
                        i18n.format("ontologies-page.error-loading-ontology"), error); //$NON-NLS-1$
                editor.clear();
            }
        });
    }

}
