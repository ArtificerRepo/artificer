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

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.local.events.ReloadHandler;
import org.artificer.ui.client.local.pages.artifacts.CommentsPanel;
import org.artificer.ui.client.local.pages.details.AddCustomPropertyDialog;
import org.artificer.ui.client.local.pages.details.AddRelationshipDialog;
import org.artificer.ui.client.local.pages.details.ClassifiersPanel;
import org.artificer.ui.client.local.pages.details.CustomPropertiesPanel;
import org.artificer.ui.client.local.pages.details.DeleteDialog;
import org.artificer.ui.client.local.pages.details.DescriptionInlineLabel;
import org.artificer.ui.client.local.pages.details.ModifyClassifiersDialog;
import org.artificer.ui.client.local.pages.details.RelationshipActionHandler;
import org.artificer.ui.client.local.pages.details.RelationshipsTable;
import org.artificer.ui.client.local.pages.details.SourceEditor;
import org.artificer.ui.client.local.services.ApplicationStateService;
import org.artificer.ui.client.local.services.ArtifactServiceCaller;
import org.artificer.ui.client.local.services.NotificationService;
import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;
import org.artificer.ui.client.local.util.DOMUtil;
import org.artificer.ui.client.local.util.DataBindingDateConverter;
import org.artificer.ui.client.local.widgets.common.EditableInlineLabel;
import org.artificer.ui.client.shared.beans.ArtifactBean;
import org.artificer.ui.client.shared.beans.ArtifactCommentBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsIndexBean;
import org.artificer.ui.client.shared.beans.NotificationBean;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ui.nav.client.local.Navigation;
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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    @Inject
    protected ApplicationStateService stateService;
    @Inject
    private Navigation navigation;

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
    DeleteDialog deleteDialog;

    // Overview tab
    @Inject @DataField("core-property-name") @Bound(property="name")
    EditableInlineLabel name;
    @Inject @DataField("core-property-version") @Bound(property="version")
    InlineLabel version;
    @Inject @DataField("core-property-type-1") @Bound(property="type")
    InlineLabel htype;
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
    @Inject @DataField("reverse-relationships-table")
    RelationshipsTable reverseRelationships;
    @Inject @DataField("relationships-header")
    InlineLabel relationshipsHeader;
    @Inject @DataField("reverse-relationships-header")
    InlineLabel reverseRelationshipsHeader;
    @Inject @DataField("btn-add-relationship")
    Button addRelationshipButton;
    @Inject
    Instance<AddRelationshipDialog> addRelationshipDialogFactory;
    protected boolean relationshipsLoaded;
    
    // Comments tab
    @Inject @DataField("sramp-artifact-tabs-comments")
    Anchor commentsTabAnchor;
    @Inject @DataField("add-comment-text")
    TextArea commentText;
    @Inject @DataField("add-comment-form-submit-button")
    Button commentButton;
    @Inject @DataField("comments-container")
    CommentsPanel comments;
    protected boolean commentsLoaded;

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
        pageContent = DOMUtil.findElementById(getElement(), "artifact-details-content-wrapper");
        pageContent.addClassName("hide");
        editorWrapper = DOMUtil.findElementById(getElement(), "editor-wrapper");
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

        name.setDialogTitle(i18n.format("artifact-details.edit-name"));
        name.setDialogLabel(i18n.format("artifact-details.new-name"));

        description.setDialogTitle(i18n.format("artifact-details.edit-descr"));
        description.setDialogLabel(i18n.format("artifact-details.new-descr"));
    }

    /**
     * @see AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        sourceLoaded = false;
        currentArtifact = null;
        pageContent.addClassName("hide");
        artifactLoading.getElement().removeClassName("hide");
        sourceTabAnchor.setVisible(false);
        editorWrapper.setAttribute("style", "display:none");

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

        commentsTabAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!commentsLoaded) {
                    loadComments(currentArtifact);
                }
                commentsTabAnchor.setFocus(false);
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
                notificationService.sendErrorNotification(i18n.format("artifact-details.error-getting-details"), error);
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
        deleteDialog.setLabel(artifact.getModel().getName());
        deleteDialog.show();
    }

    /**
     * Called when the user confirms the artifact deletion.
     */
    protected void onDeleteConfirm() {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("artifact-details.deleting-artifact-title"),
                i18n.format("artifact-details.deleting-artifact-msg", artifact.getModel().getName()));
        artifactService.delete(artifact.getModel(), new IServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("artifact-details.artifact-deleted"),
                        i18n.format("artifact-details.delete-success-msg", artifact.getModel().getName()));
                backToArtifacts.click();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("artifact-details.delete-error"),
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
    protected void loadRelationships(final ArtifactBean artifact) {
        relationships.setVisible(false);
        relationshipsTabProgress.setVisible(true);
        relationshipsHeader.setText("Targeted by " + artifact.getName());
        reverseRelationshipsHeader.setText("Targets " + artifact.getName());
        relationshipsTabProgress.setVisible(false);
        relationships.setVisible(true);
        reverseRelationships.setVisible(true);
        relationshipsLoaded = true;

        relationships.setRelationshipActionHandler(new RelationshipActionHandler() {
            @Override
            public void editRelationship(String oldRelationshipType, String newRelationshipType, String uuid,
                    IServiceInvocationHandler<Void> deleteArtifactHandler) {
                // delete relationships originating from this artifact
                artifactService.editRelationship(oldRelationshipType, newRelationshipType, artifact.getUuid(), uuid,
                        deleteArtifactHandler);
            }

            @Override
            public void deleteRelationship(String relationshipType, String uuid,
                    IServiceInvocationHandler<Void> deleteArtifactHandler) {
                // delete relationships originating from this artifact
                artifactService.deleteRelationship(relationshipType, artifact.getUuid(), uuid,
                        deleteArtifactHandler);
            }
        });

        reverseRelationships.setRelationshipActionHandler(new RelationshipActionHandler() {
            @Override
            public void editRelationship(String oldRelationshipType, String newRelationshipType, String uuid,
                    IServiceInvocationHandler<Void> deleteArtifactHandler) {
                // delete relationships originating from this artifact
                artifactService.editRelationship(oldRelationshipType, newRelationshipType, uuid, artifact.getUuid(),
                        deleteArtifactHandler);
            }

            @Override
            public void deleteRelationship(String relationshipType, String uuid,
                    IServiceInvocationHandler<Void> deleteArtifactHandler) {
                // delete relationships targeting this artifact
                artifactService.deleteRelationship(relationshipType, uuid, artifact.getUuid(),
                        deleteArtifactHandler);
            }
        });

        relationships.setReloadHandler(new ReloadHandler() {
            @Override
            public void reload() {
                doLoadRelationships(artifact);
            }
        });

        reverseRelationships.setReloadHandler(new ReloadHandler() {
            @Override
            public void reload() {
                doLoadRelationships(artifact);
            }
        });

        doLoadRelationships(artifact);
    }

    private void doLoadRelationships(final ArtifactBean artifact) {
        artifactService.getRelationships(artifact.getUuid(), artifact.getType(), new IServiceInvocationHandler<ArtifactRelationshipsIndexBean>() {
            @Override
            public void onReturn(ArtifactRelationshipsIndexBean data) {
                relationships.setValue(data.getRelationships());
                reverseRelationships.setValue(data.getReverseRelationships());
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("artifact-details.error-getting-relationships"), error);
            }
        });
    }

    /**
     * Called when the user clicks the Add Relationship button.
     * @param event
     */
    @EventHandler("btn-add-relationship")
    protected void onAddRelationship(ClickEvent event) {
        AddRelationshipDialog dialog = addRelationshipDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String relationshipType = event.getValue();
                if (relationshipType != null) {
                    // set the app into "new relationship" mode
                    stateService.setNewRelationshipSourceUuid(uuid);
                    stateService.setNewRelationshipType(relationshipType);
                    // redirect to artifacts page
                    navigation.goTo("ArtifactsPage");
                }
            }
        });
        dialog.show();
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
                editorWrapper.removeAttribute("style");
                sourceLoaded = true;
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("Error getting artifact content."), error);
            }
        });
    }

    /**
     * Called when the artifact meta data is loaded.
     * @param artifact
     */
    protected void update(ArtifactBean artifact) {
        this.artifact.setModel(artifact, InitialState.FROM_MODEL);
        this.feedLink.setHref(artifact.getRepositoryLink());
        this.linkToRepositoryContent.setHref(artifact.getRepositoryMediaLink());
        this.linkToRepositoryMetadata.setHref(artifact.getRepositoryLink());
        this.sourceEditor.setValue("");

        if (artifact.isTextDocument()) {
            sourceTabAnchor.setVisible(true);
        }

        deleteButton.setVisible(!artifact.isDerived());

        artifactLoading.getElement().addClassName("hide");
        pageContent.removeClassName("hide");

        if (stateService.inNewRelationshipMode() && stateService.getNewRelationshipSourceUuid().equals(uuid)) {
            // ie, we've just gotten back from selecting relationship targets and saving it -- go directly to that tab
            // TODO: Doesn't work.
            // TODO: Consider just reloading the relationships, then showing a success message?
//            relationshipsTabAnchor.fireEvent(new ClickEvent(){});

            // reset the state
            stateService.setNewRelationshipType(null);
            stateService.setNewRelationshipSourceUuid(null);
        }
    }

    /**
     * Sends the model back up to the server (saves local changes).
     */
    protected void pushModelToServer() {
        String noteTitle = i18n.format("artifact-details.updating-artifact.title");
        String noteBody = i18n.format("artifact-details.updating-artifact.message", artifact.getModel().getName());
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                noteTitle, noteBody);
        artifactService.update(artifact.getModel(), new IServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                String noteTitle = i18n.format("artifact-details.updated-artifact.title");
                String noteBody = i18n.format("artifact-details.updated-artifact.message", artifact.getModel().getName());
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        noteTitle, noteBody);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("artifact-details.error-updating-arty"),
                        error);
            }
        });
    }


    protected void loadComments(final ArtifactBean artifact) {
        comments.setValue(artifact.getComments());
        commentButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                addComment(artifact);
            }
        });
        commentsLoaded = true;
    }
    
    protected void addComment(final ArtifactBean artifact) {
        String comment = commentText.getValue();
        if (comment != null && comment.length() > 0) {
            final NotificationBean notificationBean = notificationService.startProgressNotification(
                    i18n.format("artifact-details.adding-comment"),
                    i18n.format("artifact-details.adding-comment-msg", artifact.getName()));

            artifactService.addComment(artifact.getUuid(), artifact.getType(), comment, new IServiceInvocationHandler<ArtifactCommentBean>() {
                @Override
                public void onReturn(ArtifactCommentBean data) {
                    notificationService.completeProgressNotification(notificationBean.getUuid(),
                            i18n.format("artifact-details.comment-added"),
                            i18n.format("artifact-details.comment-added-success-msg", artifact.getName()));

                    // add to the list
                    comments.addComment(data);
                }

                @Override
                public void onError(Throwable error) {
                    notificationService.completeProgressNotification(notificationBean.getUuid(),
                            i18n.format("artifact-details.error-adding-comment"),
                            error);
                }
            });

            // reset form
            commentText.setValue("");
        }
    }

}
