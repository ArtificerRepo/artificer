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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.DateBox;
import org.overlord.sramp.ui.client.local.widgets.common.RadioButton;
import org.overlord.sramp.ui.client.shared.beans.ArtifactFilterBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactOriginEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The artifact filtersPanel sidebar.  Whenever the user changes any of the settings in
 * the filter sidebar, a ValueChangeEvent will be fired.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/artifacts.html#sramp-filter-sidebar")
@Dependent
public class ArtifactFilters extends Composite implements HasValueChangeHandlers<ArtifactFilterBean> {

    private ArtifactFilterBean currentState = null;

    // Artifact Type
    @Inject @DataField
    protected TextBox artifactType;

    // Date Created
    @Inject @DataField
    protected DateBox dateCreatedFrom;
    @Inject @DataField
    protected DateBox dateCreatedTo;

    // Date Modified
    @Inject @DataField
    protected DateBox dateModifiedFrom;
    @Inject @DataField
    protected DateBox dateModifiedTo;

    // Create By
    @Inject @DataField
    protected TextBox createdBy;

    // Last Modified By
    @Inject @DataField
    protected TextBox lastModifiedBy;

    // Origin
    @Inject @DataField
    protected RadioButton originAny;
    @Inject @DataField
    protected RadioButton originPrimary;
    @Inject @DataField
    protected RadioButton originDerived;

    // Clear core filtersPanel
    @Inject @DataField
    protected Anchor clearCoreFilters;

    /**
     * Constructor.
     */
    public ArtifactFilters() {
    }

    /**
     * Called after construction and injection.
     */
    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void postConstruct() {
        originPrimary.setValue(true);
        clearCoreFilters.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setValue(new ArtifactFilterBean());
                onFilterValueChange();
            }
        });
        @SuppressWarnings("rawtypes")
        ValueChangeHandler valueChangeHandler = new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                onFilterValueChange();
            }
        };
        artifactType.addValueChangeHandler(valueChangeHandler);
        dateCreatedFrom.addValueChangeHandler(valueChangeHandler);
        dateCreatedTo.addValueChangeHandler(valueChangeHandler);
        dateModifiedFrom.addValueChangeHandler(valueChangeHandler);
        dateModifiedTo.addValueChangeHandler(valueChangeHandler);
        createdBy.addValueChangeHandler(valueChangeHandler);
        lastModifiedBy.addValueChangeHandler(valueChangeHandler);
        originAny.addValueChangeHandler(valueChangeHandler);
        originPrimary.addValueChangeHandler(valueChangeHandler);
        originDerived.addValueChangeHandler(valueChangeHandler);
    }

    /**
     * Called whenever any filter value changes.
     */
    protected void onFilterValueChange() {
        ArtifactFilterBean newState = new ArtifactFilterBean();
        newState.setArtifactType(artifactType.getValue())
            .setDateCreatedFrom(dateCreatedFrom.getDateValue())
            .setDateCreatedTo(dateCreatedTo.getDateValue())
            .setDateModifiedFrom(dateModifiedFrom.getDateValue())
            .setDateModifiedTo(dateModifiedTo.getDateValue())
            .setCreatedBy(createdBy.getValue())
            .setLastModifiedBy(lastModifiedBy.getValue())
            .setOrigin(ArtifactOriginEnum.valueOf(originAny.getValue(), originPrimary.getValue(), originDerived.getValue()));

        ArtifactFilterBean oldState = this.currentState;
        this.currentState = newState;
        // Only fire a change event if something actually changed.
        ValueChangeEvent.fireIfNotEqual(this, oldState, currentState);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ArtifactFilterBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @return the current filter settings
     */
    public ArtifactFilterBean getValue() {
        return this.currentState;
    }

    /**
     * @param value the new filter settings
     */
    public void setValue(ArtifactFilterBean value) {
        artifactType.setValue(value.getArtifactType() == null ? "" : value.getArtifactType());
        dateCreatedFrom.setDateValue(value.getDateCreatedFrom() == null ? null : value.getDateCreatedFrom());
        dateCreatedTo.setDateValue(value.getDateCreatedTo() == null ? null : value.getDateCreatedTo());
        dateModifiedFrom.setDateValue(value.getDateModifiedFrom() == null ? null : value.getDateModifiedFrom());
        dateModifiedTo.setDateValue(value.getDateModifiedTo() == null ? null : value.getDateModifiedTo());
        createdBy.setValue(value.getCreatedBy() == null ? "" : value.getCreatedBy());
        lastModifiedBy.setValue(value.getLastModifiedBy() == null ? "" : value.getLastModifiedBy());
        if (value.getOrigin() == ArtifactOriginEnum.any) {
            originAny.setValue(true);
        } else if (value.getOrigin() == ArtifactOriginEnum.derived) {
            originDerived.setValue(true);
        } else {
            originPrimary.setValue(true);
        }
        onFilterValueChange();
    }
}
