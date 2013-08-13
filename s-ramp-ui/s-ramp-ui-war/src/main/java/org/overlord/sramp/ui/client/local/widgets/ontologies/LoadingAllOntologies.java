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

import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;

/**
 * "Loading all ontologies" spinner.
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/dialogs/modify-classifiers-dialog.html#modify-classifiers-dialog-spinner-all")
@Dependent
public class LoadingAllOntologies extends Composite {

    /**
     * Constructor.
     */
    public LoadingAllOntologies() {
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        getElement().removeClassName("hide"); //$NON-NLS-1$
    }
}
