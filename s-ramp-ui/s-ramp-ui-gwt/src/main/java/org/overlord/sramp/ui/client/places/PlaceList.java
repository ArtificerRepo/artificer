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
package org.overlord.sramp.ui.client.places;



/**
 * Lists of all the places in the application.
 *
 * @author eric.wittmann@redhat.com
 */
public class PlaceList {

	public static final Object [][] PLACES = {
		// Prefix, Tokenizer instance, Place class
		{ "/dashboard", new DashboardPlace.Tokenizer(), DashboardPlace.class }, 
		{ "/dashboard/browse", new BrowsePlace.Tokenizer(), BrowsePlace.class }, 
		{ "/dashboard/browse/artifact", new ArtifactPlace.Tokenizer(), ArtifactPlace.class },
	};
	
}
