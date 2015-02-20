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
package org.artificer.shell;

import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ontology.OntologySummary;
import org.artificer.client.query.QueryResultSet;
import org.jboss.aesh.console.AeshContext;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.io.FileResource;
import org.jboss.aesh.io.Resource;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.util.List;

/**
 * @author Brett Meyer.
 */
public class ArtificerContext implements AeshContext {

    private Resource cwd;

    private ArtificerAtomApiClient client;
    private BaseArtifactType currentArtifact;
    private QueryResultSet currentArtifactFeed;
    private List<OntologySummary> currentOntologyFeed;
    private ArtificerArchive currentArchive;

    public ArtificerContext() {
        cwd = new FileResource("").newInstance(Config.getUserDir());
    }

    @Override
    public Resource getCurrentWorkingDirectory() {
        return cwd;
    }

    @Override
    public void setCurrentWorkingDirectory(Resource cwd) {
        if(!cwd.isLeaf())
            this.cwd = cwd;
        else
            throw new IllegalArgumentException("Current working directory must be a directory");
    }

    public ArtificerAtomApiClient getClient() {
        return client;
    }

    public void setClient(ArtificerAtomApiClient client) {
        this.client = client;
    }

    public QueryResultSet getCurrentArtifactFeed() {
        return currentArtifactFeed;
    }

    public void setCurrentArtifactFeed(QueryResultSet currentArtifactFeed) {
        this.currentArtifactFeed = currentArtifactFeed;
    }

    public BaseArtifactType getCurrentArtifact() {
        return currentArtifact;
    }

    public void setCurrentArtifact(BaseArtifactType currentArtifact) {
        this.currentArtifact = currentArtifact;
    }

    public List<OntologySummary> getCurrentOntologyFeed() {
        return currentOntologyFeed;
    }

    public void setCurrentOntologyFeed(List<OntologySummary> currentOntologyFeed) {
        this.currentOntologyFeed = currentOntologyFeed;
    }

    public ArtificerArchive getCurrentArchive() {
        return currentArchive;
    }

    public void setCurrentArchive(ArtificerArchive currentArchive) {
        this.currentArchive = currentArchive;
    }
}
