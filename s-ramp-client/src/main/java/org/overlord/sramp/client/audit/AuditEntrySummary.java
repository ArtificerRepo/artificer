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
package org.overlord.sramp.client.audit;

import java.text.DateFormat;
import java.util.Date;

import org.jboss.resteasy.plugins.providers.atom.Entry;

/**
 * Models a summary of a single S-RAMP audit entry.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditEntrySummary {

	private Entry entry;

	/**
	 * Constructor.
	 * @param entry
	 */
	public AuditEntrySummary(Entry entry) {
		this.entry = entry;
	}

    /**
     * @return the uuid
     */
    public String getUuid() {
        return this.entry.getId().toString().replace("urn:uuid:", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return the type
     */
    public String getType() {
        return this.entry.getTitle();
    }

    /**
     * @return the who
     */
    public String getWho() {
        return this.entry.getAuthors().get(0).getName();
    }

    /**
     * @return the when
     */
    public Date getWhen() {
        return this.entry.getPublished();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        return String.format("%1$s by '%2$s' on %3$s at %4$s.", getType(), getWho(), //$NON-NLS-1$
                dateFormat.format(getWhen()), timeFormat.format(getWhen()));
    }
}
