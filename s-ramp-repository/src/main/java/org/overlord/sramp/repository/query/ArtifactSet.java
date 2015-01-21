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
package org.overlord.sramp.repository.query;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.repository.AbstractSet;

import java.util.List;

/**
 * A set of s-ramp artifacts returned when performing an S-RAMP query.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ArtifactSet extends Iterable<BaseArtifactType>, AbstractSet {

    /**
     * After the query has been executed, this is called by the REST/EJB service to return all results.
     */
    public List<BaseArtifactType> list() throws Exception;

    /**
     * After the query has been executed, this is called by the REST/EJB service to page the results.
     */
    public List<BaseArtifactType> pagedList(long startIndex, long endIndex) throws Exception;
}
