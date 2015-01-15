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
package org.overlord.sramp.integration;

import org.apache.commons.io.FileUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.commons.services.ServiceRegistryUtil;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.integration.artifactbuilder.ArtifactBuilder;
import org.overlord.sramp.integration.artifactbuilder.ArtifactBuilderProvider;
import org.overlord.sramp.integration.artifactbuilder.BuiltInArtifactBuilderProvider;
import org.overlord.sramp.integration.artifacttypedetector.ArtifactTypeDetector;
import org.overlord.sramp.integration.artifacttypedetector.DefaultArtifactTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author Brett Meyer.
 */
public class ExtensionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionFactory.class);

    private static List<ArtifactTypeDetector> typeDetectors = new ArrayList<ArtifactTypeDetector>();

    private static List<ArtifactBuilderProvider> builderProviders = new ArrayList<ArtifactBuilderProvider>();

    static {
        loadBuiltIn();
        loadExtended();

        // sort by priority
        Collections.sort(typeDetectors, new ArtifactTypeDetectorComparator());
    }

    /**
     * Loads the built-in extensions.
     */
    private static void loadBuiltIn() {
        typeDetectors.add(new DefaultArtifactTypeDetector());
        builderProviders.add(new BuiltInArtifactBuilderProvider());
    }

    /**
     * Loads any custom extensions.  These can be contributed via the standard Java service loading mechanism
     * or through custom jars in the dir identified through sramp.extension.customDir
     */
    private static void loadExtended() {
        // First load via the standard ServiceRegistry mechanism.
        Set<ArtifactTypeDetector> extendedTypeDetectors = ServiceRegistryUtil.getServices(ArtifactTypeDetector.class);
        typeDetectors.addAll(extendedTypeDetectors);
        Set<ArtifactBuilderProvider> extendedBuilderProviders = ServiceRegistryUtil.getServices(
                ArtifactBuilderProvider.class);
        builderProviders.addAll(extendedBuilderProviders);

        // Allow users to provide a directory path where we will check for JARs that
        // contain extension implementations.
        Collection<ClassLoader> loaders = new LinkedList<ClassLoader>();
        String customDirPath = System.getProperty(SrampConstants.SRAMP_CUSTOM_EXTENSION_DIR);
        if (customDirPath != null && customDirPath.trim().length() > 0) {
            File directory = new File(customDirPath);
            if (directory.isDirectory()) {
                List<URL> jarURLs = new ArrayList<URL>();
                Collection<File> jarFiles = FileUtils.listFiles(directory, new String[]{"jar"}, false); //$NON-NLS-1$
                for (File jarFile : jarFiles) {
                    try {
                        URL jarUrl = jarFile.toURI().toURL();
                        jarURLs.add(jarUrl);
                    } catch (MalformedURLException e) {
                    }
                }
                URL[] urls = jarURLs.toArray(new URL[jarURLs.size()]);
                ClassLoader jarCL = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
                loaders.add(jarCL);
            }
        }
        for (ClassLoader loader : loaders) {
            for (ArtifactTypeDetector typeDetector : ServiceLoader.load(ArtifactTypeDetector.class, loader)) {
                typeDetectors.add(typeDetector);
            }
            for (ArtifactBuilderProvider builderProvider : ServiceLoader.load(ArtifactBuilderProvider.class, loader)) {
                builderProviders.add(builderProvider);
            }
        }
    }

    public static ArtifactType detect(ArtifactContent content) {
        for (ArtifactTypeDetector typeDetector : typeDetectors) {
            ArtifactType artifactType = typeDetector.detect(content);
            if (artifactType != null) {
                return artifactType;
            }
        }
        return null;
    }

    public static ArtifactType detect(ArtifactContent content, ArchiveContext archiveContext) {
        for (ArtifactTypeDetector typeDetector : typeDetectors) {
            ArtifactType artifactType = typeDetector.detect(content, archiveContext);
            if (artifactType != null) {
                return artifactType;
            }
        }
        return null;
    }

    public static boolean isArchive(ArtifactContent content) {
        for (ArtifactTypeDetector typeDetector : typeDetectors) {
            if (typeDetector.isArchive(content)) {
                return true;
            }
        }
        return false;
    }

    public static boolean allowExpansionFromArchive(ArtifactContent content, ArchiveContext archiveContext) {
        for (ArtifactTypeDetector typeDetector : typeDetectors) {
            if (typeDetector.allowExpansionFromArchive(content, archiveContext)) {
                return true;
            }
        }
        return false;
    }

    public static List<ArtifactBuilder> createArtifactBuilders(BaseArtifactType primaryArtifact,
            ArtifactContent artifactContent) {
        List<ArtifactBuilder> builders = new ArrayList<ArtifactBuilder>();
        for (ArtifactBuilderProvider builderProvider : builderProviders) {
            builders.addAll(builderProvider.createArtifactBuilders(primaryArtifact, artifactContent));
        }
        return builders;
    }

    private static class ArtifactTypeDetectorComparator implements Comparator<ArtifactTypeDetector> {
        @Override
        public int compare(ArtifactTypeDetector o1, ArtifactTypeDetector o2) {
            Integer p1 = o1.getPriority();
            Integer p2 = o2.getPriority();
            // higest value first
            return p2.compareTo(p1);
        }
    }

}
