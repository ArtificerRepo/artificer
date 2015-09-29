/*
 * Copyright 2013 JBoss Inc
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
package org.artificer.ui.client.shared.beans;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Brett Meyer
 */
@Portable
public class RelationshipGraphBean implements Serializable {

    private static final long serialVersionUID = RelationshipGraphBean.class.hashCode();

    private List<RelationshipGraphNodeBean> graph = new ArrayList<>();

    public List<RelationshipGraphNodeBean> getGraph() {
        return graph;
    }

    public void setGraph(List<RelationshipGraphNodeBean> graph) {
        this.graph = graph;
    }

    public void add(ArtifactSummaryBean artifact, ArtifactRelationshipsIndexBean relationships) {
        RelationshipGraphNodeBean graphNode = new RelationshipGraphNodeBean();
        graphNode.setArtifact(artifact);
        graphNode.setRelationships(relationships);
        graph.add(graphNode);
    }

}
