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
package org.overlord.sramp.maven.repo.handlers;

import org.overlord.sramp.maven.repo.models.DirectoryListing;

/**
 * A maven repository request handler that will output the root directory listing, displaying
 * all of the S-RAMP Artifact Models.
 *
 * @author eric.wittmann@redhat.com
 */
public class RootHandler extends AbstractDirectoryListingHandler {

	/**
	 * Default constructor.
	 */
	public RootHandler() {
	}
	
	/**
	 * @see org.overlord.sramp.maven.repo.handlers.AbstractDirectoryListingHandler#generateDirectoryListing(org.overlord.sramp.maven.repo.models.DirectoryListing)
	 */
	@Override
	protected void generateDirectoryListing(DirectoryListing directoryListing) {
		directoryListing.addDirectoryEntry("core");
		directoryListing.addDirectoryEntry("xsd");
		directoryListing.addDirectoryEntry("policy");
		directoryListing.addDirectoryEntry("soapWsdl");
		directoryListing.addDirectoryEntry("wsdl");
		directoryListing.addDirectoryEntry("serviceImplementation");
		directoryListing.addDirectoryEntry("user");
		directoryListing.addDirectoryEntry("soa");
	}

}
