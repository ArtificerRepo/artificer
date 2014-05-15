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
package org.overlord.sramp.server.services;

import java.io.IOException;
import java.util.Set;

import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.server.services.mvn.MavenArtifactWrapper;

/**
 * Interface that define the services associates to the maven repository facade
 * for s-ramp.
 *
 * @author David Virgil Naranjo
 */
public interface MavenRepositoryService {

    /**
     * Gets the artifact content.
     *
     * @param fileName
     *            the file name
     * @param groupId
     *            the group id
     * @param artifactId
     *            the artifact id
     * @param version
     *            the version
     * @return the artifact content
     * @throws SrampAtomException
     *             the sramp atom exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public MavenArtifactWrapper getArtifactContent(String fileName, String groupId,
            String artifactId,
            String version) throws SrampAtomException,
            IOException;

    /**
     * Gets the items.
     *
     * @param path
     *            the path
     * @return the items
     * @throws SrampAtomException
     *             the sramp atom exception
     */
    public Set<String> getItems(String path) throws SrampAtomException;

}
