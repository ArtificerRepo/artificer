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
package org.overlord.sramp.ui.client.local.widgets.bootstrap;

import javax.inject.Inject;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Base class for all modal dialogs.  It is expected that this
 * will be subclassed and templated using Errai UI templates.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class ModalDialog extends Composite {

    @Inject
    private RootPanel rootPanel;
    private boolean destroyOnHide = true;

    /**
     * Constructor.
     */
    public ModalDialog() {
    }

    /**
     * @param flag
     */
    public void setDestroyOnHide(boolean flag) {
        this.destroyOnHide = flag;
    }

    /**
     * Displays the dialog.
     */
    public void show() {
        rootPanel.add(this);
        addHiddenHandler(getElement());
        modal(getElement());
    }

    /**
     * Hides/closes the dialog.
     */
    public void hide() {
    }

    /**
     * Hides/closes the dialog.
     * @param destroyOnHide false if the dialog should *not* be removed from the root panel
     */
    public void hide(boolean destroyOnHide) {
        this.destroyOnHide = destroyOnHide;
        hide(getElement());
    }

    /**
     * Remove this dialog from the root panel when it is hidden.
     */
    protected final void onHidden() {
        removeHiddenHandler(getElement());
        if (destroyOnHide)
            destroy();
        onDialogHidden();
    }

    /**
     * Called to remove the dialog from the root panel.
     */
    public void destroy() {
        rootPanel.remove(this);
    }

    /**
     * Subclasses can override this to do post-hide logic.
     */
    protected void onDialogHidden() {
    }

    /**
     * Display the dialog using the native Bootstrap function.
     * @param element
     */
    private static native final void modal(Element element) /*-{
        $wnd.jQuery(element).modal({backdrop:'static',keyboard:false});
    }-*/;

    /**
     * Display the dialog using the native Bootstrap function.
     * @param element
     */
    private static native final void hide(Element element) /*-{
        $wnd.jQuery(element).modal('hide');
    }-*/;

    /**
     * Connects to the Bootstrap "hidden" event.
     * @param element
     */
    private native final void addHiddenHandler(Element element) /*-{
        var dis = this;
        $wnd.jQuery(element).on('hidden', function () {
            try {
                dis.@org.overlord.sramp.ui.client.local.widgets.bootstrap.ModalDialog::onHidden()();
            } catch (e) {
                alert(e);
            }
        });
    }-*/;

    /**
     * Connects to the Bootstrap "hidden" event.
     * @param element
     */
    private native final void removeHiddenHandler(Element element) /*-{
        $wnd.jQuery(element).off('hidden');
    }-*/;

}
