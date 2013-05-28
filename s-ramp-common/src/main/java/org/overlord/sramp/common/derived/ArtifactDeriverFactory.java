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
package org.overlord.sramp.common.derived;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.io.FileUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampConstants;

/**
 * Factory used to create an {@link ArtifactDeriver} for a particular type of artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactDeriverFactory {

	private static Map<ArtifactTypeEnum, ArtifactDeriver> derivers = new HashMap<ArtifactTypeEnum, ArtifactDeriver>();
	private static Map<String, ArtifactDeriver> extendedDerivers = new HashMap<String, ArtifactDeriver>();
	static {
		loadBuiltInDerivers();
		loadExtendedDerivers();
	}

    /**
     * Loads the built-in artifact derivers.
     */
    private static void loadBuiltInDerivers() {
        derivers.put(ArtifactTypeEnum.XsdDocument, new XsdDeriver());
		derivers.put(ArtifactTypeEnum.WsdlDocument, new WsdlDeriver());
		derivers.put(ArtifactTypeEnum.PolicyDocument, new PolicyDeriver());
    }
	/**
     * Loads any extended derivers.  These can be contributed via the
     * standard Java service loading mechanism.
     */
    private static void loadExtendedDerivers() {
        // ultimately there will be two loaders - the current thread's context classloader
        // and a single JAR loader over all JARs in the 'sramp.derivers.customDir' directory
        Collection<ClassLoader> loaders = new LinkedList<ClassLoader>();
        loaders.add(Thread.currentThread().getContextClassLoader());

        // Allow users to provide a directory path where we will check for JARs that
        // contain DeriverProvider implementations.
        String customDeriverDirPath = System.getProperty(SrampConstants.SRAMP_CUSTOM_DERIVER_DIR);
        if (customDeriverDirPath != null && customDeriverDirPath.trim().length() > 0) {
            File directory = new File(customDeriverDirPath);
            if (directory.isDirectory()) {
                List<URL> jarURLs = new ArrayList<URL>();
                Collection<File> jarFiles = FileUtils.listFiles(directory, new String[] { "jar" }, false);
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

        // Now load all of the contributed DeriverProvider implementations
        for (ClassLoader loader : loaders) {
            for (DeriverProvider provider : ServiceLoader.load(DeriverProvider.class, loader)) {
                Map<String, ArtifactDeriver> derivers = provider.createArtifactDerivers();
                if (derivers != null && !derivers.isEmpty()) {
                    extendedDerivers.putAll(derivers);
                }
            }
        }
    }
    /**
     * Create a default (null) deriver that will be used when no deriver
     * is mapped.
     */
    private static ArtifactDeriver nullDeriver = new ArtifactDeriver() {
		@SuppressWarnings("unchecked")
		@Override
		public Collection<BaseArtifactType> derive(BaseArtifactType artifact, InputStream content) {
			return Collections.EMPTY_SET;
		}
		/**
		 * @see org.overlord.sramp.common.derived.ArtifactDeriver#link(org.overlord.sramp.common.derived.LinkerContext, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.util.Collection)
		 */
		@Override
		public void link(LinkerContext context, BaseArtifactType sourceArtifact,
		        Collection<BaseArtifactType> derivedArtifacts) {
		}
	};

	/**
	 * Creates an artifact deriver for a specific type of artifact.
	 * @param artifactType type of s-ramp artifact
	 * @return an artifact deriver
	 */
	public final static ArtifactDeriver createArtifactDeriver(ArtifactType artifactType) {
	    ArtifactDeriver deriver = null;
	    if (artifactType.isExtendedType()) {
	        deriver = extendedDerivers.get(artifactType.getExtendedType());
	    } else {
    		deriver = derivers.get(artifactType.getArtifactType());
	    }
        if (deriver == null) {
            deriver = nullDeriver;
        }
		return deriver;
	}

}
