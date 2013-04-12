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
package org.overlord.sramp.ui.client.local.pages.artifacts;

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.services.OntologyRpcService;
import org.overlord.sramp.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.ModalDialog;
import org.overlord.sramp.ui.client.shared.beans.OntologyBean;
import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * The dialog used when selecting classifiers for a particular ontology.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/artifacts_dialogs.html#classifier-dialog")
@Dependent
public class ClassifierFilterSelectionDialog extends ModalDialog {

    @Inject
    private OntologyRpcService ontologyRpcService;
    @Inject
    private NotificationService notificationService;

    @Inject @DataField("classifier-dialog-title")
    private InlineLabel title;
    @Inject @DataField("classifier-dialog-body")
    private FlowPanel body;
    @Inject
    private Instance<LoadingOntology> loading;

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
        body.add(loading.get());
        // TODO load the ontology
        ontologyRpcService.get(ontology.getUuid(), new IRpcServiceInvocationHandler<OntologyBean>() {
            @Override
            public void onReturn(OntologyBean data) {
                body.clear();
                renderSelectionTree(data);
            }
            @Override
            public void onError(Throwable error) {
                // TODO i18n
                String errorTitle = "Classifier Filter Error";
                String errorMsg = "Error showing classifiers in ontology " + ontology.getLabel() + ".";
                if (error instanceof SrampUiException) {
                    notificationService.sendErrorNotification(errorTitle, errorMsg, (SrampUiException) error);
                } else {
                    notificationService.sendErrorNotification(errorTitle, errorMsg, null);
                }
                hide();
            }
        });
    }

    /**
     * Creates and renders the tree that the user can use to select classifiers
     * from the ontology.
     * @param ontology
     */
    protected void renderSelectionTree(OntologyBean ontology) {
        // TODO Auto-generated method stub
        body.add(new InlineLabel("Data loaded!"));
    }

    public Set<String> getValue() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setValue(Set<String> value) {
        this.value = value;
    }

}
