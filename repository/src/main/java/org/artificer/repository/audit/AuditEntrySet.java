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
package org.artificer.repository.audit;

import org.artificer.repository.AbstractSet;
import org.jboss.downloads.artificer._2013.auditing.AuditEntry;

import java.util.List;

/**
 * A set of audit entries returned from the audit manager.
 *
 * @author eric.wittmann@redhat.com
 */
public interface AuditEntrySet extends Iterable<AuditEntry>, AbstractSet {

    /**
     * After the query has been executed, this is called by the REST/EJB service to return all results.
     */
    public List<AuditEntry> list() throws Exception;

    /**
     * After the query has been executed, this is called by the REST/EJB service to page the results.
     */
    public List<AuditEntry> pagedList(long startIndex, long endIndex) throws Exception;
}
