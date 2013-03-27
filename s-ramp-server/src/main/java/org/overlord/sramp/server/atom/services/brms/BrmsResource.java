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
package org.overlord.sramp.server.atom.services.brms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.text.SimpleDateFormat;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.services.brms.BrmsConstants;
import org.overlord.sramp.atom.services.brms.Format;
import org.overlord.sramp.atom.services.brms.assets.Assets;
import org.overlord.sramp.atom.services.brms.assets.Assets.Asset;
import org.overlord.sramp.atom.services.brms.packages.Packages;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.QueryManagerFactory;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;

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

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SrampConstants.DATE_FORMAT);
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
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Packages getRestAllPackages() throws SrampAtomException {
        try {
            //BRMS/Drools packages should be uploaded under ExtendedArtifactType of BrmsPkgDocument
            ArtifactSet artifactSet = null;
            Packages brmsPackages = new Packages();
            QueryManager queryManager = QueryManagerFactory.newInstance();
            String query = String.format("/s-ramp/%1$s/%2$s", "ext", BrmsConstants.BRMS_PKG_DOCUMENT);
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
    @Produces({MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML})
    public Assets getRestXMLAllAssetsInPackage(@PathParam("pkgName") String pkgName) throws SrampAtomException {
        Assets assets = new Assets();
        try {
            //BRMS/Drools packages should be uploaded under ExtendedArtifactType of BrmsPkgDocument
            pkgName += ".pkg";
            QueryManager queryManager = QueryManagerFactory.newInstance();
            String query = String.format("/s-ramp/%1$s/%2$s[@name='%3$s']", "ext", BrmsConstants.BRMS_PKG_DOCUMENT, pkgName);
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
     * Returns an XML document with all BRMS packages in the system.
     * jBPM only needs the titles of the packages.
     */
    @GET
    @Path("rest/packages/{pkgName}/assets/{assetName}")
    @Produces({MediaType.APPLICATION_ATOM_XML_ENTRY, MediaType.APPLICATION_ATOM_XML})
    public Entry getRestXMLAsset(@PathParam("pkgName") String pkgName,
                                    @PathParam("assetName") String assetName)
       throws SrampAtomException {

        try {
            //BRMS/Drools packages should be uploaded under ExtendedArtifactType of BrmsPkgDocument
            pkgName += ".pkg";
            QueryManager queryManager = QueryManagerFactory.newInstance();
            String query = String.format("/s-ramp/%1$s/%2$s[@name='%3$s']", "ext", BrmsConstants.BRMS_PKG_DOCUMENT, pkgName);
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
                    Assets assets = element.getValue();
                    for (Asset asset : assets.getAsset()) {
                        if (asset.getTitle().equals(assetName)) {
                            Entry entry = new Entry();
                            entry.setBase(new URI(asset.getRefLink()));
                            entry.setTitle(asset.getTitle());
                            entry.setId(new URI(asset.getRefLink()));
                            entry.setPublished(asset.getPublished().toGregorianCalendar().getTime());
                            entry.getContributors().add(new Person(asset.getAuthor()));
                            Content content = new Content();
                            content.setType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
                            content.setBase(new URI(asset.getBinaryLink()));
                            entry.setContent(content);
                            Format format = new Format();
                            format.setValue(asset.getMetadata().getFormat());
                            entry.setAnyOtherJAXBObject(format);
                            return entry;
                        }
                    }
                }
            }
            return null;
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
     * http://localhost:8880/s-ramp-atom/brms/org.drools.guvnor.Guvnor/package/srampPackage/S-RAMP-0.0.3.0
     */
    @GET
    @Path("/org.drools.guvnor.Guvnor/package/{pkgName}/{version}")
    public Response getDroolsPackage(@PathParam("pkgName") String pkgName,
                               @PathParam("version") String version) throws SrampAtomException {
        try {
            //TODO for now ignoring version
            return getRestPackage(pkgName);
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
    public Response getRestPackage(@PathParam("pkgName") String pkgName) throws SrampAtomException {
        try {
            pkgName += ".pkg";  //S-RAMP stores the fileName not the package name
            QueryManager queryManager = QueryManagerFactory.newInstance();
            String query = String.format("/s-ramp/%1$s/%2$s[@name='%3$s']", "ext", BrmsConstants.BRMS_PKG_DOCUMENT, pkgName);
            SrampQuery srampQuery = queryManager.createQuery(query, "name", true);
            ArtifactSet artifactSet = srampQuery.executeQuery();
            if (artifactSet.iterator().hasNext()) {
                BaseArtifactType baseArtifact = artifactSet.iterator().next();
                PersistenceManager persistenceManager = PersistenceFactory.newInstance();
                ArtifactType artifactType = ArtifactType.valueOf(baseArtifact.getArtifactType());
                artifactType.setExtendedType(BrmsConstants.BRMS_PKG_DOCUMENT);
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
                String lastModifiedDate = simpleDateFormat.format(baseArtifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
                return Response.ok(output, "application/octet-stream")
                    .header("Content-Disposition", "attachment; filename=" + baseArtifact.getName())
                    .header("Content-Length", baseArtifact.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME))
                    .header("Last-Modified", lastModifiedDate)
                    .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Throwable e) {
            throw new SrampAtomException(e);
        }
    }

    @GET
    @Path("org.drools.guvnor.Guvnor/package/{pkgName}/{version}/{fileName}")
    public Response getDroolsFile(@PathParam("pkgName")   String pkgName,
                             @PathParam("version")  String version,
                             @PathParam("fileName") String fileName) throws SrampAtomException {
        String assetName = fileName;
        if (fileName.contains(".")) {
            assetName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return getRestAsset("application/octet-stream",pkgName, assetName);
    }
    /**
     * Returns the content of a Brms/Drools Package in the s-ramp repository. Note that
     * if multiple droolsPackage with the name are found it simply takes the first one.
     * This is probably a situation you want to avoid.
     *
     * @param pkgName - the name of the Brms Package
     * @param assetName - the name of the Brms Asset
     * @throws SrampAtomException
     * http://localhost:8080/s-ramp-server/brms/rest/packages/SRAMPPackage/assets/overlord.demo.SimpleReleaseProcess-taskform
     * http://localhost:8080/s-ramp-server/brms/org.drools.guvnor.Guvnor/package/SRAMPPackage/LATEST/overlord.demo.SimpleReleaseProcess-image.png
     */
    @GET
    @Produces({MediaType.APPLICATION_ATOM_XML, "application/octet-stream"})
    @Path("rest/packages/{pkgName}/assets/{assetName}/binary")
    public Response getRestAsset(@HeaderParam("Accept") String accept,
                             @PathParam("pkgName")   String pkgName,
                             @PathParam("assetName") String assetName
                             ) throws SrampAtomException {
        try {
            //1. Get all assets for this package
            Assets assets = getRestXMLAllAssetsInPackage(pkgName);

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
                BaseArtifactType baseArtifact = persistenceManager.getArtifact(uuid, artifactType);
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
                String lastModifiedDate = simpleDateFormat.format(baseArtifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
                if (accept!=null && (accept.contains(MediaType.APPLICATION_ATOM_XML) || accept.contains(MediaType.APPLICATION_XML) )) {
                    return Response.ok(output, MediaType.APPLICATION_XML)
                    .header("Content-Length", baseArtifact.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME))
                    .header("Last-Modified", lastModifiedDate)
                    .build();
                } else {
                     return Response.ok(output, "application/octet-stream")
                        .header("Content-Disposition", "attachment; filename=" + baseArtifact.getName())
                        .header("Content-Length", baseArtifact.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME))
                        .header("Last-Modified", lastModifiedDate)
                        .build();
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (Throwable e) {
            throw new SrampAtomException(e);
        }
    }

    @GET
    @Path("rest/packages/{pkgName}/assets/{assetName}/source")
    public Response getRestSourceAsset(@PathParam("pkgName")   String pkgName,
                             @PathParam("assetName") String assetName) throws SrampAtomException {
        return getRestAsset(MediaType.APPLICATION_XML, pkgName, assetName);
    }

}
