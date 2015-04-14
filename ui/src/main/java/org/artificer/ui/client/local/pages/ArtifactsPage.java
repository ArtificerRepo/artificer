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
package org.artificer.ui.client.local.pages;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import org.artificer.ui.client.local.ClientMessages;
import org.artificer.ui.client.local.pages.artifacts.ArtifactFilters;
import org.artificer.ui.client.local.pages.artifacts.ArtifactsTable;
import org.artificer.ui.client.local.pages.artifacts.CreateArtifactDialog;
import org.artificer.ui.client.local.pages.artifacts.ImportArtifactDialog;
import org.artificer.ui.client.local.services.ApplicationStateService;
import org.artificer.ui.client.local.services.ArtifactSearchServiceCaller;
import org.artificer.ui.client.local.services.ArtifactServiceCaller;
import org.artificer.ui.client.local.services.NotificationService;
import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;
import org.artificer.ui.client.local.util.IUploadCompletionHandler;
import org.artificer.ui.client.shared.beans.ArtifactFilterBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsBean;
import org.artificer.ui.client.shared.beans.ArtifactResultSetBean;
import org.artificer.ui.client.shared.beans.ArtifactSearchBean;
import org.artificer.ui.client.shared.beans.ArtifactSummaryBean;
import org.artificer.ui.client.shared.beans.NotificationBean;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.events.TableSortEvent;
import org.overlord.commons.gwt.client.local.widgets.HtmlSnippet;
import org.overlord.commons.gwt.client.local.widgets.Pager;
import org.overlord.commons.gwt.client.local.widgets.SortableTemplatedWidgetTable.SortColumn;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * The default "Artifacts" page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/artificer/ui/client/local/site/artifacts.html#page")
@Page(path="artifacts")
@Dependent
public class ArtifactsPage extends AbstractPage {

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected ArtifactSearchServiceCaller searchService;
    @Inject
    protected ArtifactServiceCaller artifactService;
    @Inject
    protected NotificationService notificationService;
    @Inject
    protected ApplicationStateService stateService;
    @Inject
    protected Navigation navigation;

    // Breadcrumbs
    @Inject @DataField("back-to-dashboard")
    TransitionAnchor<DashboardPage> backToDashboard;

    @Inject @DataField("sramp-filter-sidebar")
    protected ArtifactFilters filtersPanel;
    @Inject @DataField("sramp-search-box")
    protected TextBox searchBox;

    @Inject @DataField("btn-import")
    protected Button importDialogButton;
    @Inject @DataField("btn-create")
    protected Button createDialogButton;
    @Inject
    protected Instance<ImportArtifactDialog> importDialog;
    @Inject
    protected Instance<CreateArtifactDialog> createDialog;
    @Inject @DataField("btn-refresh")
    protected Button refreshButton;

    @Inject @DataField("sramp-artifacts-none")
    protected HtmlSnippet noDataMessage;
    @Inject @DataField("sramp-artifacts-searching")
    protected HtmlSnippet searchInProgressMessage;
    @Inject @DataField("sramp-artifacts-table")
    protected ArtifactsTable artifactsTable;

    @Inject @DataField("sramp-artifacts-pager")
    protected Pager pager;
    @DataField("sramp-artifacts-range-1")
    protected SpanElement rangeSpan1 = Document.get().createSpanElement();
    @DataField("sramp-artifacts-total-1")
    protected SpanElement totalSpan1 = Document.get().createSpanElement();
    @DataField("sramp-artifacts-range-2")
    protected SpanElement rangeSpan2 = Document.get().createSpanElement();
    @DataField("sramp-artifacts-total-2")
    protected SpanElement totalSpan2 = Document.get().createSpanElement();

    @Inject @DataField("add-relationship-instructions")
    protected InlineLabel addRelationshipInstructions;
    @Inject @DataField("add-relationship-save")
    protected Button addRelationshipSave;
    private List<String> addRelationshipTargets = new ArrayList<>();

