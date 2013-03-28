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
package org.overlord.sramp.atom.archive.jar;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Factory responsible for creating artifact meta data.
 *
 * @author eric.wittmann@redhat.com
 */
public interface MetaDataFactory {

    /**
     * Called prior to creating the s-ramp archive.  This is invoked once per
     * archive creation.
     * @param context
     */
    public void setContext(JarToSrampArchiveContext context);

	/**
	 * Creates the meta-data object (S-RAMP specific) for the given artifact that
	 * will be included in the S-RAMP archive being created.
	 * @param artifact
	 */
	public BaseArtifactType createMetaData(DiscoveredArtifact artifact);

}
