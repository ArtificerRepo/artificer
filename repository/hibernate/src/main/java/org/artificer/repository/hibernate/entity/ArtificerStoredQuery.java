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

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import javax.persistence.Cacheable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brett Meyer.
 */
@Entity
@Table(appliesTo = "ArtificerStoredQuery", indexes = {
        @Index(name = "storedquery_name_idx", columnNames = "queryName")})
@Cacheable
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
    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }
}
