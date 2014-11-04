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
package org.overlord.sramp.ui.client.shared.beans;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Brett Meyer
 */
@Portable
public class ArtifactSearchBean implements Serializable {

    private static final long serialVersionUID = ArtifactSearchBean.class.hashCode();
    
    private ArtifactFilterBean filters;
    
    private String searchText;
    
    private int page;
    
    private String sortColumnId;
    
    private boolean sortAscending;

    /**
     * @return the filters
     */
    public ArtifactFilterBean getFilters() {
        return filters;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(ArtifactFilterBean filters) {
        this.filters = filters;
    }

    /**
     * @return the searchText
     */
    public String getSearchText() {
        return searchText;
    }

    /**
     * @param searchText the searchText to set
     */
    public void setSearchText(String searchText) {
        this.searchText = searchText;
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
     * @return the sortColumnId
     */
    public String getSortColumnId() {
        return sortColumnId;
    }

    /**
     * @param sortColumnId the sortColumnId to set
     */
    public void setSortColumnId(String sortColumnId) {
        this.sortColumnId = sortColumnId;
    }

    /**
     * @return the sortAscending
     */
    public boolean isSortAscending() {
        return sortAscending;
    }

    /**
     * @param sortAscending the sortAscending to set
     */
    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }
}
