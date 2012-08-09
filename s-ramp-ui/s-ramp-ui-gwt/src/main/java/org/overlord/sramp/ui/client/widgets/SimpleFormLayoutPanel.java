/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.ui.client.widgets;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple implementation of a form panel.  This panel implementation provides a
 * mechanism to display a list of form controls within a simple tabular layout.  Each
 * form control must be associated with a text label.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimpleFormLayoutPanel extends CellPanel {
	
	/**
	 * Constructor.
	 */
	public SimpleFormLayoutPanel() {
	    DOM.setElementProperty(getTable(), "cellSpacing", "0");
	    DOM.setElementProperty(getTable(), "cellPadding", "0");
	    getElement().setClassName("simpleFormPanel");
	}

	/**
	 * Called to add a form field to the form panel.
	 * @param label
	 * @param child
	 */
	public void add(String label, Widget child) {
	    Element tr = DOM.createTR();
	    Element labelTd = DOM.createTD();
	    labelTd.setAttribute("align", "right");
	    Element widgetTd = DOM.createTD();
	    DOM.appendChild(tr, labelTd);
	    DOM.appendChild(tr, widgetTd);
	    DOM.appendChild(getBody(), tr);
	    
	    if (label != null) {
			InlineLabel l = new InlineLabel(label + ":");
			l.setStyleName("label");
		    add(l, labelTd);
	    }
	    add(child, widgetTd);
	}

	/**
	 * Called to add a form field to the form panel.  This call creates two new rows
	 * with colspan=2 on the td elements.  This is useful for TextAreas, for instance.
	 * @param label
	 * @param child
	 */
	public void addTwoCol(String label, Widget child) {
	    Element labelTr = DOM.createTR();
	    Element labelTd = DOM.createTD();
	    labelTd.setAttribute("colspan", "2");

	    Element widgetTr = DOM.createTR();
	    Element widgetTd = DOM.createTD();
	    widgetTd.setAttribute("colspan", "2");

	    DOM.appendChild(labelTr, labelTd);
	    DOM.appendChild(widgetTr, widgetTd);

	    DOM.appendChild(getBody(), labelTr);
	    DOM.appendChild(getBody(), widgetTr);
	    
	    if (label != null) {
			InlineLabel l = new InlineLabel(label + ":");
			l.setStyleName("label");
		    add(l, labelTd);
	    }
	    add(child, widgetTd);
	}

}
