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
package org.artificer.repository.hibernate.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

/**
 * @author Brett Meyer.
 */
@Entity
@Table(appliesTo = "StoredQuery", indexes = {
        @Index(name = "storedquery_name_idx", columnNames = "queryName")})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@javax.persistence.Table(name = "StoredQuery")
public class ArtificerStoredQuery implements Serializable {

    private String queryName;

    private String queryExpression;

    private List<String> propertyNames = new ArrayList<>();

    @Id
    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryExpression() {
        return queryExpression;
    }

    public void setQueryExpression(String queryExpression) {
        this.queryExpression = queryExpression;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "StoredQuery_propertyNames", joinColumns = @JoinColumn(name = "StoredQuery_queryName"))
    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }
}
