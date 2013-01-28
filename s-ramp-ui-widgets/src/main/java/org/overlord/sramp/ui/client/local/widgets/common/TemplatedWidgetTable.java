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
package org.overlord.sramp.ui.client.local.widgets.common;

import javax.annotation.PostConstruct;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.HasHTML;

/**
 * Extends the {@link WidgetTable} and adds features that make it available
 * to be used in an Errai UI template.
 *
 * @author eric.wittmann@redhat.com
 */
public class TemplatedWidgetTable extends WidgetTable implements HasHTML {

    /**
     * Constructor.
     */
    public TemplatedWidgetTable() {
    }

    /**
     * @see org.overlord.sramp.ui.client.local.widgets.common.WidgetTable#init()
     */
    @Override
    protected void init() {
    }

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postContruct() {
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    doAttachInit();
                }
            }
        });
    }

    /**
     * Called when the table is attached.  This is here to better support the WidgetTable
     * in an Errai UI template.  This method will grab the thead and tbody from the
     * template.  In addition, it will remove all of the tr children from the tbody.
     */
    protected void doAttachInit() {
        NodeList<Node> nodes = getElement().getChildNodes();
        for (int j = 0; j < nodes.getLength(); j++) {
            Node item = nodes.getItem(j);
            if ("thead".equalsIgnoreCase(item.getNodeName())) {
                this.thead = item.cast();
                NodeList<Node> childNodes = this.thead.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node theadtr = childNodes.getItem(i);
                    if ("tr".equalsIgnoreCase(theadtr.getNodeName())) {
                        int thcount = 0;
                        NodeList<Node> nodeList = theadtr.getChildNodes();
                        for (int k = 0; k < nodeList.getLength(); k++) {
                            if ("th".equalsIgnoreCase(nodeList.getItem(k).getNodeName())) {
                                thcount++;
                            }
                        }
                        this.columnCount = thcount;
                    }
                }
            } else if ("tbody".equalsIgnoreCase(item.getNodeName())) {
                this.tbody = item.cast();
                removeAllChildNodes(this.tbody);
            }
        }
    }

    /**
     * Removes all child nodes from the given element.
     */
    protected void removeAllChildNodes(Node elem) {
        NodeList<Node> childNodes = elem.getChildNodes();
        for (int i = childNodes.getLength() - 1; i >= 0; i--) {
            elem.removeChild(childNodes.getItem(i));
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#getText()
     */
    @Override
    public String getText() {
        throw new RuntimeException("Not supported - marker interface only (for Errai UI Templating)");
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        throw new RuntimeException("Not supported - marker interface only (for Errai UI Templating)");
    }

    /**
     * @see com.google.gwt.user.client.ui.HasHTML#getHTML()
     */
    @Override
    public String getHTML() {
        throw new RuntimeException("Not supported - marker interface only (for Errai UI Templating)");
    }

    /**
     * @see com.google.gwt.user.client.ui.HasHTML#setHTML(java.lang.String)
     */
    @Override
    public void setHTML(String html) {
        throw new RuntimeException("Not supported - marker interface only (for Errai UI Templating)");
    }

}
