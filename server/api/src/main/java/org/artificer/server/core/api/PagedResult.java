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
package org.artificer.server.core.api;

import org.artificer.repository.query.ArtificerQueryArgs;

import java.util.List;

/**
 * @author Brett Meyer.
 */
public class PagedResult<T> {

    private final List<T> results;

    private final String query;

    private final long totalSize;

    private final ArtificerQueryArgs args;

    public PagedResult(List<T> results, String query, long totalSize, ArtificerQueryArgs args) {
        this.results = results;
        this.query = query;
        this.totalSize = totalSize;
        this.args = args;
    }

    public List<T> getResults() {
        return results;
    }

    public String getQuery() {
        return query;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public int getPageSize() {
        return args.getCount();
    }

    public int getStartIndex() {
        return args.getStartIndex();
    }

    public String getOrderBy() {
        return args.getOrderBy();
    }

    public boolean isAscending() {
        return args.getOrderAscending();
    }
}
