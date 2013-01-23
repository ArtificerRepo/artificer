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
package org.overlord.sramp.ui.client.local.pages;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;

import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for all pages, includes/handles the header and footer.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPage extends Composite {

    /*
     * Main browser header navigation links.
     */
    @Inject @DataField
    protected TransitionAnchor<ArtifactsPage> toArtifactsPage;
    @Inject @DataField
    protected TransitionAnchor<OntologiesPage> toOntologiesPage;
    @Inject @DataField
    protected TransitionAnchor<SettingsPage> toSettingsPage;

    /*
     * Mobile browser header navigation links.
     */
    @Inject @DataField
    protected TransitionAnchor<ArtifactsPage> toArtifactsPageMobile;
    @Inject @DataField
    protected TransitionAnchor<OntologiesPage> toOntologiesPageMobile;
    @Inject @DataField
    protected TransitionAnchor<SettingsPage> toSettingsPageMobile;

    /**
     * Called after the page is constructed.
     */
    @PostConstruct
    private void postConstruct() {

    }

}
