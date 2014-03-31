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
package org.overlord.sramp.integration.teiid.expand;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveException;
import org.overlord.sramp.atom.archive.expand.registry.TypeHintInfo;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.integration.teiid.model.Vdb;

/**
 * Provides a Teiid version of the {@link ZipToSrampArchive}.
 */
public final class VdbToSrampArchiveProvider implements ZipToSrampArchiveProvider {

	private static final Map<String, String> hintsMap;
	static {
		Map<String, String>aMap = new LinkedHashMap<String,String>();
	    aMap.put("META-INF/vdb.xml", Vdb.ARTIFACT_TYPE); //$NON-NLS-1$
	    hintsMap = Collections.unmodifiableMap(aMap);
	}
    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider#accept(org.overlord.sramp.common.ArtifactType)
     */
    @Override
    public boolean accept( final ArtifactType artifactType ) {
        return (artifactType.isExtendedType() && Vdb.ARTIFACT_TYPE.equals(artifactType.getExtendedType()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider#createExtractor(org.overlord.sramp.common.ArtifactType,
     *      java.io.File)
     */
    @Override
    public ZipToSrampArchive createExtractor( final ArtifactType artifactType,
                                              final File vdbFile ) throws ZipToSrampArchiveException {
        return new VdbToSrampArchive(vdbFile);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider#createExtractor(org.overlord.sramp.common.ArtifactType,
     *      java.io.InputStream)
     */
    @Override
    public ZipToSrampArchive createExtractor( final ArtifactType artifactType,
                                              final InputStream vdbStream ) throws ZipToSrampArchiveException {
        return new VdbToSrampArchive(vdbStream);
    }
    
    @Override
   	public TypeHintInfo getArchiveTypeHints() {
   		return new TypeHintInfo(30,hintsMap);
   	}

}
