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
package org.artificer.ui.client.local.pages.artifacts;

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.local.services.NotificationService;
import org.artificer.ui.client.local.services.OntologyServiceCaller;
import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;
import org.artificer.ui.client.local.widgets.ontologies.LoadingOntology;
import org.artificer.ui.client.local.widgets.ontologies.OntologySelectorWithToolbar;
import org.artificer.ui.client.shared.beans.OntologyBean;
import org.artificer.ui.client.shared.beans.OntologySummaryBean;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * The dialog used when selecting classifiers for a particular ontology.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/artificer/ui/client/local/site/dialogs/classifier-dialog.html#classifier-dialog")
@Dependent
public class ClassifierFilterSelectionDialog extends ModalDialog implements HasValueChangeHandlers<Set<String>> {

    @Inject
    protected ClientMessages i18n;
    @Inject
    private OntologyServiceCaller ontologyServiceCaller;
    @Inject
    private NotificationService notificationService;

    @Inject @DataField("classifier-dialog-title")
    private InlineLabel title;
    @Inject @DataField("classifier-dialog-body")
    private FlowPanel body;
    @Inject
    private Instance<LoadingOntology> loading;
    @Inject
    private OntologySelectorWithToolbar selector;

    @Inject @DataField("classifier-dialog-btn-ok")
    private Button okButton;

    private Set<String> value;

    /**
     * Constructor.
     */
    public ClassifierFilterSelectionDialog() {
    }

    /**
     * Sets the ontology that this dialog will be displaying the classifiers for.
     * @param ontology
     */
    public void setOntology(final OntologySummaryBean ontology) {
        title.setText(ontology.getLabel());
        body.clear();
        LoadingOntology w = loading.get();
        w.getElement().removeClassName("hide"); //$NON-NLS-1$
        body.add(w);
        ontologyServiceCaller.get(ontology.getUuid(), false, new IServiceInvocationHandler<OntologyBean>() {
            @Override
            public void onReturn(OntologyBean data) {
                body.clear();
                renderSelectionTree(data);
                updateTreeItemCheckedStates();
            }
            @Override
            public void onError(Throwable error) {
                String errorTitle = i18n.format("classifier-filter-sel-dialog.error.title"); //$NON-NLS-1$
                String errorMsg = i18n.format("classifier-filter-sel-dialog.error.msg", ontology.getLabel()); //$NON-NLS-1$
                if (error instanceof ArtificerUiException) {
                    notificationService.sendErrorNotification(errorTitle, errorMsg, (ArtificerUiException) error);
                } else {
                    notificationService.sendErrorNotification(errorTitle, errorMsg, null);
                }
                hide();
            }
        });
    }

    /**
     * Updates the checked state of the various checkboxes in the tree.  Make sure the
     * right checkboxes are checked based on the current values.
     */
    protected void updateTreeItemCheckedStates() {
        selector.setSelection(value);
        selector.expandAll();
    }

    /**
     * Creates and renders the tree that the user can use to select classifiers
     * from the ontology.
     * @param ontology
     */
    protected void renderSelectionTree(OntologyBean ontology) {
        body.add(selector);
        selector.refresh(ontology);
    }

    /**
     * @return the current value
     */
    public Set<String> getValue() {
        return value;
    }

    /**
     * @param value a new value
     */
    public void setValue(Set<String> value) {
        this.value = value;
    }

    /**
     * Called when the user clicks the OK button.
     * @param event
     */
    @EventHandler("classifier-dialog-btn-ok")
    public void onOkClick(ClickEvent event) {
        // Gather up everything that was checked in the UI
        this.value = selector.getSelection();
        ValueChangeEvent.fire(this, this.value);
        hide();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

}
