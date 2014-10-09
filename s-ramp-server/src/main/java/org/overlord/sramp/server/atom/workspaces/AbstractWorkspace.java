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
package org.overlord.sramp.server.atom.workspaces;

import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.app.AppAccept;
import org.jboss.resteasy.plugins.providers.atom.app.AppCategories;
import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.jboss.resteasy.plugins.providers.atom.app.AppWorkspace;
import org.overlord.sramp.atom.SrampAtomConstants;
import org.overlord.sramp.common.ArtifactTypeEnum;

/**
 * Base class for all workspaces.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractWorkspace extends AppWorkspace {

	private static final long serialVersionUID = 5198131343123300536L;

	private String hrefBase;

	/**
	 * Constructor.
	 * @param hrefBase
	 * @param title
	 */
	public AbstractWorkspace(String hrefBase, String title) {
		if (hrefBase.endsWith("/")) { //$NON-NLS-1$
			hrefBase = hrefBase.substring(0, hrefBase.length() - 1);
		}
		if (hrefBase.endsWith("/s-ramp")) { //$NON-NLS-1$
			hrefBase = hrefBase.substring(0, hrefBase.length() - 8);
		}
		this.hrefBase = hrefBase;
		setTitle(title);
		configureWorkspace();
	}

	/**
	 * Creates a full URL from a path fragment.
	 * @param path
	 */
	protected String url(String path) {
		return hrefBase + path;
	}

	/**
	 * Adds a single collection to the workspace and returns it.
	 * @param path
	 * @param title
	 * @param accept
	 */
	protected AppCollection addCollection(String path, String title, String accept) {
        AppCollection collection = new AppCollection(url(path), title);
        collection.getAccept().add(new AppAccept(accept));
        getCollection().add(collection);
        return collection;
	}
	
	protected Category addCategory(AppCollection collection, String term, String label) {
        AppCategories categories = null;
        if (collection.getCategories().size() > 0) {
            categories = collection.getCategories().get(0);
        } else {
            categories = new AppCategories();
            categories.setFixed(true);
            collection.getCategories().add(categories);
        }

        Category category = new Category();
        category.setScheme(SrampAtomConstants.X_S_RAMP_TYPE_URN);
        category.setTerm(term);
        category.setLabel(label);
        categories.getCategory().add(category);

        return category;
    }

	/**
	 * Adds a single category to the given collection.
	 * @param collection
	 * @param type
	 */
	protected Category addTypeCategory(AppCollection collection, ArtifactTypeEnum type) {
	    return addCategory(collection, type.getType(), type.getLabel());
	}

    /**
	 * Configures this workspace.
	 */
	protected abstract void configureWorkspace();

}
