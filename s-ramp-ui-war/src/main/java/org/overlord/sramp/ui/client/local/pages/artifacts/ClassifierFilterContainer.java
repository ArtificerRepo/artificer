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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.overlord.sramp.ui.client.local.ClientMessages;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.services.OntologyServiceCaller;
import org.overlord.sramp.ui.client.local.services.callback.IServiceInvocationHandler;
import org.overlord.sramp.ui.client.local.widgets.ontologies.LoadingOntologies;
import org.overlord.sramp.ui.client.local.widgets.ontologies.NoOntologiesFound;
import org.overlord.sramp.ui.client.shared.beans.OntologyResultSetBean;
import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;

/**
 * A widget used to filter by classifiers.  This is a container for multiple
 * {@link ClassifierFilter} but also manages the lifecycle of pulling the
 * ontology data from the server.
 *
 * @author eric.wittmann@redhat.com
 */
public class ClassifierFilterContainer extends FlowPanel implements HasValue<Map<String, Set<String>>> {

    @Inject
    protected ClientMessages i18n;
    @Inject
    private OntologyServiceCaller ontologyServiceCaller;
    @Inject
    private NotificationService notificationService;
    @Inject
    private Instance<ClassifierFilter> classifierFilterFactory;
    @Inject
    private Instance<LoadingOntologies> loadingOntologiesFactory;
    @Inject
    private Instance<NoOntologiesFound> noOntologiesFoundFactory;

    private List<ClassifierFilter> filters = new ArrayList<ClassifierFilter>();


    private Map<String, Set<String>> value;

    /**
     * Constructor.
     */
    public ClassifierFilterContainer() {
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setElement(com.google.gwt.user.client.Element)
     */
    @Override
    protected void setElement(Element elem) {
        super.setElement(DOM.createElement("ul")); //$NON-NLS-1$
    }

    /**
     * Adds a classifier filter for a specific ontology.
     * @param ontologySummary
     */
    public void addClassifierFilterFor(final OntologySummaryBean ontologySummary) {
        ClassifierFilter classifierFilter = classifierFilterFactory.get();
        classifierFilter.addValueChangeHandler(new ValueChangeHandler<Set<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Set<String>> event) {
                onClassifierFilterValueChange(ontologySummary.getBase(), event.getValue());
            }
        });
        classifierFilter.setOntology(ontologySummary);
        classifierFilter.setLabel(ontologySummary.getLabel());
        filters.add(classifierFilter);
        add(classifierFilter);
        
        int numSelected = 0;
        if (getValue() != null) {
            Set<String> cfValue = getValue().get(ontologySummary.getBase());
            classifierFilter.setValue(cfValue);
            if (cfValue != null) {
                numSelected = cfValue.size();
            }
        }
        classifierFilter.setNumSelected(numSelected);
    }

    /**
     * Called when the value of one of the classifier filters changes.
     * @param base
     * @param newValue
     */
    protected void onClassifierFilterValueChange(String base, Set<String> newValue) {
        if (this.value == null) {
            this.value = new HashMap<String, Set<String>>();
        } else {
            this.value = new HashMap<String, Set<String>>(this.value);
        }
        this.value.put(base, newValue);
        ValueChangeEvent.fire(this, this.value);
        // Now update the UI to reflect the # selected
        ClassifierFilter classifierFilter = getClassifierFilter(base);
        if (classifierFilter != null) {
            classifierFilter.setNumSelected(newValue.size());
        }
    }

    /**
     * Returns the child widget for the given ontology.
     * @param base
     */
    private ClassifierFilter getClassifierFilter(String base) {
        for (ClassifierFilter cf : filters) {
            if (cf.getOntologyBase().equals(base)) {
                return cf;
            }
        }
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.FlowPanel#clear()
     */
    @Override
    public void clear() {
        super.clear();
    }

    /**
     * Refresh the classifier filter container from ontologies pulled down
     * from the server.
     */
    public void refresh() {
        clear();
        add(loadingOntologiesFactory.get());
        ontologyServiceCaller.list(false, new IServiceInvocationHandler<OntologyResultSetBean>() {
            @Override
            public void onReturn(OntologyResultSetBean data) {
                clear();
                if (data.getOntologies().isEmpty()) {
                    add(noOntologiesFoundFactory.get());
                } else {
                    for (OntologySummaryBean ontologySummaryBean : data.getOntologies()) {
                        addClassifierFilterFor(ontologySummaryBean);
                    }
                }
            }
            @Override
            public void onError(Throwable error) {
                clear();
                add(noOntologiesFoundFactory.get());
                if (error instanceof SrampUiException) {
                    notificationService.sendErrorNotification(i18n.format("classifier-filter-container.error"), (SrampUiException) error); //$NON-NLS-1$
                } else {
                    notificationService.sendErrorNotification(i18n.format("classifier-filter-container.error"), error.getMessage(), null); //$NON-NLS-1$
                }
            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Map<String, Set<String>>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public Map<String, Set<String>> getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Map<String, Set<String>> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(Map<String, Set<String>> value, boolean fireEvents) {
        if (value == null) {
            value = new HashMap<String, Set<String>>();
        }
        this.value = new HashMap<String, Set<String>>(value.size());
        for (String k : value.keySet()) {
            Set<String> v = value.get(k);
            this.value.put(k, new HashSet<String>(v));
        }

        // Now pass the new value through to the filter widgets (if they are present)
        for (ClassifierFilter classifierFilter : filters) {
            String base = classifierFilter.getOntologyBase();
            Set<String> cfValue = getValue().get(base);
            classifierFilter.setValue(cfValue);
            if (cfValue == null)
                classifierFilter.setNumSelected(0);
            else
                classifierFilter.setNumSelected(cfValue.size());
        }
    }
}
