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
package org.overlord.sramp.atom.archive.expand.registry;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveException;
import org.overlord.sramp.common.ArtifactType;

/**
 * A classpath based registry for mapping artifact types to subclasses of {@link ZipToSrampArchive}.  This
 * allows specific integrations (e.g. switchyard) to provide their own artifact expanders for their own
 * types.  So the switchyard integration layer can provide a switchyard-specific extension of
 * {@link ZipToSrampArchive} for artifacts of type SwitchYardApplication.
 *
 * @author eric.wittmann@redhat.com
 */
public final class ZipToSrampArchiveRegistry {

    private static Map<ArtifactType, ZipToSrampArchiveProvider> providerCache = new HashMap<ArtifactType, ZipToSrampArchiveProvider>();
    private static List<ZipToSrampArchiveProvider> providers = new ArrayList<ZipToSrampArchiveProvider>();
    static {
        discoverProviders();
    }

    /**
     * Uses the Java {@link ServiceLoader} mechanism to find contributed providers.
     */
    private static void discoverProviders() {
        for (ZipToSrampArchiveProvider provider : ServiceLoader.load(ZipToSrampArchiveProvider.class)) {
            providers.add(provider);
        }
    }

    /**
     * Creates an expander for the given artifact type.
     * @param artifactType
     * @param zipStream
     */
    public static ZipToSrampArchive createExpander(ArtifactType artifactType, InputStream zipStream) throws ZipToSrampArchiveException {
        ZipToSrampArchiveProvider provider = getProvider(artifactType);
        if (provider == null) {
            return null;
        } else {
            return provider.createExtractor(artifactType, zipStream);
        }
    }

    /**
     * Creates an expander for the given artifact type.
     * @param artifactType
     * @param zipFile
     */
    public static ZipToSrampArchive createExpander(ArtifactType artifactType, File zipFile) throws ZipToSrampArchiveException {
        ZipToSrampArchiveProvider provider = getProvider(artifactType);
        if (provider == null) {
            return null;
        } else {
            return provider.createExtractor(artifactType, zipFile);
        }
    }

    /**
     * Returns true if an expander exists for the given type.
     * @param artifactType
     */
    public static boolean canExpand(ArtifactType artifactType) throws ZipToSrampArchiveException {
        ZipToSrampArchiveProvider provider = getProvider(artifactType);
        return provider != null;
    }

    /**
     * Gets a provider for the given artifact type.
     * @param artifactType
     */
    protected static ZipToSrampArchiveProvider getProvider(ArtifactType artifactType) {
        ZipToSrampArchiveProvider provider = null;
        if (providerCache.containsKey(artifactType)) {
            provider = providerCache.get(artifactType);
        } else {
            provider = null;
            for (ZipToSrampArchiveProvider p : providers) {
                if (p.accept(artifactType)) {
                    provider = p;
                    break;
                }
            }
            // Cache it either way (even if it's null)
            providerCache.put(artifactType, provider);
        }
        return provider;
    }

}
