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
package org.overlord.sramp.ui.client.local.pages;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.pages.artifacts.ArtifactFilters;
import org.overlord.sramp.ui.client.local.pages.artifacts.ArtifactsTable;
import org.overlord.sramp.ui.client.local.services.ArtifactSearchRpcService;
import org.overlord.sramp.ui.client.local.services.IRpcServiceInvocationHandler;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;
import org.overlord.sramp.ui.client.shared.beans.ArtifactFilterBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactSummaryBean;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The default "Artifacts" page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/artifacts.html#page")
@Page(path="artifacts", startingPage=true)
@Dependent
public class ArtifactsPage extends AbstractPage {

    @Inject
    protected ArtifactSearchRpcService searchService;

    @Inject @DataField("sramp-filter-sidebar")
    protected ArtifactFilters filtersPanel;
    @Inject @DataField("sramp-search-box")
    protected TextBox searchBox;

    @Inject @DataField("sramp-artifacts-none")
    protected HtmlSnippet noDataMessage;
    @Inject @DataField("sramp-artifacts-searching")
    protected HtmlSnippet searchInProgressMessage;
    @Inject @DataField("sramp-artifacts-table")
    protected ArtifactsTable artifactsTable;

    @Inject
    protected ClientMessageBus bus;

    /**
     * Constructor.
     */
    public ArtifactsPage() {
    }

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
        filtersPanel.addValueChangeHandler(new ValueChangeHandler<ArtifactFilterBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<ArtifactFilterBean> event) {
                doArtifactSearch();
            }
        });
        searchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                doArtifactSearch();
            }
        });
        // Hide columns 2-5 when in mobile mode.
        artifactsTable.setColumnClasses(2, "desktop-only");
        artifactsTable.setColumnClasses(3, "desktop-only");
        artifactsTable.setColumnClasses(4, "desktop-only");
        artifactsTable.setColumnClasses(5, "desktop-only");
        onSearchStarting();
    }

    @PageShown
    protected void onPageShown() {
        // Kick off an artifact search, but do it as a post-init task
        // of the errai bus so that the RPC endpoints are ready.  This
        // is only necessary on initial app load, but it doesn't hurt
        // to always do it.
        bus.addPostInitTask(new Runnable() {
            @Override
            public void run() {
                doArtifactSearch();
            }
        });
    }

    /**
     * Search for artifacts based on the current filter settings and search text.
     */
    protected void doArtifactSearch() {
        onSearchStarting();
        searchService.search(filtersPanel.getValue(), this.searchBox.getValue(), new IRpcServiceInvocationHandler<List<ArtifactSummaryBean>>() {
            @Override
            public void onReturn(List<ArtifactSummaryBean> data) {
                updateArtifactTable(data);
            }
            @Override
            public void onError(Throwable error) {
                Window.alert("Error finding artifacts: " + error.getMessage());
//                artifactsTable.setVisible(false);
            }
        });
    }

    /**
     * Called when a new artifact search is kicked off.
     */
    protected void onSearchStarting() {
        this.searchInProgressMessage.setVisible(true);
        this.artifactsTable.setVisible(false);
        this.noDataMessage.setVisible(false);
    }

    /**
     * Updates the table of artifacts with the given data.
     * @param data
     */
    protected void updateArtifactTable(List<ArtifactSummaryBean> data) {
        this.artifactsTable.clear();
        this.searchInProgressMessage.setVisible(false);
        if (data.size() > 0) {
            for (ArtifactSummaryBean artifactSummaryBean : data) {
                this.artifactsTable.addRow(artifactSummaryBean);
            }
            this.artifactsTable.setVisible(true);
        } else {
            this.noDataMessage.setVisible(true);
        }
    }

}
