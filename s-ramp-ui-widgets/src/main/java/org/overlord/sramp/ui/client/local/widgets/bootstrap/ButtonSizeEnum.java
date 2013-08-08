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

/**
 * Enum for the different button sizes in bootstrap.
 *
 * @author eric.wittmann@redhat.com
 */
public enum ButtonSizeEnum {

    large("btn-large"), //$NON-NLS-1$
    normal(null),
    small("btn-small"), //$NON-NLS-1$
    mini("btn-mini"); //$NON-NLS-1$

    private String c;

    /**
     * Constructor.
     * @param c
     */
    private ButtonSizeEnum(String c) {
        this.c = c;
    }

    /**
     * The HTML class(es) to use for the button.
     */
    public String getButtonClasses() {
        String rval = "btn"; //$NON-NLS-1$
        if (this.c != null) {
            rval += " " + this.c; //$NON-NLS-1$
        }
        return rval;
    }
}
