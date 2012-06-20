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


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

@Path("/s-ramp")
public class EntryResource
{
    @GET
    @Path("entry")
    @Produces("application/atom+xml")
    public Entry getEntry() {
 
        Entry entry = new Entry();
        entry.setTitle("Hello World");
        Content content = new Content();
        Artifact artifact = new Artifact();
        XsdDocument xsdDocument = new XsdDocument();
        xsdDocument.setName("my.xsd");
        artifact.setXsdDocument(xsdDocument);
        
        content.setJAXBObject(artifact);
        entry.setContent(content);

        return entry;
    }
    
  
}
