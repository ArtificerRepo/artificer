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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Implements a simple unordered list (ul).
 *
 * @author eric.wittmann@redhat.com
 */
public class UnorderedListPanel extends Panel {
	
	private List<Widget> children = new ArrayList<Widget>();
	private Map<Widget, Element> wrapperMap = new HashMap<Widget, Element>();

	/**
	 * Constructor.
	 */
	public UnorderedListPanel() {
		setElement(Document.get().createULElement());
	}

	/**
	 * @see com.google.gwt.user.client.ui.Panel#add(com.google.gwt.user.client.ui.Widget)
	 */
	@Override
	public void add(Widget w) {
		if (w == null)
			throw new NullPointerException("Cannot add a null widget.");
		w.removeFromParent();
		children.add(w);
		Element li = Document.get().createLIElement().cast();
		wrapperMap.put(w, li);
		DOM.appendChild(li, w.getElement());
		DOM.appendChild(getElement(), li);
		adopt(w);
	}

	/**
	 * @see com.google.gwt.user.client.ui.Panel#clear()
	 */
	@Override
	public void clear() {
		List<Widget> childrenClone = new ArrayList<Widget>(this.children);
		for (Widget widget : childrenClone) {
			this.remove(widget);
		}
	}

	/**
	 * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
	 */
	@Override
	public Iterator<Widget> iterator() {
		return this.children.iterator();
	}

	/**
	 * @see com.google.gwt.user.client.ui.Panel#remove(com.google.gwt.user.client.ui.Widget)
	 */
	@Override
	public boolean remove(Widget w) {
		if (!this.children.contains(w))
			return false;
		orphan(w);
		Element liWrapper = this.wrapperMap.get(w);
		getElement().removeChild(liWrapper);
		this.children.remove(w);
		this.wrapperMap.remove(w);
		return true;
	}
	
}
