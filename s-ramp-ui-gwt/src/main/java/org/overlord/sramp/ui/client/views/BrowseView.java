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
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.client.widgets.DataTable;
import org.overlord.sramp.ui.client.widgets.PlacePager;
import org.overlord.sramp.ui.client.widgets.PleaseWait;
import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Concrete implementation of the browse view.
 *
 * @author eric.wittmann@redhat.com
 */
public class BrowseView extends AbstractView<IBrowseActivity> implements IBrowseView {

	private static final int DEFAULT_PAGE_SIZE = 15;

	private DataTable<ArtifactSummary> artifactTable;
	private PlacePager pager;

	/**
	 * Constructor.
	 */
	public BrowseView() {
		Label filtersPanel = new Label("Filters Go Here (TBD)");
		Label summaryPanel = new Label("Artifact Summary Goes Here (TBD)");
		pager = new PlacePager();

		HorizontalPanel hpanel = new HorizontalPanel();
		hpanel.setWidth("100%");
		hpanel.add(filtersPanel);
		FlowPanel centerPanel = new FlowPanel();
		artifactTable = createArtifactTable();
		centerPanel.add(artifactTable);
		centerPanel.add(pager);
		hpanel.add(centerPanel);
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
	private DataTable<ArtifactSummary> createArtifactTable() {
		ArtifactDataTable table = new ArtifactDataTable();
		table.setWidth("100%");
		table.setPageSize(40);
		table.setLoadingIndicator(new PleaseWait("Querying, please wait..."));
		table.setEmptyTableWidget(new InlineLabel("No artifacts found."));
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
		this.artifactTable.reset();
		this.pager.setVisible(false);
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IBrowseView#onQueryComplete(java.util.List, int, boolean)
	 */
	@Override
	public void onQueryComplete(List<ArtifactSummary> artifacts, BrowsePlace place, boolean hasMore) {
		this.artifactTable.setRowData(artifacts);
		Place prevPlace = null;
		Place nextPlace = null;
		if (place.getPage(0) > 0) {
			prevPlace = new BrowsePlace(place.getPage(0) - 1, place.getPageSize(), place.getTypeFilter());
		}
		if (hasMore) {
			nextPlace = new BrowsePlace(place.getPage(0) + 1, place.getPageSize(), place.getTypeFilter());
		}
		int start = (place.getPage(0) * place.getPageSize(getDefaultPageSize())) + 1;
		int end = start + artifacts.size();
		String pagerMsg = i18n().translate("browse.artifacts.pager.label", place.getPage(0)+1, start, end);
		this.pager.init(prevPlace, nextPlace, pagerMsg);
		this.pager.setVisible(true);
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IBrowseView#onQueryFailed(org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException)
	 */
	@Override
	public void onQueryFailed(RemoteServiceException error) {
		// TODO do something interesting with the error
	}

	/**
	 * The Artifact {@link DataTable}.
	 *
	 * @author eric.wittmann@redhat.com
	 */
	private static class ArtifactDataTable extends DataTable<ArtifactSummary> {
		/**
		 * Constructor.
		 */
		public ArtifactDataTable() {
			super(new ProvidesKey<ArtifactSummary>() {
				@Override
				public Object getKey(ArtifactSummary item) {
					return item;
				}
			});
			getElement().setClassName("dataTable");
			createColumns();
		}

		/**
		 * Creates the table columns.
		 */
		private void createColumns() {
			TextColumn<ArtifactSummary> name = new TextColumn<ArtifactSummary>() {
				@Override
				public String getValue(ArtifactSummary artifact) {
					return artifact.getName();
				}
			};
			name.setSortable(true);
			addColumn(name, "Artifact Name");
//			setColumnWidth(name, 15.0, Unit.PCT);
			
			TextColumn<ArtifactSummary> uuid = new TextColumn<ArtifactSummary>() {
				@Override
				public String getValue(ArtifactSummary artifact) {
					return artifact.getUuid();
				}
			};
			uuid.setSortable(false);
			addColumn(uuid, "UUID");
//			setColumnWidth(uuid, 20.0, Unit.PCT);
		}
		
	}
}
