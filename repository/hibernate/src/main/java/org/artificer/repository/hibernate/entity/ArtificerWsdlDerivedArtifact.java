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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brett Meyer.
 */
@Entity
@Indexed
@Analyzer(impl = StandardAnalyzer.class)
public class ArtificerWsdlDerivedArtifact extends ArtificerArtifact {

    private List<ArtificerWsdlDerivedArtifact> extension = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getExtension() {
        return extension;
    }

    public void setExtension(List<ArtificerWsdlDerivedArtifact> extension) {
        this.extension = extension;
    }
}
