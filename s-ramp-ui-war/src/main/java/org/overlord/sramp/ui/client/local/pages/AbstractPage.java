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

import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.overlord.sramp.ui.client.local.util.SrampJS;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.SplitButtonDropdown;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for all pages, includes/handles the header and footer.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPage extends Composite {

    @Inject
    protected ClientMessageBus bus;

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
    @Inject @DataField
    protected Anchor toLogoutMobile;

    /*
     * Desktop: User Menu (top-right on navbar)
     */
    @Inject @DataField
    protected SplitButtonDropdown navbarUserMenu;

    /*
     * Mobile: User Menu (slide-down)
     */
    @DataField
    protected LIElement userMenuMobileLabel = Document.get().createLIElement();

    /**
     * Constructor.
     */
    public AbstractPage() {
    }

    /**
     * Called after the page is constructed.
     */
    @PostConstruct
    private final void _onPostConstruct() {
        // Call the SRAMP javascript every time the page loads.
        SrampJS.onPageLoad();
        ClickHandler logoutClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onLogout();
                event.preventDefault();
            }
        };

        // Set the current user name in the header(s)
        navbarUserMenu.setLabel("Eric Wittmann");
        userMenuMobileLabel.setInnerText("Eric Wittmann");

        // Add the Logout option to the desktop navbar action dropdown
        navbarUserMenu.addOption("Logout", "logout").addClickHandler(logoutClickHandler);
        // Listen for mobile logout link clicks
        toLogoutMobile.addClickHandler(logoutClickHandler);
    }

    /**
     * Called when a page is about to be shown (either when the app is first loaded or
     * when navigating TO this page from another).
     */
    @PageShowing
    private final void _onPageShowing() {
        // Do initial page loading work, but do it as a post-init task
        // of the errai bus so that all RPC endpoints are ready.  This
        // is only necessary on initial app load, but it doesn't hurt
        // to always do it.
        CDI.addPostInitTask(new Runnable() {
            @Override
            public void run() {
                onPageShowing();
            }
        });
    }

    /**
     * Subclasses can implement this to do any work they need done when the page
     * is about to be shown.
     */
    protected void onPageShowing() {
    }

    /**
     * Handle the logic of logging out of the application.
     */
    protected void onLogout() {
        // TODO implement logout logic here
        Window.alert("Handle logout logic here! (TBD)");
    }

}
