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
package org.overlord.sramp.integration.artifactbuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactContent;

/**
 * @author Brett Meyer
 */
public abstract class AbstractArtifactBuilder implements ArtifactBuilder {
    
    private final List<BaseArtifactType> derivedArtifacts = new ArrayList<BaseArtifactType>();
    
    private BaseArtifactType primaryArtifact;
    
    private ArtifactContent artifactContent = null;
    
    @Override
    public ArtifactBuilder buildArtifacts(BaseArtifactType primaryArtifact, ArtifactContent artifactContent)
            throws IOException {
        this.primaryArtifact = primaryArtifact;
        this.artifactContent = artifactContent;
        
        return this;
    }

    @Override
    public Collection<BaseArtifactType> getDerivedArtifacts() {
        return derivedArtifacts;
    }
    
    protected BaseArtifactType getPrimaryArtifact() {
        return primaryArtifact;
    }
    
    /**
     * Since the build process is multi-step and parses the content multiple times, it's necessary to be given
     * the byte[] and provide on-demand ByteArrayInputStreams as needed.
     * 
     * @return InputStream
     */
    protected InputStream getContentStream() throws FileNotFoundException {
        return artifactContent.getInputStream();
    }
}
