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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Container for the children of a given ontology node (if it has any).
 * @author eric.wittmann@redhat.com
 */
public class OntologySelectorNodePanel extends FlowPanel {

    /**
     * Constructor.
     */
    public OntologySelectorNodePanel() {
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setElement(com.google.gwt.user.client.Element)
     */
    @Override
    protected void setElement(Element elem) {
        super.setElement(DOM.createElement("ul")); //$NON-NLS-1$
    }

    /**
     * @return the panel's ID
     */
    public String getPanelId() {
        return getElement().getId();
    }

}
