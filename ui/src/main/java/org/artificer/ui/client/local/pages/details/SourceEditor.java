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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple widget that wraps the ACE JavaScript source editor.
 *
 * @author eric.wittmann@redhat.com
 */
public class SourceEditor extends Widget implements HasValue<String> {

    private JavaScriptObject editor;

    /**
     * Constructor.
     */
    public SourceEditor() {
        setElement(Document.get().createDivElement());
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    initEditor();
                } else {
                    destroyEditor();
                }
            }
        });
    }

    /**
     * Initializes the ACE editor.
     */
    protected void initEditor() {
        String id = getElement().getId();
        initACE(id);
    }

    /**
     * Initializes the ACE editor.
     *
     * @param id
     * @return the ACE javascript object
     */
    public native void initACE(String id) /*-{
		var editor = $wnd.ace.edit(id);
		editor.setTheme("ace/theme/eclipse");
		editor.getSession().setMode("ace/mode/xml");
		editor.setReadOnly(true);
		this.@org.artificer.ui.client.local.pages.details.SourceEditor::editor = editor;
    }-*/;

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        // TODO: handle value change in SourceEditor
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public native String getValue() /*-{
        return this.@org.artificer.ui.client.local.pages.details.SourceEditor::editor.getValue();
    }-*/;

    /**
     * Sets the value in the editor.
     *
     * @param value
     */
    public native void setEditorValue(String value) /*-{
		this.@org.artificer.ui.client.local.pages.details.SourceEditor::editor.setValue(value, -1);
    }-*/;

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(String value) {
        setEditorValue(value);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(String value, boolean fireEvents) {
    }

    /**
     * Destroys the editor.
     */
    protected native void destroyEditor() /*-{
		this.@org.artificer.ui.client.local.pages.details.SourceEditor::editor.destroy();
    }-*/;

}