    private int currentPage = 1;

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
                populateQueryBox();
            }
        });
        searchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                doArtifactSearch();
            }
        });
        pager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                doArtifactSearch(event.getValue());
            }
        });
        artifactsTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
                doArtifactSearch(currentPage);
            }
        });

        // Hide columns 2-5 when in mobile mode.
        artifactsTable.setColumnClasses(2, "desktop-only"); //$NON-NLS-1$
        artifactsTable.setColumnClasses(3, "desktop-only"); //$NON-NLS-1$
        artifactsTable.setColumnClasses(4, "desktop-only"); //$NON-NLS-1$
        artifactsTable.setColumnClasses(5, "desktop-only"); //$NON-NLS-1$

        this.rangeSpan1.setInnerText("?"); //$NON-NLS-1$
        this.rangeSpan2.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan1.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan2.setInnerText("?"); //$NON-NLS-1$

        if (stateService.inNewRelationshipMode()) {
            initRelationshipMode();
        }
    }

    private void initRelationshipMode() {
        // in "new relationship" mode -- hide import/create buttons, display instructions, display checkboxes,
        // and display a "create relationship" button
        importDialogButton.setVisible(false);
        createDialogButton.setVisible(false);
        addRelationshipInstructions.getElement().removeClassName("hide");
        addRelationshipInstructions.setText(i18n.format("artifacts.add-relationship-instructions",
                stateService.getNewRelationshipType()));
        addRelationshipSave.getElement().removeClassName("hide");
        addRelationshipSave.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ArtifactRelationshipsBean relationships = new ArtifactRelationshipsBean();
                for (String target : addRelationshipTargets) {
                    ArtifactRelationshipBean relationship = new ArtifactRelationshipBean();
                    relationship.setRelationshipType(stateService.getNewRelationshipType());
                    relationship.setTargetUuid(target);
                    relationships.getRelationships().add(relationship);
                }

                final NotificationBean notificationBean = notificationService.startProgressNotification(
                        i18n.format("artifacts.adding-relationship"),
                        i18n.format("artifacts.adding-relationship-msg", stateService.getNewRelationshipType()));
                artifactService.addRelationships(stateService.getNewRelationshipSourceUuid(), relationships,
                        new IServiceInvocationHandler<Void>() {
                            @Override
                            public void onReturn(Void data) {
                                notificationService.completeProgressNotification(notificationBean.getUuid(),
                                        i18n.format("artifacts.add-relationship-success"),
                                        i18n.format("artifacts.add-relationship-success-msg", stateService.getNewRelationshipType()));
                                addRelationshipRedirect();
                            }

                            @Override
                            public void onError(Throwable error) {
                                notificationService.completeProgressNotification(notificationBean.getUuid(),
                                        i18n.format("artifacts.add-relationship-error"), error);
                                addRelationshipRedirect();
                            }
                        });
            }
        });
    }

    private void addRelationshipRedirect() {
        // navigate back to the artifact details page
        Multimap<String, String> multimap = ImmutableMultimap.of("uuid", stateService.getNewRelationshipSourceUuid());
        navigation.goTo(ArtifactDetailsPage.class, multimap);
    }

    /**
     * Event handler that fires when the user clicks the Import Artifacts button.
     * @param event
     */
    @EventHandler("btn-import")
    public void onImportClick(ClickEvent event) {
        ImportArtifactDialog dialog = importDialog.get();
        dialog.setCompletionHandler(new IUploadCompletionHandler() {
            @Override
            public void onImportComplete() {
                if (isAttached()) {
                    refreshButton.click();
                }
            }
        });
        dialog.show();
    }

    /**
     * Event handler that fires when the user clicks the Create Artifact button.
     * @param event
     */
    @EventHandler("btn-create")
    public void onCreateClick(ClickEvent event) {
        CreateArtifactDialog dialog = createDialog.get();
        dialog.setCompletionHandler(new IUploadCompletionHandler() {
            @Override
            public void onImportComplete() {
                if (isAttached()) {
                    refreshButton.click();
                }
            }
        });
        dialog.show();
    }

    /**
     * Event handler that fires when the user clicks the refresh button.
     * @param event
     */
    @EventHandler("btn-refresh")
    public void onRefreshClick(ClickEvent event) {
        doArtifactSearch(currentPage);
    }

    /**
     * Kick off an artifact search at this point so that we show some data in the UI.
     *
     * @see AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        ArtifactFilterBean filterBean = stateService.getArtifactsFilter();
        String searchText = stateService.getArtifactsSearchText();
        SortColumn sortColumn = stateService.getArtifactsSortColumn(artifactsTable.getDefaultSortColumn());

        this.filtersPanel.setValue(filterBean);
    	this.searchBox.setValue(searchText);
    	this.artifactsTable.sortBy(sortColumn.columnId, sortColumn.ascending);

        // Refresh the artifact filters
        filtersPanel.refresh();
    }

    @PageShown
    public void onPageShown() {
        Integer page = stateService.getArtifactsPage();
        // Kick off an artifact search
        doArtifactSearch(page);
    }

    // Theoretically, query generation could be done right here.  However, numerous conveniences are used (Calendar, etc.)
    // that are not supported by GWT emulation.  So, for now, offloading to the server-side services.
    protected void populateQueryBox() {
        final ArtifactFilterBean filterBean = filtersPanel.getValue();
        stateService.setArtifactsFilter(filterBean);

        searchService.query(filterBean, new IServiceInvocationHandler<String>() {
            @Override
            public void onReturn(String data) {
                searchBox.setValue(data);
                doArtifactSearch();
            }

            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("artifacts.error-searching"), error); //$NON-NLS-1$
                noDataMessage.setVisible(true);
                searchInProgressMessage.setVisible(false);
            }
        });
    }

    /**
     * Search for artifacts based on the current filter settings and search text.
     */
    protected void doArtifactSearch() {
        doArtifactSearch(1);
    }

    /**
     * Search for artifacts based on the current filter settings and search text.
     * @param page
     */
    protected void doArtifactSearch(int page) {
        onSearchStarting();
        currentPage = page;
		String searchText = this.searchBox.getValue();
        if (searchText == null || searchText.length() == 0) {
            searchText = "/s-ramp";
        }
        final SortColumn currentSortColumn = this.artifactsTable.getCurrentSortColumn();

        stateService.setArtifactsSearchText(searchText);
        stateService.setArtifactsPage(currentPage);
        stateService.setArtifactsSortColumn(currentSortColumn);
        
        ArtifactSearchBean searchBean = new ArtifactSearchBean();
        searchBean.setQueryText(searchText);
        searchBean.setPage(page);
        searchBean.setSortColumnId(currentSortColumn.columnId);
        searchBean.setSortAscending(currentSortColumn.ascending);

		searchService.search(searchBean, new IServiceInvocationHandler<ArtifactResultSetBean>() {
            @Override
            public void onReturn(ArtifactResultSetBean data) {
                updateArtifactTable(data);
                updatePager(data);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("artifacts.error-searching"), error); //$NON-NLS-1$
                noDataMessage.setVisible(true);
                searchInProgressMessage.setVisible(false);
            }
        });
    }

    /**
     * Called when a new artifact search is kicked off.
     */
    protected void onSearchStarting() {
        this.pager.setVisible(false);
        this.searchInProgressMessage.setVisible(true);
        this.artifactsTable.setVisible(false);
        this.noDataMessage.setVisible(false);
        this.rangeSpan1.setInnerText("?"); //$NON-NLS-1$
        this.rangeSpan2.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan1.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan2.setInnerText("?"); //$NON-NLS-1$
    }

    /**
     * Updates the table of artifacts with the given data.
     * @param data
     */
    protected void updateArtifactTable(ArtifactResultSetBean data) {
        this.artifactsTable.clear();
        this.searchInProgressMessage.setVisible(false);
        if (data.getArtifacts().size() > 0) {
            for (final ArtifactSummaryBean artifactSummaryBean : data.getArtifacts()) {
                // If in "new relationship" mode, add checkboxes to the last column.  Maintain the target list
                // based on the clicks.
                if (stateService.inNewRelationshipMode()) {
                    final CheckBox checkbox = new CheckBox();
                    boolean checked = addRelationshipTargets.contains(artifactSummaryBean.getUuid());
                    checkbox.setValue(checked);

                    checkbox.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            boolean checked = checkbox.getValue();
                            if (checked) {
                                addRelationshipTargets.add(artifactSummaryBean.getUuid());
                            } else {
                                addRelationshipTargets.remove(artifactSummaryBean.getUuid());
                            }
                        }
                    });

                    this.artifactsTable.addRow(artifactSummaryBean, checkbox);
                } else {
                    this.artifactsTable.addRow(artifactSummaryBean);
                }
            }
            this.artifactsTable.setVisible(true);
            // Important to set this here.  If you click on a search suggestion, filtersPanel's ValueChangeHandler
            // gets kicked off twice -- once due to the click on the suggestion popup and again when that replaces
            // the value in the field.  The first one sets noDataMessage.setVisible(true), below.  See SRAMP-557
            this.noDataMessage.setVisible(false);
        } else {
            this.noDataMessage.setVisible(true);
        }
    }

    /**
     * Updates the pager with the given data.
     * @param data
     */
    protected void updatePager(ArtifactResultSetBean data) {
        int numPages = ((int) (data.getTotalResults() / data.getItemsPerPage())) + (data.getTotalResults() % data.getItemsPerPage() == 0 ? 0 : 1);
        int thisPage = (data.getStartIndex() / data.getItemsPerPage()) + 1;
        this.pager.setNumPages(numPages);
        this.pager.setPage(thisPage);
        if (numPages > 1)
            this.pager.setVisible(true);

        int startIndex = 0;
        int endIndex = 0;
        if (data.getTotalResults() > 0) {
            startIndex = data.getStartIndex() + 1;
            endIndex = startIndex + data.getArtifacts().size() - 1;
        }
        String rangeText = startIndex + "-" + endIndex; //$NON-NLS-1$
        String totalText = String.valueOf(data.getTotalResults());
        this.rangeSpan1.setInnerText(rangeText);
        this.rangeSpan2.setInnerText(rangeText);
        this.totalSpan1.setInnerText(totalText);
        this.totalSpan2.setInnerText(totalText);
    }

}
