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
package org.overlord.sramp.server.atom.workspaces;

import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.app.AppCategories;
import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.overlord.sramp.atom.SrampAtomConstants;

/**
 * Models the stored query workspace.
 *
 * @author Brett Meyer
 */
public class StoredQueryWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = 9119601241133543724L;

    /**
	 * Constructor.
	 * @param hrefBase
	 */
	public StoredQueryWorkspace(String hrefBase) {
		super(hrefBase, "Stored Query Model"); //$NON-NLS-1$
	}

	/**
	 * @see org.overlord.sramp.common.server.atom.workspaces.AbstractWorkspace#configureWorkspace()
	 */
	@Override
	protected void configureWorkspace() {
	    // TODO: What is the last arg?
        AppCollection collection = addCollection("/s-ramp/query", "Stored Queries", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		AppCategories categories = new AppCategories();
        categories.setFixed(true);
        collection.getCategories().add(categories);

        Category category = new Category();
        try {
            category.setScheme(SrampAtomConstants.URN_X_S_RAMP_2013_TYPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        category.setTerm("queries"); //$NON-NLS-1$
        category.setLabel("Stored Query Entries"); //$NON-NLS-1$
        categories.getCategory().add(category);
	}
}
