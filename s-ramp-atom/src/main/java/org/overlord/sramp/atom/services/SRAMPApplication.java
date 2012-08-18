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
package org.overlord.sramp.atom.services;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.overlord.sramp.atom.err.SrampAtomExceptionMapper;

/**
 * The SRAMP RESTEasy application.  This is essentially the main entry point into a 
 * RESTEasy application - it provides the resource implementaiton as well as any other
 * providers (mappers, etc).
 */
public class SRAMPApplication extends Application {
	
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> classes = new HashSet<Class<?>>();

	/**
	 * Constructor.
	 */
	public SRAMPApplication() {
		singletons.add(new EntryResource());
		singletons.add(new ServiceDocumentResource());
		singletons.add(new FeedResource());
		singletons.add(new XsdDocumentResource());
		singletons.add(new XmlDocumentResource());
		singletons.add(new AdHocQueryResource());
		
		classes.add(SrampAtomExceptionMapper.class);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
