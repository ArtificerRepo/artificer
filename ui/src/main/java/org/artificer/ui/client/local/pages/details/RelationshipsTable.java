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
package org.artificer.ui.client.local.pages.details;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.local.events.ReloadHandler;
import org.artificer.ui.client.local.pages.ArtifactDetailsPage;
import org.artificer.ui.client.local.services.NotificationService;
import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;
import org.artificer.ui.client.local.widgets.common.DropdownMenu;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsBean;
import org.artificer.ui.client.shared.beans.NotificationBean;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.TemplatedWidgetTable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A table of artifacts.
 *
 * @author eric.wittmann@redhat.com
 * @author Brett Meyer
 */
@Dependent
public class RelationshipsTable extends TemplatedWidgetTable implements HasValue<Map<String, ArtifactRelationshipsBean>> {

    @Inject
    protected TransitionAnchorFactory<ArtifactDetailsPage> toDetailsPageLinkFactory;
    @Inject
    Instance<DropdownMenu> dropdownMenuFactory;

    @Inject
    EditRelationshipDialog editDialog;
    @Inject
    DeleteDialog deleteDialog;

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected NotificationService notificationService;

    private RelationshipActionHandler actionHandler;
    private ReloadHandler reloadHandler;

    private int currentRowIndex;
    private ArtifactRelationshipBean currentRelationship;

    public void setRelationshipActionHandler(RelationshipActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    public void setReloadHandler(ReloadHandler reloadHandler) {
        this.reloadHandler = reloadHandler;
    }

    @PostConstruct
    protected void onPostConstruct() {
        editDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onEdit();
            }
        });
        deleteDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDeleteConfirm();
            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Map<String, ArtifactRelationshipsBean>> handler) {
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public Map<String, ArtifactRelationshipsBean> getValue() {
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Map<String, ArtifactRelationshipsBean> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(Map<String, ArtifactRelationshipsBean> value, boolean fireEvents) {
        clear();
        Set<String> keys = new TreeSet<String>(value.keySet());
        for (String key : keys) {
            addHeadingRow(key);
            ArtifactRelationshipsBean relationships = value.get(key);
            for (ArtifactRelationshipBean relationship : relationships.getRelationships()) {
                addDataRow(relationship);
            }
        }
    }

    /**
     * Adds a heading row to the table.  One of these gets added per
     * relationship type.
     * @param key
     */
    private void addHeadingRow(String key) {
        int rowIdx = this.rowElements.size();
        InlineLabel heading = new InlineLabel(key);
        add(rowIdx, 0, heading).setAttribute("colspan", "4");
        getRow(rowIdx).setClassName("sramp-relationship-type");
    }

    /**
     * Adds a single data row to the table.
     * @param relationship
     */
    private void addDataRow(final ArtifactRelationshipBean relationship) {
        final int rowIdx = this.rowElements.size();

        Anchor name = toDetailsPageLinkFactory.get("uuid", relationship.getTargetUuid());
        name.setText(relationship.getTargetName());
        InlineLabel type = new InlineLabel(relationship.getTargetType());
        InlineLabel derived = new InlineLabel(relationship.getTargetDerived().toString());

        add(rowIdx, 0, name);
        add(rowIdx, 1, type);
        add(rowIdx, 2, derived);

        if (relationship.getRelationshipGeneric()) {
            DropdownMenu actions = dropdownMenuFactory.get();
            actions.setLabel("Actions");

            Anchor editButton = actions.addMenuAction("Edit");
            editButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    currentRowIndex = rowIdx;
                    currentRelationship = relationship;
                    editDialog.setRelationshipType(relationship.getRelationshipType());
                    editDialog.show();
                }
            });

            Anchor deleteButton = actions.addMenuAction("Delete");
            deleteButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    currentRowIndex = rowIdx;
                    currentRelationship = relationship;
                    deleteDialog.setLabel(relationship.getRelationshipType());
                    deleteDialog.show();
                }
            });

            add(rowIdx, 3, actions);
        } else {
            add(rowIdx, 3, new InlineLabel(""));
        }
    }

    protected void onEdit() {
        final String relationshipType = currentRelationship.getRelationshipType();

        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("artifact-details.editing-relationship-title"),
                i18n.format("artifact-details.editing-relationship-msg"));
        actionHandler.editRelationship(relationshipType, editDialog.getRelationshipType(), currentRelationship.getTargetUuid(),
                new IServiceInvocationHandler<Void>() {
                    @Override
                    public void onReturn(Void data) {
                        notificationService.completeProgressNotification(notificationBean.getUuid(),
                                i18n.format("artifact-details.relationship-edited"),
                                i18n.format("artifact-details.edit-relationship-success-msg"));

                        reloadHandler.reload();
                    }

                    @Override
                    public void onError(Throwable error) {
                        notificationService.completeProgressNotification(notificationBean.getUuid(),
                                i18n.format("artifact-details.edit-relationship-error"),
                                error);
                    }
                });
    }

    /**
     * Called when the user confirms the deletion.
     */
    protected void onDeleteConfirm() {
        deleteRow(currentRowIndex);

        final String relationshipType = currentRelationship.getRelationshipType();

        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("artifact-details.deleting-relationship-title"),
                i18n.format("artifact-details.deleting-relationship-msg", relationshipType));
        actionHandler.deleteRelationship(relationshipType, currentRelationship.getTargetUuid(),
                new IServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("artifact-details.relationship-deleted"),
                        i18n.format("artifact-details.delete-relationship-success-msg", relationshipType));
            }

            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("artifact-details.delete-relationship-error"),
                        error);
            }
        });
    }

}
