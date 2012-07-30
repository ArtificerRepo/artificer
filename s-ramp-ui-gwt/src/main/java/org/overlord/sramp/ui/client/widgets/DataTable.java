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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.view.client.ProvidesKey;

/**
 * Extends the GWT {@link CellTable} to provide a custom look and feel.
 *
 * @author eric.wittmann@redhat.com
 */
public class DataTable<T> extends CellTable<T> {

	public static interface DataTableStyle extends CellTable.Style {}
	public static interface DataTableResources extends CellTable.Resources {
		@Source({CellTable.Style.DEFAULT_CSS, "DataTable.css"})
		DataTableStyle cellTableStyle();
	}
	private static final CellTable.Resources TABLE_RESOURCES = GWT.create(DataTableResources.class);

	/**
     * C'tor.
     */
    public DataTable() {
    	super(20, TABLE_RESOURCES, null);
    }

    /**
     * C'tor.
     *
     * @param providesKey
     */
    public DataTable(ProvidesKey<T> providesKey) {
    	super(20, TABLE_RESOURCES, providesKey);
    }

	/**
     * Called to reset the table and prepare it to receive
     * new data.
     */
    public void reset() {
    	this.setRowCount(0, true);

    	ScheduledCommand command = new ScheduledCommand() {
    		@Override
    		public void execute() {
    			onLoadingStateChanged(LoadingState.LOADING);
    		}
    	};
        Scheduler.get().scheduleFinally(command);
    }

}
