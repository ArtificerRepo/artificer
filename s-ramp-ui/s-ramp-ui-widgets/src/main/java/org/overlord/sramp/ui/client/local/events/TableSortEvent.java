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
package org.overlord.sramp.ui.client.local.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Event fired by the sortable table widget when the user clicks on one of the
 * table headers (in order to specify a sort order).
 *
 * @author eric.wittmann@redhat.com
 */
public class TableSortEvent extends GwtEvent<TableSortEvent.Handler> {

	/**
	 * Handler for {@link TableSortEvent}.
	 */
	public static interface Handler extends EventHandler {

		/**
		 * Called when {@link TableSortEvent} is fired.
		 *
		 * @param event the {@link TableSortEvent} that was fired
		 */
		public void onTableSort(TableSortEvent event);
	}

	/**
	 * Indicates if a widget supports ok/cancel.
	 */
	public static interface HasTableSortHandlers extends HasHandlers {

		/**
		 * Adds a handler to the widget.
		 * @param handler
		 */
		public HandlerRegistration addTableSortHandler(Handler handler);

	}

	private static Type<Handler> TYPE;

	/**
	 * Fires the event.
	 *
	 * @param source
	 * @param columnId
	 * @param ascending
	 */
	public static TableSortEvent fire(HasHandlers source, String columnId, boolean ascending) {
		TableSortEvent event = new TableSortEvent(columnId, ascending);
		if (TYPE != null)
			source.fireEvent(event);
		return event;
	}

	/**
	 * Gets the type associated with this event.
	 *
	 * @return returns the handler type
	 */
	public static Type<Handler> getType() {
		if (TYPE == null) {
			TYPE = new Type<Handler>();
		}
		return TYPE;
	}

	private String columnId;
	private boolean ascending;

	/**
	 * Constructor.
	 * @param columnId
	 * @param ascending
	 */
	public TableSortEvent(String columnId, boolean ascending) {
	    this.columnId = columnId;
	    this.ascending = ascending;
	}

	/**
	 * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
	 */
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	/**
	 * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
	 */
	@Override
	protected void dispatch(Handler handler) {
		handler.onTableSort(this);
	}

    /**
     * @return the columnId
     */
    public String getHeaderId() {
        return columnId;
    }

    /**
     * @return the ascending
     */
    public boolean isAscending() {
        return ascending;
    }
}
