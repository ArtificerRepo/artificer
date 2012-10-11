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
package org.overlord.sramp.atom.services.brms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.QueryManagerFactory;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;

/**
 * The JAX-RS resource that handles artifact specific tasks, including:
 *
 * <ul>
 *   <li>Get a BRMS artifact content (binary content)</li>
 *   <li>Update artifact meta data</li>
 *   <li>Update artifact content</li>
 *   <li>Delete an artifact</li>
 * </ul>
 *
 */
@Path("/brms")
public class BrmsResource {


    /**
     * Constructor.
     */
    public BrmsResource() {
    }


    /**
     * Returns an XML document with all BRMS packages in the system.
     * jBPM only needs the titles of the packages.
     */
    @GET
    @Path("rest/packages/")
    @Produces(MediaType.APPLICATION_XML)
    public Packages getAllPackages() throws SrampAtomException {
        try {
            //BRMS/Drools packages should be uploaded under UserDefinedArtifactType of BrmsPkgDocument
            ArtifactSet artifactSet = null;
            Packages brmsPackages = new Packages();
            QueryManager queryManager = QueryManagerFactory.newInstance();
            String query = String.format("/s-ramp/%1$s/%2$s", "user", BrmsConstants.BRMS_PKG_DOCUMENT);
            SrampQuery srampQuery = queryManager.createQuery(query, "name", true);
            artifactSet = srampQuery.executeQuery();
            for (BaseArtifactType artifact : artifactSet) {
                Packages.Package brmsPackage = new Packages.Package();
                brmsPackage.setTitle(artifact.getName().substring(0,artifact.getName().lastIndexOf(".")));
                brmsPackage.setPublished(artifact.getCreatedTimestamp());
                brmsPackage.setAuthor(artifact.getCreatedBy());
                brmsPackage.setDescription(artifact.getDescription());
                Packages.Package.Metadata metaData = new Packages.Package.Metadata();
                metaData.setUuid(artifact.getUuid());
                brmsPackage.setMetadata(metaData);
                brmsPackages.getPackage().add(brmsPackage);
            }
            return brmsPackages;
        } catch (Throwable e) {
            throw new SrampAtomException(e);
        }
    }

    /**
     * Returns an XML document with all BRMS packages in the system.
     * jBPM only needs the titles of the packages.
     */
    @GET
    @Path("rest/packages/{pkgName}/assets/")
    @Produces(MediaType.APPLICATION_XML)
    public Assets getAllAssetsInPackage(@PathParam("pkgName") String pkgName) throws SrampAtomException {
        Assets assets = new Assets();
        try {
            //BRMS/Drools packages should be uploaded under UserDefinedArtifactType of BrmsPkgDocument
            pkgName += ".pkg";
            QueryManager queryManager = QueryManagerFactory.newInstance();
            String query = String.format("/s-ramp/%1$s/%2$s[@name='%3$s']", "user", BrmsConstants.BRMS_PKG_DOCUMENT, pkgName);
            SrampQuery srampQuery = queryManager.createQuery(query, "name", true);
            ArtifactSet artifactSet = srampQuery.executeQuery();
            if (artifactSet.iterator().hasNext()) {
                BaseArtifactType baseArtifact = artifactSet.iterator().next();
                String assetsString = "";
                for (Property property : baseArtifact.getProperty()) {
                    if (BrmsConstants.ASSET_INFO_XML.equals(property.getPropertyName())) {
                        assetsString = property.getPropertyValue();
                        break;
                    }
                }
                if (assetsString.length() > 0) {
                    JAXBContext jaxbContext=JAXBContext.newInstance("org.overlord.sramp.atom.services.brms");
                    Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
                    StringReader reader = new StringReader(assetsString);
                    JAXBElement<Assets> element = unMarshaller.unmarshal(new StreamSource(reader),Assets.class);
                    assets = element.getValue();
                }
            }
            return assets;
        } catch (Throwable e) {
            throw new SrampAtomException(e);
        }
    }

    /**
     * Returns the content of a Brms/Drools Package in the s-ramp repository. Note that
     * if multiple droolsPackage with the name are found it simply takes the first one.
     * This is probably a situation you want to avoid.
     * 
     * @param pkgName - the name of the Brms Package
     * @throws SrampAtomException
     */
    @GET
    @Path("rest/packages/{pkgName}/binary")
    public Response getPackage(@PathParam("pkgName") String pkgName) throws SrampAtomException {
        try {
            pkgName += ".pkg";  //S-RAMP stores the fileName not the package name
            QueryManager queryManager = QueryManagerFactory.newInstance();
            String query = String.format("/s-ramp/%1$s/%2$s[@name='%3$s']", "user", BrmsConstants.BRMS_PKG_DOCUMENT, pkgName);
            SrampQuery srampQuery = queryManager.createQuery(query, "name", true);
            ArtifactSet artifactSet = srampQuery.executeQuery();
            if (artifactSet.iterator().hasNext()) {
                BaseArtifactType baseArtifact = artifactSet.iterator().next();
                PersistenceManager persistenceManager = PersistenceFactory.newInstance();
                ArtifactType artifactType = ArtifactType.valueOf(baseArtifact.getArtifactType());
                artifactType.setUserType(BrmsConstants.BRMS_PKG_DOCUMENT);
                final InputStream artifactContent = persistenceManager.getArtifactContent(baseArtifact.getUuid(), artifactType);
                Object output = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        try {
                            IOUtils.copy(artifactContent, output);
                        } finally {
                            IOUtils.closeQuietly(artifactContent);
                        }
                    }
                };
                return Response.ok(output, "application/octet-stream").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Throwable e) {
            throw new SrampAtomException(e);
        }
    }

    /**
     * Returns the content of a Brms/Drools Package in the s-ramp repository. Note that
     * if multiple droolsPackage with the name are found it simply takes the first one.
     * This is probably a situation you want to avoid.
     * 
     * @param pkgName - the name of the Brms Package
     * @param assetName - the name of the Brms Asset
     * @throws SrampAtomException
     */
    @GET
    @Path("rest/packages/{pkgName}/assets/{assetName}/binary")
    public Response getAsset(@PathParam("pkgName")   String pkgName, 
                             @PathParam("assetName") String assetName) throws SrampAtomException {
        try {
            //1. Get all assets for this package
            Assets assets = getAllAssetsInPackage(pkgName);

            //2. Find the asset with {assetName}, and obtain the UUID
            String uuid = null;
            String format = null;
            for (Assets.Asset asset : assets.getAsset()) {
                if (asset.getTitle().equals(assetName)) {
                    uuid = asset.getMetadata().getUuid();
                    format = asset.getMetadata().getFormat();
                    break;
                }
            }

            //3. Use this UUID to lookup the content, and stream back the response
            if (uuid!=null) {
                ArtifactType artifactType = ArtifactType.fromFileExtension(format);
                PersistenceManager persistenceManager = PersistenceFactory.newInstance();
                final InputStream artifactContent = persistenceManager.getArtifactContent(uuid, artifactType);
                Object output = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        try {
                            IOUtils.copy(artifactContent, output);
                        } finally {
                            IOUtils.closeQuietly(artifactContent);
                        }
                    }
                };
                return Response.ok(output, "application/octet-stream").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (Throwable e) {
            throw new SrampAtomException(e);
        }
    }

}
