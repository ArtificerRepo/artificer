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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
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
    private static Map<String,String> sortedPathEntryHintMapCache = null;
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

    /**
     * Tries to match an ArchiveType based on the content of the archive.
     * For example a Drools kiejar contains a META-INF/kmodule.xml entry,
     * which has a S-RAMP Model of KieJarArchive.
     *
     * @param InputStream resourceInputStream, gets consumed and closed.
     *
     * @return ArchiveInfo containing Table of Content and archiveType match.
     *
     * @throws ZipToSrampArchiveException
     */
    public static ArchiveInfo inspectArchive(InputStream resourceInputStream) throws ZipToSrampArchiveException {
    	try {
	    	String matchedType = null;
	    	if (sortedPathEntryHintMapCache==null) {
	    		sortedPathEntryHintMapCache = new LinkedHashMap<String,String>();
	    		List<TypeHintInfo> typeHintInfoList = new ArrayList<TypeHintInfo>();
	    		//loop over all the providers providers
	    		for (ZipToSrampArchiveProvider p : providers) {
	    			typeHintInfoList.add(p.getArchiveTypeHints());
	    		}
	    		//sort by priority
	    		Collections.sort(typeHintInfoList);
	    		for (TypeHintInfo typeHintInfo: typeHintInfoList) {
	    			for (String path: typeHintInfo.pathEntryHintMap.keySet()) {
	    				sortedPathEntryHintMapCache.put(path, typeHintInfo.pathEntryHintMap.get(path));
	    			}
	    		}
	    	}
			ZipInputStream zip = new ZipInputStream(resourceInputStream);
			ZipEntry entry;
			String toc = ""; //$NON-NLS-1$
			//get table of content
			while((entry = zip.getNextEntry()) != null) {
				toc += entry.getName() + "\n"; //$NON-NLS-1$
			}
			//in the order of priority match toc with path hints (case insensitive).
			String lowerCaseToc = toc.toLowerCase();
    		for (String path: sortedPathEntryHintMapCache.keySet()) {
    			if (lowerCaseToc.contains(path.toLowerCase())) {
    				matchedType = sortedPathEntryHintMapCache.get(path);
    				break;
    			}
    		}
    		
	    	return new ArchiveInfo(matchedType, toc);
	    } catch (IOException e) {
	    	throw new ZipToSrampArchiveException(e.getMessage(),e);
	    } finally {
	    	IOUtils.closeQuietly(resourceInputStream);
	    }
    }


}
