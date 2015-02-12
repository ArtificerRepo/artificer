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
package org.artificer.repository.jcr;

import java.net.URI;
import java.util.Collection;

import org.artificer.common.ArtificerException;


/**
 * This interface is used to assist with Classification related tasks.  For
 * example, it is used to verify and normalize the classifications on an artifact
 * during create and update of an artifact's meta data.  It's also used in the
 * query layer to resolve classifications to URIs from simple IDs (in the various
 * classification custom functions).
 *
 * @author eric.wittmann@redhat.com
 */
public interface ClassificationHelper {

	/**
	 * Resolves the single classified-by value into a URI.  If the classified-by value
	 * is already a URI, this method will simply return it.  If the classified-by value
	 * cannot be resolved using any of the ontologies known to the S-RAMP repository
	 * then an exception will be thrown.
	 * @param classifiedBy
	 * @throws org.artificer.common.ArtificerException
	 */
	public URI resolve(String classifiedBy) throws ArtificerException;

	/**
	 * Normalizes a single classification URI to the list of URIs matching itself and all
	 * of its ancestors within its ontology.
	 * @param classification
	 * @throws org.artificer.common.ArtificerException
	 */
	public Collection<URI> normalize(URI classification) throws ArtificerException;

	/**
	 * Resolves all of the "classified-by" values into full URIs.  Note that these
	 * may already be full URIs, in which case nothing will be done.  Note also that
	 * this method also serves to validate all of the values.  If one of the values
	 * cannot be resolved to an existing ontology, this method will throw.
	 * @param classifiedBy
     * @throws org.artificer.common.ArtificerException
	 */
	public Collection<URI> resolveAll(Collection<String> classifiedBy) throws ArtificerException;

	/**
	 * Normalizes all of the classification URIs.  In this context, normalizing the
	 * cassifications means walking up the ontology and including all ancestors along
	 * with the specified classifications themselves.
	 * @param classifications
     * @throws org.artificer.common.ArtificerException
	 */
	public Collection<URI> normalizeAll(Collection<URI> classifications) throws ArtificerException;

}
