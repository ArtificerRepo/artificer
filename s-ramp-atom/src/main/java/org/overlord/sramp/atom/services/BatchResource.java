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
package org.overlord.sramp.atom.services;

import java.io.InputStream;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.beans.HttpResponseBean;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * The JAX-RS resource that handles pushing artifacts into the repository in batches.  The
 * S-RAMP specification defines two mechanisms for this.  The first is via an archive file
 * and the second is via a multipart/related http POST.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("/s-ramp")
public class BatchResource {

	//private static final MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

	/**
	 * Constructor.
	 */
	public BatchResource() {
	}

    /**
     * S-RAMP atom POST of a package file (.zip) containing the artifacts and meta data that
     * should be published in the repository.
     * @param fileName the name of the .zip file (optional)
     * @param content the zip content
     * @return a multipart/mixed response as defined in the S-RAMP Atom binding document
     * @throws SrampAtomException
     */
    @POST
    @Consumes(MediaType.APPLICATION_ZIP)
    @Produces(MediaType.MULTIPART_MIXED)
    @PartType("message/http")
	public MultipartOutput zipPackage(@HeaderParam("Slug") String fileName, InputStream content) throws SrampAtomException {
        InputStream is = content;
    	SrampArchive archive = null;
        try {
        	archive = new SrampArchive(content);
        } catch (Exception e) {
			throw new SrampAtomException(e);
        } finally {
        	IOUtils.closeQuietly(is);
        	if (archive != null)
        		SrampArchive.closeQuietly(archive);
        }

        MultipartOutput output = new MultipartOutput();
        output.setBoundary("package");

        HttpResponseBean resp1 = new HttpResponseBean(201, "Created");
        resp1.setBody(createEntry("hello-world.xsd"), MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        output.addPart(resp1, MediaType.MESSAGE_HTTP_TYPE).getHeaders().putSingle("Content-ID", "<schemas/PO.xsd@package>");

        HttpResponseBean resp2 = new HttpResponseBean(201, "Created");
        resp2.setBody(createEntry("hello-world2.xsd"), MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        output.addPart(resp2, MediaType.MESSAGE_HTTP_TYPE).getHeaders().putSingle("Content-ID", "<schemas/XMLSchema.xsd@package>");

        return output;
    }

	/**
	 * asdfasdfasfdsadf
	 */
	private Entry createEntry(String title) {
		XsdDocument xsd = new XsdDocument();
		xsd.setUuid(UUID.randomUUID().toString());
		xsd.setName(title);
		xsd.setVersion("1.0");
		xsd.setDescription("Hello: " + title);
		try {
			return SrampAtomUtils.wrapSrampArtifact(xsd);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
