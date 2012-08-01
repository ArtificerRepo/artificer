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
package org.overlord.sramp.ui.client.places;

import com.google.gwt.place.shared.Place;

/**
 * A {@link Place} that supports paginated results.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPagedPlace extends Place {
	private Integer page;
	private Integer pageSize;
	private String orderBy;
	private Boolean ascending;

	/**
	 * Constructor.
	 */
	public AbstractPagedPlace() {
		this(null, null, null, null);
	}
	
	/**
	 * Constructor.
	 * @param page
	 * @param pageSize
	 * @param orderBy
	 * @param ascending
	 */
	public AbstractPagedPlace(Integer page, Integer pageSize, String orderBy, Boolean ascending) {
		setPage(page);
		setPageSize(pageSize);
		setOrderBy(orderBy);
		setAscending(ascending);
	}

	/**
	 * @return the page
	 */
	public Integer getPage() {
		return page;
	}

	/**
	 * @return the page
	 */
	public Integer getPage(Integer defaultPage) {
		if (page == null)
			return defaultPage;
		return page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(Integer page) {
		this.page = page;
	}

	/**
	 * @return the pageSize
	 */
	public Integer getPageSize() {
		return pageSize;
	}

	/**
	 * @return the pageSize
	 */
	public Integer getPageSize(Integer defaultPageSize) {
		if (pageSize == null)
			return defaultPageSize;
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the orderBy
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * @return the orderBy
	 */
	public String getOrderBy(String defaultOrderBy) {
		if (orderBy == null)
			return defaultOrderBy;
		return orderBy;
	}

	/**
	 * @param orderBy the orderBy to set
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * @return the ascending
	 */
	public Boolean isAscending() {
		return ascending;
	}

	/**
	 * @return the ascending
	 */
	public Boolean isAscending(Boolean defaultAscending) {
		if (ascending == null)
			return defaultAscending;
		return ascending;
	}

	/**
	 * @param ascending the ascending to set
	 */
	public void setAscending(Boolean ascending) {
		this.ascending = ascending;
	}
}
