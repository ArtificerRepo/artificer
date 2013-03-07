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
package org.overlord.sramp.ui.client.shared.services;

import org.jboss.errai.bus.server.annotations.Remote;
import org.overlord.sramp.ui.client.shared.beans.ArtifactBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;

/**
 * Provides a way to get and set Artifact meta data.
 *
 * @author eric.wittmann@redhat.com
 */
@Remote
public interface IArtifactService {

    /**
     * Gets the full meta data for an artifact (by UUID).
     * @param uuid
     * @throws SrampUiException
     */
    public ArtifactBean get(String uuid) throws SrampUiException;

    /**
     * Gets the full document content for an artifact (by UUID).
     * @param uuid
     * @param artifactType
     * @throws SrampUiException
     */
    public String getDocumentContent(String uuid, String artifactType) throws SrampUiException;

    /**
     * Called to update the given artifact bean.
     * @param artifact
     * @throws SrampUiException
     */
    public void update(ArtifactBean artifact) throws SrampUiException;

}
