/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.common.artifactbuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.commons.services.ServiceRegistryUtil;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.derived.ArtifactBuilderProviderAdapter;
import org.overlord.sramp.common.derived.DeriverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers all built-in and external {@link ArtifactBuilderProvider}s.
 * 
 * @author Brett Meyer
 */
public class ArtifactBuilderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactBuilderFactory.class);
    
    private static List<ArtifactBuilderProvider> builderProviders = new ArrayList<ArtifactBuilderProvider>();
	static {
		loadBuiltIn();
		loadExtended();
		loadDerivers();
	}

    /**
     * Loads the built-in artifact builder providers.
     */
    private static void loadBuiltIn() {
        builderProviders.add(new BuiltInArtifactBuilderProvider());
    }
    
	/**
     * Loads any extended builder providers.  These can be contributed via the
     * standard Java service loading mechanism.
     */
    private static void loadExtended() {
        // First load via the standard ServiceRegistry mechanism.
        Set<ArtifactBuilderProvider> extendedBuilderProviders = ServiceRegistryUtil.getServices(
                ArtifactBuilderProvider.class);
        builderProviders.addAll(extendedBuilderProviders);
        
        // Allow users to provide a directory path where we will check for JARs that
        // contain ArtifactBuilderProvider implementations.
        Collection<ClassLoader> loaders = new LinkedList<ClassLoader>();
        String customDirPath = System.getProperty(SrampConstants.SRAMP_CUSTOM_ARTIFACT_BUILDER_PROVIDER_DIR);
        if (customDirPath != null && customDirPath.trim().length() > 0) {
            File directory = new File(customDirPath);
            if (directory.isDirectory()) {
                List<URL> jarURLs = new ArrayList<URL>();
                Collection<File> jarFiles = FileUtils.listFiles(directory, new String[] { "jar" }, false); //$NON-NLS-1$
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
        // Now load all of these contributed ArtifactBuilderProvider implementations
        for (ClassLoader loader : loaders) {
            for (ArtifactBuilderProvider builderProvider : ServiceLoader.load(ArtifactBuilderProvider.class, loader)) {
                builderProviders.add(builderProvider);
            }
        }
    }
    
    /**
     * Temporarily load the old derivers.
     */
    @Deprecated
    private static void loadDerivers() {
        // First load via the standard ServiceRegistry mechanism.
        Set<DeriverProvider> deriverProviders = ServiceRegistryUtil.getServices(
                DeriverProvider.class);
        for (DeriverProvider deriverProvider : deriverProviders) {
            builderProviders.add(new ArtifactBuilderProviderAdapter(deriverProvider));
            LOGGER.warn("ArtifactDeriver and DeriverProvider have been deprecated and will be removed in a future release!  Please see ArtifactBuilder and ArtifactBuilderProvider!");
        }
        
        // Allow users to provide a directory path where we will check for JARs that
        // contain ArtifactBuilderProvider implementations.
        Collection<ClassLoader> loaders = new LinkedList<ClassLoader>();
        String customDirPath = System.getProperty(SrampConstants.SRAMP_CUSTOM_DERIVER_DIR);
        if (customDirPath != null && customDirPath.trim().length() > 0) {
            File directory = new File(customDirPath);
            if (directory.isDirectory()) {
                List<URL> jarURLs = new ArrayList<URL>();
                Collection<File> jarFiles = FileUtils.listFiles(directory, new String[] { "jar" }, false); //$NON-NLS-1$
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
        // Now load all of these contributed ArtifactBuilderProvider implementations
        for (ClassLoader loader : loaders) {
            for (DeriverProvider deriverProvider : ServiceLoader.load(DeriverProvider.class, loader)) {
                builderProviders.add(new ArtifactBuilderProviderAdapter(deriverProvider));
                LOGGER.warn("ArtifactDeriver and DeriverProvider have been deprecated and will be removed in a future release!  Please see ArtifactBuilder and ArtifactBuilderProvider!");
            }
        }
    }

	public final static List<ArtifactBuilder> createArtifactBuilders(BaseArtifactType primaryArtifact, byte[] content) {
	    List<ArtifactBuilder> builders = new ArrayList<ArtifactBuilder>();
	    for (ArtifactBuilderProvider builderProvider : builderProviders) {
	        builders.addAll(builderProvider.createArtifactBuilders(primaryArtifact, content));
	    }
	    return builders;
	}

}
