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
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.QueryManagerFactory;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

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
            String query = String.format("/s-ramp/%1$s/%2$s", "user", "UserDefinedArtifactType");
            SrampQuery srampQuery = queryManager.createQuery(query, "name", true);
            artifactSet = srampQuery.executeQuery();
            for (BaseArtifactType artifact : artifactSet) {
                Packages.Package brmsPackage = new Packages.Package();
                brmsPackage.setTitle(artifact.getName());
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
        try {
            Assets assets = new Assets();
            // For now reading from file
            JAXBContext jaxbContext=JAXBContext.newInstance("org.overlord.sramp.atom.services.brms");
            Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
            if (pkgName.equals("defaultPackage")) {
                StringReader reader = new StringReader(ASSETS_XML);
                JAXBElement<Assets> element = unMarshaller.unmarshal(new StreamSource(reader),Assets.class);
                assets = element.getValue();
            }
            return assets;
        } catch (Throwable e) {
            throw new SrampAtomException(e);
        }
    }

    /**
     * Returns the content of an artifact in the s-ramp repository.
     * @param model
     * @param type
     * @param uuid
     * @throws SrampAtomException
     */
    @GET
    @Path("rest/packages/{pkgName}/binary")
	public Response getPackage(@PathParam("pkgName") String pkgName) throws SrampAtomException {
        try {
			final InputStream artifactContent = null; //get the content from the repo
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
		} catch (Throwable e) {
			throw new SrampAtomException(e);
		}
    }
    
    static String PACKAGES_XML="<packages>\n" + 
    		" <package>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Test\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Performance%20Evaluation-taskform\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Evaluation4\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4-image\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultservicenodeicon\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/DefaultTask\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/drools\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4-taskform\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultemailicon\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Evaluation4-image\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultlogicon\n" + 
    		"  </assets>\n" + 
    		"  <assets>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/WorkDefinitions\n" + 
    		"  </assets>\n" + 
    		"  <author>admin</author>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description>The default rule package</description>\n" + 
    		"  <metadata>\n" + 
    		"   <archived>false</archived>\n" + 
    		"   <checkinComment>The default rule package</checkinComment>\n" + 
    		"   <created>2012-09-04T15:44:26.761-04:00</created>\n" + 
    		"   <state></state>\n" + 
    		"   <uuid>a7d50a3c-b25e-4609-b716-6ed13b07a49d</uuid>\n" + 
    		"   <versionNumber>2</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-19T13:09:43.566-04:00</published>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>defaultPackage</title>\n" + 
    		" </package>\n" + 
    		"</packages>";
    
    static String ASSETS_XML="<assets>\n" + 
    		" <asset>\n" + 
    		"  <author>admin</author>\n" + 
    		"  <binaryContentAttachmentFileName></binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/drools/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment></checkInComment>\n" + 
    		"   <created>2012-09-04T15:44:26.864-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>package</format>\n" + 
    		"   <note>&lt;![CDATA[ ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>82bd7cf0-444f-4f38-9a7d-971a35b4bac1</uuid>\n" + 
    		"   <versionNumber>2</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T17:48:42.835-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/drools\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/drools/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>drools</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>krisv</author>\n" + 
    		"  <binaryContentAttachmentFileName></binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Evaluation4-image/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>&lt;content from webdav&gt;</checkInComment>\n" + 
    		"   <created>2012-09-04T17:47:16.196-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>png</format>\n" + 
    		"   <note>&lt;![CDATA[ &lt;content from webdav&gt; ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>dd55df5c-cac7-48ed-9672-3d7a3910634a</uuid>\n" + 
    		"   <versionNumber>3</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T17:47:16.602-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Evaluation4-image\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Evaluation4-image/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>Evaluation4-image</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>guest</author>\n" + 
    		"  <binaryContentAttachmentFileName></binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Evaluation4/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>dd</checkInComment>\n" + 
    		"   <created>2012-09-04T17:47:16.902-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>bpmn</format>\n" + 
    		"   <note>&lt;![CDATA[ dd ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>3d8c8abe-8f77-4e8f-9c9d-983a474f2973</uuid>\n" + 
    		"   <versionNumber>6</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-06T14:19:18.877-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Evaluation4\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Evaluation4/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>Evaluation4</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>guest</author>\n" + 
    		"  <binaryContentAttachmentFileName>defaultemailicon\n" + 
    		"  </binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultemailicon/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>update binary</checkInComment>\n" + 
    		"   <created>2012-09-04T17:48:17.539-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>gif</format>\n" + 
    		"   <note>&lt;![CDATA[ update binary ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>7c3273c4-06ff-4bff-997c-5e33e26214df</uuid>\n" + 
    		"   <versionNumber>1</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T17:48:17.571-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultemailicon\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultemailicon/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>defaultemailicon</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>guest</author>\n" + 
    		"  <binaryContentAttachmentFileName>defaultlogicon\n" + 
    		"  </binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultlogicon/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>update binary</checkInComment>\n" + 
    		"   <created>2012-09-04T17:48:17.612-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>gif</format>\n" + 
    		"   <note>&lt;![CDATA[ update binary ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>557db35f-37de-442a-8981-47504bf8abaf</uuid>\n" + 
    		"   <versionNumber>1</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T17:48:17.637-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultlogicon\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultlogicon/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>defaultlogicon</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>guest</author>\n" + 
    		"  <binaryContentAttachmentFileName>defaultservicenodeicon\n" + 
    		"  </binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultservicenodeicon/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>update binary</checkInComment>\n" + 
    		"   <created>2012-09-04T17:48:17.684-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>png</format>\n" + 
    		"   <note>&lt;![CDATA[ update binary ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>cd6435ac-cc71-4518-97bc-087de9fcfa66</uuid>\n" + 
    		"   <versionNumber>1</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T17:48:17.707-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultservicenodeicon\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/defaultservicenodeicon/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>defaultservicenodeicon</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>guest</author>\n" + 
    		"  <binaryContentAttachmentFileName>WorkDefinitions\n" + 
    		"  </binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/WorkDefinitions/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>update binary</checkInComment>\n" + 
    		"   <created>2012-09-04T17:48:17.803-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>wid</format>\n" + 
    		"   <note>&lt;![CDATA[ update binary ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>16e4fe52-9cdc-4827-a4c1-8fbad6c87486</uuid>\n" + 
    		"   <versionNumber>1</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T17:48:18.316-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/WorkDefinitions\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/WorkDefinitions/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>WorkDefinitions</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>admin</author>\n" + 
    		"  <binaryContentAttachmentFileName></binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Test/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>dd</checkInComment>\n" + 
    		"   <created>2012-09-04T17:56:22.664-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>bpmn2</format>\n" + 
    		"   <note>&lt;![CDATA[ dd ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>6d51b5c2-6c55-4aad-8f07-90e671ae0551</uuid>\n" + 
    		"   <versionNumber>1</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T17:57:49.003-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Test\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Test/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>Test</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>krisv</author>\n" + 
    		"  <binaryContentAttachmentFileName></binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>&lt;content from webdav&gt;</checkInComment>\n" + 
    		"   <created>2012-09-04T20:17:33.466-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>ftl</format>\n" + 
    		"   <note>&lt;![CDATA[ &lt;content from webdav&gt; ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>0fa5a614-9013-415b-9be4-23cca7a72ca6</uuid>\n" + 
    		"   <versionNumber>3</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T20:17:33.953-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>com.sample.evaluation4</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>krisv</author>\n" + 
    		"  <binaryContentAttachmentFileName></binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4-image/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>&lt;content from webdav&gt;</checkInComment>\n" + 
    		"   <created>2012-09-04T20:18:10.795-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>png</format>\n" + 
    		"   <note>&lt;![CDATA[ &lt;content from webdav&gt; ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>3a37f8e8-ed0d-48e1-b367-bae17480e22e</uuid>\n" + 
    		"   <versionNumber>3</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T20:18:10.950-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4-image\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4-image/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>com.sample.evaluation4-image</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>krisv</author>\n" + 
    		"  <binaryContentAttachmentFileName></binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/DefaultTask/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>&lt;content from webdav&gt;</checkInComment>\n" + 
    		"   <created>2012-09-04T20:37:33.004-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>ftl</format>\n" + 
    		"   <note>&lt;![CDATA[ &lt;content from webdav&gt; ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>a0f2f992-e200-4574-ab2b-69255d871ce7</uuid>\n" + 
    		"   <versionNumber>3</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T20:37:33.472-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/DefaultTask\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/DefaultTask/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>DefaultTask</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>guest</author>\n" + 
    		"  <binaryContentAttachmentFileName>com.sample.evaluation4-taskform\n" + 
    		"  </binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4-taskform/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>update binary</checkInComment>\n" + 
    		"   <created>2012-09-04T20:46:13.444-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>flt</format>\n" + 
    		"   <note>&lt;![CDATA[ update binary ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>e1d95407-1307-4923-8563-ec4dbe3a6534</uuid>\n" + 
    		"   <versionNumber>1</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T20:46:13.668-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4-taskform\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/com.sample.evaluation4-taskform/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>com.sample.evaluation4-taskform</title>\n" + 
    		" </asset>\n" + 
    		" <asset>\n" + 
    		"  <author>guest</author>\n" + 
    		"  <binaryContentAttachmentFileName>Performance Evaluation-taskform\n" + 
    		"  </binaryContentAttachmentFileName>\n" + 
    		"  <binaryLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Performance%20Evaluation-taskform/binary\n" + 
    		"  </binaryLink>\n" + 
    		"  <description></description>\n" + 
    		"  <metadata>\n" + 
    		"   <checkInComment>update binary</checkInComment>\n" + 
    		"   <created>2012-09-04T20:46:13.745-04:00</created>\n" + 
    		"   <disabled>false</disabled>\n" + 
    		"   <format>flt</format>\n" + 
    		"   <note>&lt;![CDATA[ update binary ]]&gt;</note>\n" + 
    		"   <state>Draft</state>\n" + 
    		"   <uuid>13aff194-6616-4d24-91cf-60d1a0c2c859</uuid>\n" + 
    		"   <versionNumber>1</versionNumber>\n" + 
    		"  </metadata>\n" + 
    		"  <published>2012-09-04T20:46:13.782-04:00</published>\n" + 
    		"  <refLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Performance%20Evaluation-taskform\n" + 
    		"  </refLink>\n" + 
    		"  <sourceLink>http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Performance%20Evaluation-taskform/source\n" + 
    		"  </sourceLink>\n" + 
    		"  <title>Performance Evaluation-taskform</title>\n" + 
    		" </asset>\n" + 
    		"</assets>";

}
