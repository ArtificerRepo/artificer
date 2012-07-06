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
package org.overlord.sramp.maven.repo.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.overlord.sramp.maven.repo.servlets.MavenRepositoryHandlerFactory;

/**
 * Models a simple directory listing.  This model is used in many of the pages that show
 * simple directory listings of information.  For more information, see the javadoc in the
 * {@link MavenRepositoryHandlerFactory} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class DirectoryListing {
	
	private String mavenPath;
	private String urlPath;
	private List<DirectoryEntry> entries = new ArrayList<DirectoryEntry>();

	/**
	 * Constructor.
	 * @param mavenPath the Maven directory path
	 * @param urlPath the full URL path
	 */
	public DirectoryListing(String mavenPath, String urlPath) {
		this.mavenPath = mavenPath;
		this.urlPath = urlPath;
	}
	
	/**
	 * Adds a directory entry to the listing.
	 * @param name the directory name
	 */
	public void addDirectoryEntry(String name) {
		addDirectoryEntry(name, new Date());
	}

	/**
	 * Adds a directory entry to the listing.
	 * @param name the directory name
	 * @param lastModified when the directory was last modified
	 */
	public void addDirectoryEntry(String name, Date lastModified) {
		DirectoryEntry entry = new DirectoryEntry();
		entry.setDescription("");
		entry.setLastModified(lastModified);
		entry.setName(name);
		entry.setSize(-1);
		entry.setType(DirectoryEntryType.directory);
		
		getEntries().add(entry);
	}

	/**
	 * @return the path
	 */
	public String getMavenPath() {
		return mavenPath;
	}

	/**
	 * @return the entries
	 */
	public List<DirectoryEntry> getEntries() {
		return entries;
	}

	/**
	 * @return the fullPath
	 */
	public String getUrlPath() {
		return urlPath;
	}

	/**
	 * @param fullPath the fullPath to set
	 */
	public void setFullPath(String fullPath) {
		this.urlPath = fullPath;
	}
}
