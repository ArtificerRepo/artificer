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

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Injectable class that represents any random snippet of HTML.  Useful for controlling
 * pieces of an Errai template (such as visibility).  This snippet expects to be applied
 * to a div when @Inject'd.  If you want it to represent some other element, you will need
 * to construct it and pass in the desired Element.
 *
 * @author eric.wittmann@redhat.com
 */
public class HtmlSnippet extends Widget implements HasHTML {

    /**
     * Constructor.
     */
    public HtmlSnippet() {
        setElement(Document.get().createDivElement());
    }

    /**
     * Constructor.
     * @param elem
     */
    public HtmlSnippet(com.google.gwt.user.client.Element elem) {
        setElement(elem);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#getText()
     */
    @Override
    public String getText() {
        // Using HasHTML as a marker interface - no impl necessary.
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        // Using HasHTML as a marker interface - no impl necessary.
    }

    /**
     * @see com.google.gwt.user.client.ui.HasHTML#getHTML()
     */
    @Override
    public String getHTML() {
        // Using HasHTML as a marker interface - no impl necessary.
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasHTML#setHTML(java.lang.String)
     */
    @Override
    public void setHTML(String html) {
        // Using HasHTML as a marker interface - no impl necessary.
    }

}
