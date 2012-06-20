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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.plugins.providers.atom.app.AppService;
import org.overlord.sramp.atom.models.CoreModel;

@Path("/s-ramp")
public class ServiceDocumentResource
{
    /**
     * S-RAMP implementations SHALL return an Atom Publishing Protocol Service
     * Document to clients who perform an HTTP GET on the following URL:
     * 
     * {base URL}/s-ramp/servicedocument
     * 
     * The content of the Service Document that is returned is defined as
     * follows:
     * <ul>
     * <li>MUST contain a workspace for each of the artifact models identified
     * in Section 3 of the SOA Repository Artifact Model & Protocol
     * Specification - Foundation Document.</li>
     * <li>Each workspace MUST contain an app:collection element for each of the
     * artifact types that are defined within the corresponding artifact model
     * for that workspace.</li>
     * <li>Each collection in a workspace MUST specify an atom:categories
     * element that will define the categories that MUST be applied to the
     * member resources of the collection as defined in Section 2.3.1.</li>
     * <li>The workspace for the query artifact model MUST contain an
     * app:collection element for each Stored Query that exists in the S-RAMP
     * implementation.</li>
     * </ul>
     * The workspace for the SOA or Service Implementation Artifact Model MUST
     * contain an app:collection element for each user defined type that has
     * been registered in the S-RAMP implementation.
     * 
     * @return AppService - service document.
     */
    @GET
    @Path("servicedocument")
    @Produces("application/atom+xml")
    public AppService getService() {
 
        AppService appService = new AppService();
        String hrefBase = getRawPath();
        
        appService.getWorkspace().add(CoreModel.getWorkSpace(hrefBase));

        
        return appService;
    }
    
    @Context
    private UriInfo uriInfo;
    
    public String getRawPath(){
      String rawPath = uriInfo.getBaseUri().getRawPath();
      if (rawPath==null) {
          rawPath = "http://localhost:8080/s-ramp-atom/";
      }
      return rawPath;
    }
  
}
