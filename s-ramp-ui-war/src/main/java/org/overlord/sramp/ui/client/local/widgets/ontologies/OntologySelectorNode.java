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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/dialogs/classifier-dialog.html#ontology-selector-node")
@Dependent
public class OntologySelectorNode extends Composite {

    private static int counter = 0;

    @Inject @DataField("ontology-selector-node-toggle-btn")
    private Button treeToggleButton;
    @Inject @DataField("ontology-selector-node-label")
    private InlineLabel label;
    @Inject @DataField("ontology-selector-node-checkbox")
    private CheckBox checkbox;
    @Inject @DataField("ontology-selector-node-childPanel")
    private OntologySelectorNodePanel childPanel;

    /**
     * Constructor.
     */
    public OntologySelectorNode() {
    }

    /**
     * Called after construction of the widget.
     */
    @PostConstruct
    public void onPostConstruct() {
        treeToggleButton.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        String id = "osnp_ul_" + counter++; //$NON-NLS-1$
        childPanel.getElement().setId(id);
        treeToggleButton.getElement().setAttribute("data-target", "#" + id); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label.setText(label);
    }

    /**
     * Sets the ontology class's ID as the name of the checkbox.
     * @param id
     */
    public void setClassId(String id) {
        this.checkbox.setName(id);
    }

    /**
     * Adds a child node.
     * @param node
     */
    public void addChild(OntologySelectorNode node) {
        if (childPanel.getWidgetCount() == 0)
            treeToggleButton.getElement().getStyle().setVisibility(Visibility.VISIBLE);
        childPanel.add(node);
    }

}
