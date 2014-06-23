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
package org.overlord.sramp.ui.client.local.widgets.ontologies;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.shared.beans.OntologyBean;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Drop-down for choosing an ontology.
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/dialogs/modify-classifiers-dialog.html#modify-classifiers-dialog-ontology-selector")
@Dependent
public class OntologyDropDown extends Composite implements HasChangeHandlers {

    @Inject @DataField("ontology-select")
    private ListBox selector;

    /**
     * Constructor.
     */
    public OntologyDropDown() {
    }

    /**
     * Sets the options for the drop-down based on the ontologies in the list.
     * @param ontologies
     */
    public void setOptions(List<OntologyBean> ontologies) {
        selector.clear();
        selector.addItem("", ""); //$NON-NLS-1$ //$NON-NLS-2$
        for (OntologyBean ontology : ontologies) {
            selector.addItem(ontology.getId(), ontology.getBase());
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.HasChangeHandlers#addChangeHandler(com.google.gwt.event.dom.client.ChangeHandler)
     */
    @Override
    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return selector.addChangeHandler(handler);
    }

    /**
     * @return the currently selected option's value
     */
    public String getSelection() {
        int selectedIndex = selector.getSelectedIndex();
        if (selectedIndex == 0) {
            return null;
        } else {
            return selector.getValue(selectedIndex);
        }
    }
}
