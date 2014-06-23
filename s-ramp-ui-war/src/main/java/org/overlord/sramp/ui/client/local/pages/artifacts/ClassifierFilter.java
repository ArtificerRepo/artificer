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

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.ClientMessages;
import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A widget that allows the user to select the classifiers she would like to
 * filter by.  There is one of these for each ontology on the server.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/artifacts.html#classifier-filter")
@Dependent
public class ClassifierFilter extends Composite implements HasClickHandlers, HasValue<Set<String>> {

    @Inject
    protected ClientMessages i18n;

    @Inject @DataField("classifier-filter-anchor")
    private Anchor anchor;
    @Inject @DataField("classifier-filter-label")
    private InlineLabel label;
    @Inject @DataField("number-selected-label")
    private InlineLabel numSelected;
    @Inject
    private Instance<ClassifierFilterSelectionDialog> dialogFactory;

    private OntologySummaryBean ontology;
    private Set<String> value;

    /**
     * Constructor.
     */
    public ClassifierFilter() {
    }

    /**
     * Sets the filter's label.
     * @param label
     */
    public void setLabel(String label) {
        this.label.setText(label);
    }

    /**
     * Sets the # selected information in the UI.
     * @param selected
     * @param outOf
     */
    public void setNumSelected(int selected) {
        this.numSelected.setText(i18n.format("classifier-filter.num-selected", String.valueOf(selected))); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return anchor.addClickHandler(handler);
    }

    /**
     * @param ontology the ontology to set
     */
    public void setOntology(OntologySummaryBean ontology) {
        this.ontology = ontology;
    }

    /**
     * Gets the base of the ontology associated with this filter.
     */
    public String getOntologyBase() {
        return ontology.getBase();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public Set<String> getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Set<String> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(Set<String> value, boolean fireEvents) {
        if (value == null)
            value = new HashSet<String>();
        this.value = value;
    }

    /**
     * Called when the user clicks on the filter - shows the user a tree of classes
     * that they can choose from.
     * @param event
     */
    @EventHandler("classifier-filter-anchor")
    public void onClick(ClickEvent event) {
        ClassifierFilterSelectionDialog dialog = dialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<Set<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Set<String>> event) {
                doDialogOk(event.getValue());
            }
        });
        dialog.setValue(this.value);
        dialog.setOntology(ontology);
        dialog.show();
    }

    /**
     * Called when the user clicks OK on the dialog.
     * @param data
     */
    protected void doDialogOk(Set<String> data) {
        this.value = data;
        ValueChangeEvent.fire(this, data);
    }


}
