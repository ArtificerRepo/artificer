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
package org.artificer.ui.client.local.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.local.pages.details.AddCustomPropertyDialog;
import org.artificer.ui.client.local.pages.details.ClassifiersPanel;
import org.artificer.ui.client.local.pages.details.CustomPropertiesPanel;
import org.artificer.ui.client.local.pages.details.DeleteArtifactDialog;
import org.artificer.ui.client.local.pages.details.DescriptionInlineLabel;
import org.artificer.ui.client.local.pages.details.ModifyClassifiersDialog;
import org.artificer.ui.client.local.pages.details.RelationshipsTable;
import org.artificer.ui.client.local.pages.details.SourceEditor;
import org.artificer.ui.client.local.services.ArtifactServiceCaller;
import org.artificer.ui.client.local.services.NotificationService;
import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;
import org.artificer.ui.client.local.util.DOMUtil;
import org.artificer.ui.client.local.util.DataBindingDateConverter;
import org.artificer.ui.client.local.widgets.common.EditableInlineLabel;
import org.artificer.ui.client.shared.beans.ArtifactBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsIndexBean;
import org.artificer.ui.client.shared.beans.NotificationBean;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.HtmlSnippet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 * The page shown to the user when she clicks on one of the artifacts
 * displayed in the Artifacts Table on the Artifacts page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/artificer/ui/client/local/site/artifact-details.html#page")
@Page(path="details")
@Dependent
public class ArtifactDetailsPage extends AbstractPage {

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected ArtifactServiceCaller artifactService;
    @Inject
    protected NotificationService notificationService;
    protected ArtifactBean currentArtifact;

    @PageState
    private String uuid;

    @Inject @AutoBound
    protected DataBinder<ArtifactBean> artifact;

    // Breadcrumbs
    @Inject @DataField("back-to-dashboard")
    TransitionAnchor<DashboardPage> backToDashboard;
    @Inject @DataField("back-to-artifacts")
    TransitionAnchor<ArtifactsPage> backToArtifacts;

    @Inject @DataField("feed-link")
    Anchor feedLink;

    // Actions
    @Inject  @DataField("btn-delete")
    Button deleteButton;
    @Inject
    DeleteArtifactDialog deleteDialog;

    // Overview tab
    @Inject @DataField("core-property-name") @Bound(property="name")
    EditableInlineLabel name;
    @Inject @DataField("core-property-version") @Bound(property="version")
    InlineLabel version;
    @Inject @DataField("core-property-type-1") @Bound(property="type")
    InlineLabel htype;
    @Inject @DataField("link-download-content")
    Anchor downloadContentLink;
    @Inject @DataField("link-repository-content")
    Anchor linkToRepositoryContent;
    @Inject @DataField("link-repository-metadata")
    Anchor linkToRepositoryMetadata;
    @Inject @DataField("core-property-type-2") @Bound(property="type")
    Label type;
    @Inject @DataField("core-property-uuid") @Bound(property="uuid")
    Label uuidField;
    @Inject @DataField("core-property-derived") @Bound(property="derived")
    Label derived;
    @Inject @DataField("core-property-createdOn") @Bound(property="createdOn", converter=DataBindingDateConverter.class)
    InlineLabel createdOn;
    @Inject @DataField("core-property-createdBy") @Bound(property="createdBy")
    InlineLabel createdBy;
    @Inject @DataField("core-property-modifiedOn") @Bound(property="updatedOn", converter=DataBindingDateConverter.class)
    InlineLabel modifiedOn;
    @Inject @DataField("core-property-modifiedBy") @Bound(property="updatedBy")
    InlineLabel modifiedBy;
    @Inject @DataField("custom-properties-container") @Bound(property="properties")
    CustomPropertiesPanel customProperties;
    @Inject @DataField("add-property-button")
    Button addProperty;
    @Inject
    Instance<AddCustomPropertyDialog> addPropertyDialogFactory;
    @Inject @DataField("classifiers-container") @Bound(property="classifiedBy")
    ClassifiersPanel classifiers;
    @Inject @DataField("modify-classifiers-button")
    Button modifyClassifier;
    @Inject
    Instance<ModifyClassifiersDialog> modifyClassifiersDialogFactory;
    @Inject @DataField("core-property-description") @Bound(property="description")
    DescriptionInlineLabel description;

