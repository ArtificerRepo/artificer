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
import org.overlord.sramp.ui.client.widgets.DataTable;
import org.overlord.sramp.ui.client.widgets.DataTableWithPager;
import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
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

	private ArtifactDataTable artifacts;

	/**
	 * Constructor.
	 */
	public BrowseView() {
		Label filtersPanel = new Label("Filters Go Here (TBD)");
		Label summaryPanel = new Label("Artifact Summary Goes Here (TBD)");
		artifacts = createArtifactTable();

		HorizontalPanel hpanel = new HorizontalPanel();
		hpanel.setWidth("100%");
		hpanel.add(filtersPanel);
		hpanel.add(artifacts);
		hpanel.add(summaryPanel);

		hpanel.setCellWidth(filtersPanel, "200px");
		hpanel.setCellWidth(summaryPanel, "300px");

		this.initWidget(hpanel);
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.views.IBrowseView#getDefaultPageSize()
	 */
	@Override
	public int getDefaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}

	/**
	 * Creates the table in which our list of artifacts will be displayed.
	 */
	private ArtifactDataTable createArtifactTable() {
		ArtifactDataTable table = new ArtifactDataTable();
		table.setWidth("100%");
		table.setEmptyTableMessage(i18n().translate("browse.artifacts.no-artifacts"));
		table.setLoadingMessage(i18n().translate("browse.artifacts.loading-artifacts"));
		SingleSelectionModel<ArtifactSummary> selectionModel = new SingleSelectionModel<ArtifactSummary>();
		selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
			}
		});
		table.setSelectionModel(selectionModel);
		return table;
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IBrowseView#onQueryStarting()
	 */
	@Override
	public void onQueryStarting() {
		this.artifacts.reset();
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IBrowseView#onQueryComplete(java.util.List, int, boolean)
	 */
	@Override
	public void onQueryComplete(List<ArtifactSummary> artifacts, BrowsePlace place, boolean hasMoreRows) {
		this.artifacts.setRowData(artifacts, place, getDefaultPageSize(), hasMoreRows);
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IBrowseView#onQueryFailed(org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException)
	 */
	@Override
	public void onQueryFailed(RemoteServiceException error) {
		// TODO do something interesting with the error
	}
	
	/*
	 * Impl class for the artifact data table.
	 */
	private static class ArtifactDataTable extends DataTableWithPager<ArtifactSummary> {

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
			TextColumn<ArtifactSummary> name = new TextColumn<ArtifactSummary>() {
				@Override
				public String getValue(ArtifactSummary artifact) {
					return artifact.getName();
				}
			};
			name.setSortable(true);
			table.addColumn(name, "Artifact Name");
//			setColumnWidth(name, 15.0, Unit.PCT);
			
			TextColumn<ArtifactSummary> uuid = new TextColumn<ArtifactSummary>() {
				@Override
				public String getValue(ArtifactSummary artifact) {
					return artifact.getUuid();
				}
			};
			uuid.setSortable(false);
			table.addColumn(uuid, "UUID");
//			setColumnWidth(uuid, 20.0, Unit.PCT);
		}
	}
}
