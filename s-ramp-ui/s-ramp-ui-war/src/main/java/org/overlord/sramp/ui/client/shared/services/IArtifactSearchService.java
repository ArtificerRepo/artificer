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
import org.overlord.sramp.ui.client.shared.beans.ArtifactFilterBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactResultSetBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;

/**
 * Provides a way to search for artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
@Remote
public interface IArtifactSearchService {

    /**
     * Search for artifacts using the given filtersPanel and search text.
     * @param filtersPanel
     * @param searchText
     * @param page
     * @throws SrampUiException
     */
    public ArtifactResultSetBean search(ArtifactFilterBean filters, String searchText, int page) throws SrampUiException;

}
