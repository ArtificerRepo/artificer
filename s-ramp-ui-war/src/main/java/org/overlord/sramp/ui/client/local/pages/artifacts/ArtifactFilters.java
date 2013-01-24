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
import org.overlord.sramp.ui.client.local.widgets.common.RadioButton;
import org.overlord.sramp.ui.client.shared.ArtifactFilterBean;
import org.overlord.sramp.ui.client.shared.ArtifactOriginEnum;

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
 * The artifact filters sidebar.  Whenever the user changes any of the settings in
 * the filter sidebar, a ValueChangeEvent will be fired.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/artifacts.html#sramp-filter-sidebar")
@Dependent
public class ArtifactFilters extends Composite implements HasValueChangeHandlers<ArtifactFilterBean> {

    private ArtifactFilterBean prevState = null;

    // Artifact Type
    @Inject @DataField
    protected TextBox artifactType;

    // Date Created
    @Inject @DataField
    protected TextBox dateCreatedFrom;
    @Inject @DataField
    protected TextBox dateCreatedTo;

    // Date Modified
    @Inject @DataField
    protected TextBox dateModifiedFrom;
    @Inject @DataField
    protected TextBox dateModifiedTo;

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

    // Clear core filters
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
                artifactType.setValue("");
                dateCreatedFrom.setValue("");
                dateCreatedTo.setValue("");
                dateModifiedFrom.setValue("");
                dateModifiedTo.setValue("");
                createdBy.setValue("");
                lastModifiedBy.setValue("");
                originPrimary.setValue(true);
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
//            .setDateCreatedFrom(dateCreatedFrom.getValue())
//            .setDateCreatedTo(dateCreatedTo.getValue())
//            .setDateModifiedFrom(dateModifiedFrom.getValue())
//            .setDateModifiedTo(dateModifiedTo.getValue())
            .setCreatedBy(createdBy.getValue())
            .setLastModifiedBy(lastModifiedBy.getValue())
            .setOrigin(ArtifactOriginEnum.valueOf(originAny.getValue(), originPrimary.getValue(), originDerived.getValue()));

        ValueChangeEvent.fireIfNotEqual(this, this.prevState, newState);
        this.prevState = newState;
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ArtifactFilterBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
