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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.local.services.NotificationService;
import org.artificer.ui.client.local.services.OntologyServiceCaller;
import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;
import org.artificer.ui.client.local.widgets.ontologies.LoadingAllOntologies;
import org.artificer.ui.client.local.widgets.ontologies.OntologyDropDown;
import org.artificer.ui.client.shared.beans.OntologyBean;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.ModalDialog;
import org.artificer.ui.client.local.widgets.ontologies.OntologySelectorWithToolbar;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Dialog that allows the user to edit a property value.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/artificer/ui/client/local/site/dialogs/modify-classifiers-dialog.html#modify-classifiers-dialog")
@Dependent
public class ModifyClassifiersDialog extends ModalDialog implements HasValue<List<String>>, IServiceInvocationHandler<List<OntologyBean>> {

    @Inject
    protected ClientMessages i18n;
    @Inject
    private OntologyServiceCaller ontologyServiceCaller;
    @Inject
    private NotificationService notificationService;

    @Inject @DataField("modify-classifiers-dialog-body")
    private FlowPanel body;
    @Inject @DataField("modify-classifiers-dialog-ontology-selector")
    private OntologyDropDown dropDown;
    @Inject
    private Instance<LoadingAllOntologies> loading;
    @Inject
    private Instance<OntologySelectorWithToolbar> selectorFactory;
    @Inject @DataField("modify-classifiers-dialog-btn-ok")
    private Button submitButton;

    private Set<String> originalValue;
    private List<String> value;
    private Map<String, OntologySelectorWithToolbar> selectors = new HashMap<String, OntologySelectorWithToolbar>();
    private OntologySelectorWithToolbar currentSelector;

    /**
     * Constructor.
     */
    public ModifyClassifiersDialog() {
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        submitButton.setEnabled(false);
    }

    /**
     * Called when the user changes the drop-down value.
     * @param event
     */
    @EventHandler("modify-classifiers-dialog-ontology-selector")
    public void onDropDownChange(ChangeEvent event) {
        if (currentSelector != null)
            currentSelector.setVisible(false);

        String selectedBase = dropDown.getSelection();
        OntologySelectorWithToolbar selector = selectors.get(selectedBase);
        if (selector != null) {
            selector.setVisible(true);
            selector.expandAll();
            currentSelector = selector;
        } else {
            currentSelector = null;
        }
    }

    /**
     * @see org.overlord.commons.gwt.client.local.widgets.ModalDialog#show()
     */
    @Override
    public void show() {
        // Add the 'please wait' spinner
        body.add(loading.get());
        // Download all the ontologies
        ontologyServiceCaller.getAll(false, this);
        super.show();
    }

    /**
     * Called when the user clicks the submit button.
     * @param event
     */
    @EventHandler("modify-classifiers-dialog-btn-ok")
    protected void onSubmit(ClickEvent event) {
        Set<String> newValue = new HashSet<String>();
        Set<Entry<String,OntologySelectorWithToolbar>> entrySet = selectors.entrySet();
        for (Entry<String, OntologySelectorWithToolbar> entry : entrySet) {
            String base = entry.getKey();
            OntologySelectorWithToolbar selector = entry.getValue();
            Set<String> selection = selector.getSelection();
            for (String id : selection) {
                String uri = base + "#" + id; //$NON-NLS-1$
                newValue.add(uri);
            }
        }
        this.value = new ArrayList<String>(newValue);
        if (!newValue.equals(this.originalValue)) {
            ValueChangeEvent.fire(this, this.value);
        }
        hide();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<String> getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<String> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<String> value, boolean fireEvents) {
        this.originalValue = new HashSet<String>(value);
        this.value = new ArrayList<String>(value);
        if (fireEvents) {
            ValueChangeEvent.fire(this, this.value);
        }
    }

    /**
     * @see org.artificer.ui.client.local.services.callback.IServiceInvocationHandler#onReturn(java.lang.Object)
     */
    @Override
    public void onReturn(List<OntologyBean> ontologies) {
        // Clear the loading spinner
        body.clear();
        // Set the options in the drop-down and add it to the dom.
        dropDown.setOptions(ontologies);
        body.add(dropDown);
        // Create all the classifier selector w/ toolbar widgets - one for each ontology
        for (OntologyBean ontologyBean : ontologies) {
            OntologySelectorWithToolbar selectorWithToolbar = selectorFactory.get();
            selectorWithToolbar.refresh(ontologyBean);
            selectorWithToolbar.setVisible(false);
            Set<String> selection = getValueFor(ontologyBean);
            selectorWithToolbar.setSelection(selection);
            body.add(selectorWithToolbar);
            selectors.put(ontologyBean.getBase(), selectorWithToolbar);
        }
        this.submitButton.setEnabled(true);
    }

    /**
     * @see org.artificer.ui.client.local.services.callback.IServiceInvocationHandler#onError(java.lang.Throwable)
     */
    @Override
    public void onError(Throwable error) {
        notificationService.sendErrorNotification(i18n.format("modify-classifiers-dialog.error"), error); //$NON-NLS-1$
    }

    /**
     * Gets the current value sliced by the given ontology.
     * @param ontologyBean
     */
    private Set<String> getValueFor(OntologyBean ontologyBean) {
        Set<String> items = new HashSet<String>();
        for (String classifier : this.value) {
            if (classifier.startsWith(ontologyBean.getBase() + "#")) { //$NON-NLS-1$
                items.add(classifier.substring(ontologyBean.getBase().length() + 1));
            }
        }
        return items;
    }

}
