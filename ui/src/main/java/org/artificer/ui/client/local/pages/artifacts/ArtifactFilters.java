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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.shared.beans.ArtifactFilterBean;
import org.artificer.ui.client.shared.beans.ArtifactOriginEnum;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.DateBox;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * The artifact filtersPanel sidebar.  Whenever the user changes any of the settings in
 * the filter sidebar, a ValueChangeEvent will be fired.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/artificer/ui/client/local/site/artifacts.html#sramp-filter-sidebar")
@Dependent
public class ArtifactFilters extends Composite implements HasValue<ArtifactFilterBean> {

    private ArtifactFilterBean currentState = new ArtifactFilterBean();

    @Inject
    protected ClientMessages i18n;

    @Inject @DataField
    protected TextBox keywords;

    // Artifact Type
    @Inject @DataField
    protected TextBox artifactType;

    @Inject @DataField
    protected TextBox uuid;
    @Inject @DataField
    protected TextBox name;

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
    protected ListBox originSelect;

    @Inject @DataField
    protected Button clearAllFilters;

    @Inject @DataField("classifier-filter-container")
    protected ClassifierFilterContainer classifierFilters;

    @Inject @DataField
    protected Button addCustomPropertyFilter;
    @Inject @DataField("filter-custom-properties")
    protected CustomPropertyFilters customPropertyFilters;
    @Inject
    protected Instance<AddPropertyFilterDialog> addPropertyFilterDialogFactory;

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
        ClickHandler clearFilterHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setValue(new ArtifactFilterBean(), true);
            }
        };
        clearAllFilters.addClickHandler(clearFilterHandler);
        @SuppressWarnings("rawtypes")
        ValueChangeHandler valueChangeHandler = new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                onFilterValueChange();
            }
        };
        ChangeHandler changeHandler = new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onFilterValueChange();
            }
        };
        keywords.addValueChangeHandler(valueChangeHandler);
        artifactType.addValueChangeHandler(valueChangeHandler);
        uuid.addValueChangeHandler(valueChangeHandler);
        name.addValueChangeHandler(valueChangeHandler);
        dateCreatedFrom.addValueChangeHandler(valueChangeHandler);
        dateCreatedTo.addValueChangeHandler(valueChangeHandler);
        dateModifiedFrom.addValueChangeHandler(valueChangeHandler);
        dateModifiedTo.addValueChangeHandler(valueChangeHandler);
        createdBy.addValueChangeHandler(valueChangeHandler);
        lastModifiedBy.addValueChangeHandler(valueChangeHandler);
        classifierFilters.addValueChangeHandler(valueChangeHandler);
        customPropertyFilters.addValueChangeHandler(valueChangeHandler);

        originSelect.addItem(i18n.format("artifacts.all"), ArtifactOriginEnum.ALL.name());
        originSelect.addItem(i18n.format("artifacts.primary-original"), ArtifactOriginEnum.PRIMARY_ORIGINAL.name());
        originSelect.addItem(i18n.format("artifacts.primary-expanded"), ArtifactOriginEnum.PRIMARY_EXPANDED.name());
        originSelect.addItem(i18n.format("artifacts.derived"), ArtifactOriginEnum.DERIVED.name());
        originSelect.addChangeHandler(changeHandler);
    }

    /**
     * Called whenever any filter value changes.
     */
    protected void onFilterValueChange() {
        ArtifactFilterBean newState = new ArtifactFilterBean();
        newState.setKeywords(keywords.getValue())
            .setArtifactType(artifactType.getValue())
            .setUuid(uuid.getValue())
            .setName(name.getValue())
            .setDateCreatedFrom(dateCreatedFrom.getDateValue())
            .setDateCreatedTo(dateCreatedTo.getDateValue())
            .setDateModifiedFrom(dateModifiedFrom.getDateValue())
            .setDateModifiedTo(dateModifiedTo.getDateValue())
            .setCreatedBy(createdBy.getValue())
            .setLastModifiedBy(lastModifiedBy.getValue())
            .setOrigin(ArtifactOriginEnum.valueOf(originSelect.getSelectedValue()))
            .setClassifiers(classifierFilters.getValue())
            .setCustomProperties(customPropertyFilters.getValue());

        ArtifactFilterBean oldState = this.currentState;
        this.currentState = newState;
        // Only fire a change event if something actually changed.
        ValueChangeEvent.fireIfNotEqual(this, oldState, currentState);
    }

    /**
     * Called when the Add Filter button is clicked (to add a custom property filter).
     * @param event
     */
    @EventHandler("addCustomPropertyFilter")
    protected void onAddPropertyFilter(ClickEvent event) {
        AddPropertyFilterDialog dialog = addPropertyFilterDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                customPropertyFilters.addPropertyFilter(event.getValue());
            }
        });
        dialog.show();
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
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(ArtifactFilterBean value, boolean fireEvents) {
        keywords.setValue(value.getKeywords() == null ? "" : value.getKeywords());
        artifactType.setValue(value.getArtifactType() == null ? "" : value.getArtifactType());
        uuid.setValue(value.getUuid() == null ? "" : value.getUuid());
        name.setValue(value.getName() == null ? "" : value.getName());
        dateCreatedFrom.setDateValue(value.getDateCreatedFrom() == null ? null : value.getDateCreatedFrom());
        dateCreatedTo.setDateValue(value.getDateCreatedTo() == null ? null : value.getDateCreatedTo());
        dateModifiedFrom.setDateValue(value.getDateModifiedFrom() == null ? null : value.getDateModifiedFrom());
        dateModifiedTo.setDateValue(value.getDateModifiedTo() == null ? null : value.getDateModifiedTo());
        createdBy.setValue(value.getCreatedBy() == null ? "" : value.getCreatedBy());
        switch (value.getOrigin()) {
            case ALL:
                originSelect.setSelectedIndex(0);
                break;
            case PRIMARY_ORIGINAL:
                originSelect.setSelectedIndex(1);
                break;
            case PRIMARY_EXPANDED:
                originSelect.setSelectedIndex(2);
                break;
            case DERIVED:
                originSelect.setSelectedIndex(3);
                break;
        }
        classifierFilters.setValue(value.getClassifiers());
        customPropertyFilters.setValue(value.getCustomProperties());
        ArtifactFilterBean oldState = this.currentState;
        currentState = value;
        if (fireEvents) {
        	ValueChangeEvent.fireIfNotEqual(this, oldState, currentState);
        }
    }

    /**
     * @param value the new filter settings
     */
    public void setValue(ArtifactFilterBean value) {
    	setValue(value, false);
    }

    /**
     * Refresh any data (e.g. ontology selectors) in the artifact filter panel.
     */
    public void refresh() {
        classifierFilters.refresh();
    }
}
