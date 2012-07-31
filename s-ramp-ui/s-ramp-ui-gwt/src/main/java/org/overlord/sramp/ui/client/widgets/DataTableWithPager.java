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
package org.overlord.sramp.ui.client.widgets;

import java.util.List;

import org.overlord.sramp.ui.client.places.AbstractPagedPlace;
import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;
import org.overlord.sramp.ui.client.services.place.IPlaceService;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * A single widget that models a data table with a pager.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class DataTableWithPager<T> extends FlowPanel {

	private DataTable<T> table;
	private PlacePager pager;
	private AbstractPagedPlace currentPlace;

	/**
	 * Constructor.
	 */
	public DataTableWithPager() {
		table = new DataTable<T>(createKeyProvider());
		pager = new PlacePager();
		currentPlace = null;
		createColumns(table);
		configureTable(table);
		getElement().setClassName("dataTableWrapper");
		
		add(table);
		add(pager);
	}

	/**
	 * Called to configure the table.
	 * @param table
	 */
	protected void configureTable(DataTable<T> table) {
		table.getElement().setClassName("dataTable");
		table.setWidth("100%");
    	table.addColumnSortHandler(new Handler() {
			@Override
			public void onColumnSort(ColumnSortEvent event) {
				ColumnSortList columnSortList = event.getColumnSortList();
				ColumnSortInfo sortInfo = columnSortList.get(0);
				onSort(sortInfo.getColumn().getDataStoreName(), sortInfo.isAscending());
			}
		});
	}

	/**
	 * Called when the user clicks on a column header to sort by it.
	 * @param dataStoreName
	 * @param ascending
	 */
	protected void onSort(String dataStoreName, boolean ascending) {
		AbstractPagedPlace place = createPagerPlace(currentPlace);
		place.setPage(currentPlace.getPage());
		place.setPageSize(currentPlace.getPageSize());
		place.setOrderBy(dataStoreName);
		place.setAscending(ascending);
		Services.getServices().getService(IPlaceService.class).goTo(place);
	}

	/**
	 * Creates the data table's key provider.
	 */
	protected ProvidesKey<T> createKeyProvider() {
		return new ProvidesKey<T>() {
			@Override
			public Object getKey(T item) {
				return item;
			}
		};
	}

	/**
	 * Called to create the table columns.
	 * @param table
	 */
	protected abstract void createColumns(DataTable<T> table);

	/**
	 * @param message
	 */
	public void setLoadingMessage(String message) {
		this.table.setLoadingIndicator(new PleaseWait(message));
	}

	/**
	 * @param message
	 */
	public void setEmptyTableMessage(String message) {
		this.table.setEmptyTableWidget(new InlineLabel(message));
	}

	/**
	 * Sets the selection model on the table.
	 * @param selectionModel
	 */
	public void setSelectionModel(SingleSelectionModel<T> selectionModel) {
		this.table.setSelectionModel(selectionModel);
	}

	/**
	 * Resets the table to be ready to receive data.
	 */
	public void reset() {
		this.table.reset();
		this.pager.setVisible(false);
	}

	/**
	 * Sets the table's row data.
	 * @param rowData
	 * @param currentPlace
	 * @param defaultPageSize
	 * @param hasMoreRows
	 */
	public void setRowData(List<T> rowData, AbstractPagedPlace currentPlace, int defaultPageSize, boolean hasMoreRows) {
		AbstractPagedPlace prevPlace = null;
		AbstractPagedPlace nextPlace = null;
		if (currentPlace.getPage(0) > 0) {
			prevPlace = createPagerPlace(currentPlace);
			prevPlace.setPage(currentPlace.getPage(0) - 1);
			prevPlace.setPageSize(currentPlace.getPageSize());
			prevPlace.setOrderBy(currentPlace.getOrderBy());
			prevPlace.setAscending(currentPlace.isAscending());
		}
		if (hasMoreRows) {
			nextPlace = createPagerPlace(currentPlace);
			nextPlace.setPage(currentPlace.getPage(0) + 1);
			nextPlace.setPageSize(currentPlace.getPageSize());
			nextPlace.setOrderBy(currentPlace.getOrderBy());
			nextPlace.setAscending(currentPlace.isAscending());
		}
		int start = (currentPlace.getPage(0) * currentPlace.getPageSize(defaultPageSize)) + 1;
		int end = start + rowData.size();
		ILocalizationService i18n = Services.getServices().getService(ILocalizationService.class);
		String pagerMsg = i18n.translate("dataTable.pager.label", currentPlace.getPage(0) + 1, start, end);
		
		updateColumnSort(currentPlace);
		
		this.currentPlace = currentPlace;
		this.pager.init(prevPlace, nextPlace, pagerMsg);
		this.pager.setVisible(true);
		this.table.setRowData(rowData);
	}

	/**
	 * Called to update thet able's column sort info.
	 * @param currentPlace
	 */
	private void updateColumnSort(AbstractPagedPlace currentPlace) {
		String orderBy = currentPlace.getOrderBy();
		Boolean asc = currentPlace.isAscending();
		if (orderBy == null)
			orderBy = getDefaultOrderBy();
		if (orderBy == null)
			return;
		if (asc == null)
			asc = true;
		for (int idx = 0; idx < this.table.getColumnCount(); idx++) {
			Column<T, ?> column = this.table.getColumn(idx);
			if (column.isSortable() && orderBy.equals(column.getDataStoreName())) {
				ColumnSortInfo sortInfo = new ColumnSortInfo(column, asc);
				this.table.getColumnSortList().push(sortInfo);
			}
		}
	}

	/**
	 * Returns the default order by.  This string must match up to the 'data store name' of one
	 * of the previously created columns.
	 */
	protected abstract String getDefaultOrderBy();

	/**
	 * Factory method for creating a place.
	 * @param currentPlace
	 */
	protected abstract AbstractPagedPlace createPagerPlace(AbstractPagedPlace currentPlace);

}
