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
package org.artificer.ui.client.local.widgets.ontologies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.artificer.ui.client.shared.beans.OntologyBean;
import org.artificer.ui.client.shared.beans.OntologyClassBean;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;

/**
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/artificer/ui/client/local/site/dialogs/classifier-dialog.html#ontology-selector-with-toolbar")
@Dependent
public class OntologySelectorWithToolbar extends Composite {

    @Inject @DataField("ontology-selector")
    private OntologySelectorNodePanel nodePanel;
    @Inject
    private Instance<OntologySelectorNode> nodeFactory;

    private Set<String> value;

    /**
     * Constructor.
     */
    public OntologySelectorWithToolbar() {
    }

    /**
     * @return the selector
     */
    public OntologySelectorNodePanel getNodePanel() {
        return nodePanel;
    }

    /**
     * @param ontology
     */
    public void refresh(OntologyBean ontology) {
        nodePanel.clear();
        List<OntologyClassBean> rootClasses = ontology.getRootClasses();
        for (OntologyClassBean ontologyClass : rootClasses) {
            createAndAddNode(ontologyClass, getNodePanel());
        }
    }

    /**
     * Method to create and add tree nodes.
     * @param ontologyClass
     * @param nodePanel
     */
    private void createAndAddNode(OntologyClassBean ontologyClass, OntologySelectorNodePanel nodePanel) {
        OntologySelectorNode node = nodeFactory.get();
        String label = ontologyClass.getId();
        if (ontologyClass.getLabel() != null && ontologyClass.getLabel().trim().length() > 0) {
            label = ontologyClass.getLabel();
        }
        node.setLabel(label);
        node.setClassId(ontologyClass.getId());
        List<OntologyClassBean> children = ontologyClass.getChildren();
        for (OntologyClassBean childClass : children) {
            createAndAddNode(childClass, node);
        }
        nodePanel.add(node);
    }

    /**
     * Method to create and add tree nodes.
     * @param ontologyClass
     * @param parentNode
     */
    private void createAndAddNode(OntologyClassBean ontologyClass, OntologySelectorNode parentNode) {
        OntologySelectorNode node = nodeFactory.get();
        String label = ontologyClass.getId();
        if (ontologyClass.getLabel() != null && ontologyClass.getLabel().trim().length() > 0) {
            label = ontologyClass.getLabel();
        }
        node.setLabel(label);
        node.setClassId(ontologyClass.getId());
        List<OntologyClassBean> children = ontologyClass.getChildren();
        for (OntologyClassBean childClass : children) {
            createAndAddNode(childClass, node);
        }
        parentNode.addChild(node);
    }

    /**
     * Native JS to gather up the values of all the nodes in the dialog.
     * @param element
     */
    private native final void getCheckedNodes() /*-{
        var element = this.@org.artificer.ui.client.local.widgets.ontologies.OntologySelectorWithToolbar::getElement()();
        var dis = this;
        $wnd.jQuery(element).find(':checkbox').each(function() {
            if ($wnd.jQuery(this).attr('checked')) {
                var name = $wnd.jQuery(this).attr('name');
                if (name) {
                    dis.@org.artificer.ui.client.local.widgets.ontologies.OntologySelectorWithToolbar::addValue(Ljava/lang/String;)(name);
                }
            }
        });
    }-*/;

    /**
     * Ensures that the given ontology class is checked in the tree.
     * @param className
     */
    public native final void ensureChecked(String className) /*-{
        var element = this.@org.artificer.ui.client.local.widgets.ontologies.OntologySelectorWithToolbar::getElement()();
        $wnd.jQuery(element).find(':checkbox').each(function() {
            if ($wnd.jQuery(this).attr('name') == className) {
                $wnd.jQuery(this).attr('checked', 'checked');
            }
        });
    }-*/;

    /**
     * Expands all nodes in the tree
     * @param className
     */
    public native final void expandAll() /*-{
        var element = this.@org.artificer.ui.client.local.widgets.ontologies.OntologySelectorWithToolbar::getElement()();
        $wnd.expandAllTreeNodes($wnd.jQuery(element));
    }-*/;

    /**
     * Collapses all nodes in the tree
     * @param className
     */
    public native final void collapseAll() /*-{
        var element = this.@org.artificer.ui.client.local.widgets.ontologies.OntologySelectorWithToolbar::getElement()();
        $wnd.collapseAllTreeNodes($wnd.jQuery(element));
    }-*/;

    /**
     * @param v
     */
    protected void addValue(String v) {
        this.value.add(v);
    }

    /**
     * @return the currently selected values
     */
    public Set<String> getSelection() {
        value = new HashSet<String>();
        getCheckedNodes();
        return value;
    }

    /**
     * Sets the currently selected items in the tree.
     * @param value
     */
    public void setSelection(Set<String> value) {
        // Should we be de-selecting all first?
        if (value == null) {
            this.value = null;
        } else {
            this.value = new HashSet<String>(value);
            if (value != null && !value.isEmpty()) {
                for (String oclass : value) {
                    ensureChecked(oclass);
                }
            }
        }
    }

}
