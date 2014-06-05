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

import java.util.List;
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
import org.overlord.sramp.ui.client.local.pages.ontologies.OntologiesTable;
import org.overlord.sramp.ui.client.local.pages.ontologies.OntologiesUtil;
import org.overlord.sramp.ui.client.local.pages.ontologies.OntologyEditor;
import org.overlord.sramp.ui.client.local.pages.ontologies.UploadOntologyDialog;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.services.OntologyRpcService;
import org.overlord.sramp.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.overlord.sramp.ui.client.local.util.IUploadCompletionHandler;
import org.overlord.sramp.ui.client.shared.beans.NotificationBean;
import org.overlord.sramp.ui.client.shared.beans.OntologyBean;
import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;

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
    OntologiesTable ontologies;
    @Inject @DataField("btn-new-ontology")
    Button newOntologyButton;
    @Inject @DataField("btn-upload-ontology")
    Button uploadOntologyButton;
    @Inject @DataField("ontology-editor")
    OntologyEditor editor;

    private String uuidNewOntology;

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
        ontologies.addSelectionHandler(new SelectionHandler<OntologySummaryBean>() {

            @Override
            public void onSelection(SelectionEvent<OntologySummaryBean> event) {
                OntologySummaryBean bean = event.getSelectedItem();
                onOntologyClicked(bean);
            }
        });
    }

    /**
     * On page showing.
     *
     * @see org.overlord.sramp.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        ontologyService.list(true, new IRpcServiceInvocationHandler<List<OntologySummaryBean>>() {
            @Override
            public void onReturn(List<OntologySummaryBean> data) {
                ontologyMap.clear();
                for (OntologySummaryBean ontology : data) {
                    ontologyMap.put(OntologiesUtil.createOntologyLabel(ontology), ontology);
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
     * Update ontology list.
     */
    protected void updateOntologyList() {
        ontologies.clear();
        ontologies.setValue(ontologyMap);
        if (uuidNewOntology != null && !uuidNewOntology.equals("") //$NON-NLS-1$
                && ontologies.getSelectedOntology() == null) {
            ontologies.selectItem(uuidNewOntology);
        } else if (ontologyMap.size() == 1) {
            ontologies.selectItem(ontologyMap.get(ontologyMap.keySet().iterator().next()).getUuid());
        } else if (ontologies.getSelectedOntology() != null) {
            ontologies.selectItem(ontologies.getSelectedOntology().getUuid());
        }
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
                String uuid = dialog.getOntologyUploadedUUID();
                if (uuid != null && !uuid.equals("")) { //$NON-NLS-1$
                    uuidNewOntology = uuid;
                }
                onPageShowing();

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
        ontologyService.add(value, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("new-ontology.created.title"), //$NON-NLS-1$
                        i18n.format("new-ontology.created.message", value.getId())); //$NON-NLS-1$
                uuidNewOntology = value.getUuid();
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

    /**
     * Called when the user clicks one of the ontologies in the list.
     *
     * @param ontology
     *            the ontology
     */
    protected void onOntologyClicked(OntologySummaryBean ontology) {
        if (ontology == ontologies.getSelectedOntology()) {
            if (this.editor.isDirty()) {
                Window.alert(i18n.format("ontologies-page.editor-is-dirty")); //$NON-NLS-1$
                return;
            }
            this.editor.setValue(null);
            ontologies.unselectItem();
            return;
        } else if (ontologies.getSelectedOntology() != null) {
            if (this.editor.isDirty()) {
                Window.alert(i18n.format("ontologies-page.editor-is-dirty")); //$NON-NLS-1$
                return;
            }
            ontologies.unselectItem();
        }
        ontologies.selectItem(ontology);

        reloadOntologyEditor(ontology.getUuid());
        this.uuidNewOntology = ""; //$NON-NLS-1$

    }

    /**
     * Reload the ontology editor.
     *
     * @param uuid
     *            the uuid
     */
    private void reloadOntologyEditor(String uuid) {
        this.editor.clear();
        this.ontologyService.get(uuid, true, new IRpcServiceInvocationHandler<OntologyBean>() {
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
