/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.server.core.api;

import java.util.List;

/**
 * @author Brett Meyer.
 */
public class PagedResult<T> {

    private final List<T> results;

    private final String query;

    private final int totalSize;

    private final int startIndex;

    private final int pageSize;

    private final String orderBy;

    private final boolean ascending;

    public PagedResult(List<T> results, String query, int totalSize, int startIndex, int pageSize, String orderBy, boolean ascending) {
        this.results = results;
        this.query = query;
        this.totalSize = totalSize;
        this.startIndex = startIndex;
        this.pageSize = pageSize;
        this.orderBy = orderBy;
        this.ascending = ascending;
    }

    public List<T> getResults() {
        return results;
    }

    public String getQuery() {
        return query;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public boolean isAscending() {
        return ascending;
    }
}
