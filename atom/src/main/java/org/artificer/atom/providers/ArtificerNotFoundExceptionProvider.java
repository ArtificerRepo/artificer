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
package org.artificer.atom.providers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.artificer.common.error.ArtificerNotFoundException;

/**
 * The spec requires that a 404 is returned when an artifact is not found.
 * 
 * @author Brett Meyer
 */
@Provider
public class ArtificerNotFoundExceptionProvider implements ExceptionMapper<ArtificerNotFoundException> {

    @Override
    public Response toResponse(ArtificerNotFoundException exception) {
        return Response.status(Status.NOT_FOUND).entity(exception.getMessage()).build();
    }

}