    // Relationships tab
    @Inject @DataField("sramp-artifact-tabs-relationships")
    Anchor relationshipsTabAnchor;
    @Inject @DataField("relationship-tab-progress")
    HtmlSnippet relationshipsTabProgress;
    @Inject @DataField("relationships-table")
    RelationshipsTable relationships;
    protected boolean relationshipsLoaded;

    // Source tab
    @Inject @DataField("sramp-artifact-tabs-source")
    Anchor sourceTabAnchor;
    @Inject @DataField("source-tab-progress")
    HtmlSnippet sourceTabProgress;
    @Inject @DataField("artifact-editor")
    SourceEditor sourceEditor;
    protected boolean sourceLoaded;

    @Inject @DataField("artifact-details-loading-spinner")
    protected HtmlSnippet artifactLoading;
    protected Element pageContent;
    protected Element editorWrapper;

    /**
     * Constructor.
     */
    public ArtifactDetailsPage() {
    }

    /**
     * Called after the widget is constructed.
     */
    @PostConstruct
    protected void onPostConstruct() {
        pageContent = DOMUtil.findElementById(getElement(), "artifact-details-content-wrapper"); //$NON-NLS-1$
        pageContent.addClassName("hide"); //$NON-NLS-1$
        editorWrapper = DOMUtil.findElementById(getElement(), "editor-wrapper"); //$NON-NLS-1$
        artifact.addPropertyChangeHandler(new PropertyChangeHandler<Object>() {
            @Override
            public void onPropertyChange(PropertyChangeEvent<Object> event) {
                pushModelToServer();
            }
        });
        deleteDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDeleteConfirm();
            }
        });

        name.setDialogTitle(i18n.format("artifact-details.edit-name")); //$NON-NLS-1$
        name.setDialogLabel(i18n.format("artifact-details.new-name")); //$NON-NLS-1$

        description.setDialogTitle(i18n.format("artifact-details.edit-descr")); //$NON-NLS-1$
        description.setDialogLabel(i18n.format("artifact-details.new-descr")); //$NON-NLS-1$
    }

    /**
     * @see AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        sourceLoaded = false;
        currentArtifact = null;
        pageContent.addClassName("hide"); //$NON-NLS-1$
        artifactLoading.getElement().removeClassName("hide"); //$NON-NLS-1$
        sourceTabAnchor.setVisible(false);
        editorWrapper.setAttribute("style", "display:none"); //$NON-NLS-1$ //$NON-NLS-2$

        relationshipsTabAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!relationshipsLoaded) {
                    loadRelationships(currentArtifact);
                }
                relationshipsTabAnchor.setFocus(false);
            }
        });
        sourceTabAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!sourceLoaded) {
                    loadSource(currentArtifact);
                }
                sourceTabAnchor.setFocus(false);
            }
        });
    }

    @PageShown
    public void onPageShown() {
        artifactService.get(uuid, new IServiceInvocationHandler<ArtifactBean>() {
            @Override
            public void onReturn(ArtifactBean data) {
                currentArtifact = data;
                update(data);
            }

            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("artifact-details.error-getting-details"), error); //$NON-NLS-1$
            }
        });
    }

    /**
     * Called when the user clicks the Add Property button.
     * @param event
     */
    @EventHandler("add-property-button")
    protected void onAddProperty(ClickEvent event) {
        AddCustomPropertyDialog dialog = addPropertyDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<Map.Entry<String,String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Entry<String, String>> event) {
                Entry<String, String> value = event.getValue();
                if (value != null) {
                    String propName = value.getKey();
                    String propValue = value.getValue();
                    Map<String, String> newProps = new HashMap<String,String>(artifact.getModel().getProperties());
                    newProps.put(propName, propValue);
                    customProperties.setValue(newProps, true);
                }
            }
        });
        dialog.show();
    }

    /**
     * Called when the user clicks the Delete button.
     * @param event
     */
    @EventHandler("btn-delete")
    protected void onDeleteClick(ClickEvent event) {
        deleteDialog.setArtifactName(artifact.getModel().getName());
        deleteDialog.show();
    }

    /**
     * Called when the user confirms the artifact deletion.
     */
    protected void onDeleteConfirm() {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("artifact-details.deleting-artifact-title"), //$NON-NLS-1$
                i18n.format("artifact-details.deleting-artifact-msg", artifact.getModel().getName())); //$NON-NLS-1$
        artifactService.delete(artifact.getModel(), new IServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("artifact-details.artifact-deleted"), //$NON-NLS-1$
                        i18n.format("artifact-details.delete-success-msg", artifact.getModel().getName())); //$NON-NLS-1$
                backToArtifacts.click();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("artifact-details.delete-error"), //$NON-NLS-1$
                        error);
            }
        });
    }

    /**
     * Called when the user clicks the Add Property button.
     * @param event
     */
    @EventHandler("modify-classifiers-button")
    protected void onModifyClassifiers(ClickEvent event) {
        ModifyClassifiersDialog dialog = modifyClassifiersDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                List<String> value = event.getValue();
                if (value != null) {
                    List<String> newValue = new ArrayList<String>(value);
                    classifiers.setValue(newValue, true);
                }
            }
        });
        dialog.setValue(artifact.getModel().getClassifiedBy());
        dialog.show();
    }

    /**
     * Loads the artifact's relationships and displays them in the proper table.
     * @param artifact
     */
    protected void loadRelationships(ArtifactBean artifact) {
        relationships.setVisible(false);
        relationshipsTabProgress.setVisible(true);
        artifactService.getRelationships(artifact.getUuid(), artifact.getType(), new IServiceInvocationHandler<ArtifactRelationshipsIndexBean>() {
            @Override
            public void onReturn(ArtifactRelationshipsIndexBean data) {
                relationships.setValue(data.getRelationships());
                relationshipsTabProgress.setVisible(false);
                relationships.setVisible(true);
                relationshipsLoaded = true;
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("artifact-details.error-getting-relationships"), error); //$NON-NLS-1$
            }
        });
    }

    /**
     * Loads the artifact's source (async) into the source tab's editor control.
     * @param artifact
     */
    protected void loadSource(ArtifactBean artifact) {
        if (!artifact.isTextDocument()) {
            return;
        }
        sourceTabProgress.setVisible(true);
        artifactService.getDocumentContent(artifact.getUuid(), artifact.getType(), new IServiceInvocationHandler<String>() {
            @Override
            public void onReturn(String data) {
                sourceEditor.setValue(data);
                sourceTabProgress.setVisible(false);
                editorWrapper.removeAttribute("style"); //$NON-NLS-1$
                sourceLoaded = true;
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("Error getting artifact content."), error); //$NON-NLS-1$
            }
        });
    }

    /**
     * Called when the artifact meta data is loaded.
     * @param artifact
     */
    protected void update(ArtifactBean artifact) {
        this.artifact.setModel(artifact, InitialState.FROM_MODEL);
        String contentUrl = GWT.getModuleBaseURL() + "services/artifactDownload"; //$NON-NLS-1$
        contentUrl += "?uuid=" + artifact.getUuid() + "&type=" + artifact.getType(); //$NON-NLS-1$ //$NON-NLS-2$
        this.downloadContentLink.setHref(contentUrl);
        // hide the link if the artifact is derived or has no content
        this.downloadContentLink.setVisible(!artifact.isDerived() && artifact.isDocument());
        this.feedLink.setHref(artifact.getRepositoryLink());
        this.linkToRepositoryContent.setHref(artifact.getRepositoryMediaLink());
        this.linkToRepositoryMetadata.setHref(artifact.getRepositoryLink());
        this.sourceEditor.setValue(""); //$NON-NLS-1$

        if (artifact.isTextDocument()) {
            sourceTabAnchor.setVisible(true);
        }
        if (artifact.isDocument()) {
            this.downloadContentLink.getElement().removeClassName("hidden"); //$NON-NLS-1$
        } else {
            this.downloadContentLink.getElement().addClassName("hidden"); //$NON-NLS-1$
        }

        deleteButton.setVisible(!artifact.isDerived());

        artifactLoading.getElement().addClassName("hide"); //$NON-NLS-1$
        pageContent.removeClassName("hide"); //$NON-NLS-1$
    }

    /**
     * Sends the model back up to the server (saves local changes).
     */
    protected void pushModelToServer() {
        String noteTitle = i18n.format("artifact-details.updating-artifact.title"); //$NON-NLS-1$
        String noteBody = i18n.format("artifact-details.updating-artifact.message", artifact.getModel().getName()); //$NON-NLS-1$
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                noteTitle, noteBody);
        artifactService.update(artifact.getModel(), new IServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                String noteTitle = i18n.format("artifact-details.updated-artifact.title"); //$NON-NLS-1$
                String noteBody = i18n.format("artifact-details.updated-artifact.message", artifact.getModel().getName()); //$NON-NLS-1$
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        noteTitle, noteBody);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("artifact-details.error-updating-arty"), //$NON-NLS-1$
                        error);
            }
        });
    }

}
