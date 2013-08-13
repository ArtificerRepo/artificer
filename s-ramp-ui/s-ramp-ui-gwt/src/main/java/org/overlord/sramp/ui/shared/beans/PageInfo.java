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
package org.overlord.sramp.ui.shared.beans;

import java.io.Serializable;

/**
 * Information about a single page in a search result.
 *
 * @author eric.wittmann@redhat.com
 */
public class PageInfo implements Serializable {
	
	private static final long serialVersionUID = -445935015284453781L;
	
	private int page;
	private int pageSize;
	private String orderBy;
	private boolean ascending;
	
	/**
	 * Constructor.
	 */
	public PageInfo() {
	}

	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the orderBy
	 */
	public String getOrderBy() {
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
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * @param ascending the ascending to set
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

}
