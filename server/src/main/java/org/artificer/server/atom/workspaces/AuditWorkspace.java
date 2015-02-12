/*
 * Copyright 2011 JBoss Inc
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
package org.artificer.server.atom.workspaces;

import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.app.AppCategories;
import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.artificer.atom.MediaType;
import org.artificer.atom.ArtificerAtomConstants;

/**
 * Models the custom audit workspace.  This workspace is not defined by the
 * S-RAMP specification.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditWorkspace extends AbstractWorkspace {

    private static final long serialVersionUID = 1857987473764891457L;

    /**
	 * Constructor.
	 * @param hrefBase
	 */
	public AuditWorkspace(String hrefBase) {
		super(hrefBase, "Auditing Model"); //$NON-NLS-1$
	}

	@Override
	protected void configureWorkspace() {
		AppCollection collection = addCollection("/s-ramp/audit", "Audit Entries", MediaType.APPLICATION_AUDIT_ENTRY_XML); //$NON-NLS-1$ //$NON-NLS-2$

		AppCategories categories = new AppCategories();
		categories.setFixed(true);
		collection.getCategories().add(categories);

		Category category = new Category();
		category.setScheme(ArtificerAtomConstants.X_S_RAMP_TYPE_URN);
		category.setTerm("audit"); //$NON-NLS-1$
		category.setLabel("Audit"); //$NON-NLS-1$
		categories.getCategory().add(category);
	}
}
