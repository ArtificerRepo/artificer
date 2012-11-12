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
package org.overlord.sramp.ui.client.views;

import java.util.List;

import org.overlord.sramp.ui.client.activities.IBrowseActivity;
import org.overlord.sramp.ui.client.places.AbstractPagedPlace;
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.place.IPlaceService;
import org.overlord.sramp.ui.client.widgets.ArtifactSummaryPanel;
import org.overlord.sramp.ui.client.widgets.DataTable;
import org.overlord.sramp.ui.client.widgets.DataTableWithPager;
import org.overlord.sramp.ui.client.widgets.PlaceFilterPanel;
import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;
import org.overlord.sramp.ui.shared.types.ArtifactTypeFilter;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Concrete implementation of the browse view.
 *
 * @author eric.wittmann@redhat.com
 */
public class BrowseView extends AbstractView<IBrowseActivity> implements IBrowseView {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final String DEFAULT_ORDER_BY = "name";

	private ArtifactDataTable artifacts;
	private PlaceFilterPanel<BrowsePlace> typeFilterPanel;
	private TextBox nameFilterBox;
	private ArtifactSummaryPanel summaryPanel;
	private BrowsePlace currentPlace;

	/**
	 * Constructor.
	 */
	public BrowseView() {
		typeFilterPanel = new PlaceFilterPanel<BrowsePlace>(i18n().translate("views.browse.filter-panel.label")) {
			@Override
			protected boolean matches(BrowsePlace currentPlace, BrowsePlace targetPlace) {
				String currentTypeFilter = currentPlace.getTypeFilter();
				String targetTypeFilter = targetPlace.getTypeFilter();
				if (currentTypeFilter == targetTypeFilter)
					return true;
				else if (currentTypeFilter != null && currentTypeFilter.equals(targetTypeFilter))
					return true;
				return false;
			}
		};
		artifacts = createArtifactTable();
		nameFilterBox = new TextBox();
		nameFilterBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				BrowsePlace newPlace = currentPlace.clone();
				newPlace.setNameFilter(nameFilterBox.getValue());
				History.newItem(toHistoryToken(newPlace));
			}
		});
		summaryPanel = new ArtifactSummaryPanel();

		FlowPanel nameFilterPanel = new FlowPanel();
		nameFilterPanel.setStyleName("filterPanel");
		InlineLabel label = new InlineLabel("Filter by name:");
		label.setStyleName("label");
		nameFilterPanel.add(label);
		nameFilterPanel.add(nameFilterBox);

		VerticalPanel vpanel = new VerticalPanel();
		vpanel.add(typeFilterPanel);
		vpanel.add(nameFilterPanel);

		HorizontalPanel hpanel = new HorizontalPanel();
		hpanel.setWidth("100%");
		hpanel.add(vpanel);
		hpanel.add(artifacts);
		hpanel.add(summaryPanel);

		hpanel.setCellWidth(vpanel, "175px");
		hpanel.setCellWidth(summaryPanel, "300px");

		this.initWidget(hpanel);
	}

	/**
	 * Creates a history token for the given {@link Place}.
	 * @param place the {@link Place} to tokenize
	 * @return the place token
	 */
	private static String toHistoryToken(Place place) {
		if (place == null)
			return "";
		IPlaceService placeService = Services.getServices().getService(IPlaceService.class);
		return placeService.generatePlaceToken(place);
	}

	/**
	 * Called to configure the filters.
	 * @param currentPlace
	 */
	private void configureFilters(BrowsePlace currentPlace) {
		for (ArtifactTypeFilter filter : ArtifactTypeFilter.values()) {
			BrowsePlace p = currentPlace.clone();
			p.setPage(null);
			p.setTypeFilter(filter.getCode());
			this.typeFilterPanel.addFilterOption(i18n().translate(filter.getI18nKey()), p);
		}

		this.typeFilterPanel.setCurrentPlace(currentPlace);
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IPagedResultView#getDefaultPageSize()
	 */
	@Override
	public int getDefaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IPagedResultView#getDefaultOrderBy()
	 */
	@Override
	public String getDefaultOrderBy() {
		return DEFAULT_ORDER_BY;
	}

	/**
	 * Creates the table in which our list of artifacts will be displayed.
	 */
	private ArtifactDataTable createArtifactTable() {
		final ArtifactDataTable table = new ArtifactDataTable();
		table.setWidth("100%");
		table.setEmptyTableMessage(i18n().translate("views.browse.artifacts.no-artifacts"));
		table.setLoadingMessage(i18n().translate("views.browse.artifacts.loading-artifacts"));
		final SingleSelectionModel<ArtifactSummary> selectionModel = new SingleSelectionModel<ArtifactSummary>();
		selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				ArtifactSummary artifact = selectionModel.getSelectedObject();
				if (artifact == null)
					summaryPanel.reset();
				else
					summaryPanel.setValue(artifact);
			}
		});
		table.setSelectionModel(selectionModel);
		return table;
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IBrowseView#onQueryStarting(BrowsePlace)
	 */
	@Override
	public void onQueryStarting(BrowsePlace currentPlace) {
		configureFilters(currentPlace);
		this.currentPlace = currentPlace;
		this.artifacts.reset();
		this.nameFilterBox.setValue(currentPlace.getNameFilter());
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IBrowseView#onQueryComplete(java.util.List, boolean)
	 */
	@Override
	public void onQueryComplete(List<ArtifactSummary> artifacts, boolean hasMoreRows) {
		this.artifacts.setRowData(artifacts, this.currentPlace, getDefaultPageSize(), hasMoreRows);
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IBrowseView#onQueryFailed(org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException)
	 */
	@Override
	public void onQueryFailed(RemoteServiceException error) {
		this.artifacts.setRowData(null, null, -1, false);
		growl().growl(
				i18n().translate("views.browse.search-error.title"),
				i18n().translate("views.browse.search-error.message"),
				error);
	}

	/*
	 * Impl class for the artifact data table.
	 */
	private class ArtifactDataTable extends DataTableWithPager<ArtifactSummary> {

		/**
		 * Constructor.
		 */
		public ArtifactDataTable() {
		}

		/**
		 * @see org.overlord.sramp.ui.client.widgets.DataTableWithPager#createPagerPlace(org.overlord.sramp.ui.client.places.AbstractPagedPlace)
		 */
		@Override
		protected AbstractPagedPlace createPagerPlace(AbstractPagedPlace currentPlace) {
			BrowsePlace place = new BrowsePlace();
			place.setTypeFilter(((BrowsePlace)currentPlace).getTypeFilter());
			return place;
		}

		/**
		 * @see org.overlord.sramp.ui.client.widgets.DataTableWithPager#createColumns(org.overlord.sramp.ui.client.widgets.DataTable)
		 */
		@Override
		protected void createColumns(DataTable<ArtifactSummary> table) {
			// Name
			TextColumn<ArtifactSummary> name = new TextColumn<ArtifactSummary>() {
				@Override
				public String getValue(ArtifactSummary artifact) {
					return artifact.getName();
				}
			};
			name.setSortable(true);
			name.setDataStoreName("name");
			table.addColumn(name, i18n().translate("views.browse.table.name-column"));

			// Type
			TextColumn<ArtifactSummary> type = new TextColumn<ArtifactSummary>() {
				@Override
				public String getValue(ArtifactSummary artifact) {
					return artifact.getType();
				}
			};
			type.setSortable(false);
			type.setDataStoreName("type");
			table.addColumn(type, i18n().translate("views.browse.table.type-column"));

			// Updated On
			TextColumn<ArtifactSummary> updatedOn = new TextColumn<ArtifactSummary>() {
				@Override
				public String getValue(ArtifactSummary artifact) {
					return i18n().formatDateTime(artifact.getUpdatedOn());
				}
			};
			updatedOn.setSortable(true);
			updatedOn.setDataStoreName("updatedOn");
			table.addColumn(updatedOn, i18n().translate("views.browse.table.updatedOn-column"));
//			setColumnWidth(uuid, 20.0, Unit.PCT);
		}

		/**
		 * @see org.overlord.sramp.ui.client.widgets.DataTableWithPager#getDefaultOrderBy()
		 */
		@Override
		protected String getDefaultOrderBy() {
			return DEFAULT_ORDER_BY;
		}
	}
}
