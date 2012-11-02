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
package org.overlord.sramp.client.shell.commands.brms;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.services.brms.Assets;
import org.overlord.sramp.atom.services.brms.BrmsConstants;
import org.overlord.sramp.atom.services.brms.Packages;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.ShellContext;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;

/**
 * BRMS command that will copy a package out of BRMS and push it into the
 * S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class Pkg2SrampCommand extends AbstractShellCommand {

    private ClientRequestFactory fac = null;

	/**
	 * Constructor.
	 */
	public Pkg2SrampCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("brms:pkg2sramp <brmsPackageName> <tag> <brmsBaseUrl> <brmsUserId> <brmsPassword>");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'pkg2sramp' command copies a named BRMS package from the");
		print("BRMS system and uploads it to the S-RAMP repository.");
		print("");
		print("Example usage:");
		print("> brms:pkg2sramp defaultPackage LATEST http://localhost:8080/drools-guvnor admin admin");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		QName clientVarName = new QName("s-ramp", "client");
		SrampAtomApiClient client = (SrampAtomApiClient) context.getVariable(clientVarName);
		if (client == null) {
			print("No S-RAMP repository connection is currently open.");
			return;
		}

        String brmsPackageName = optionalArgument(0, "defaultPackage");
        String tag             = optionalArgument(1, "LATEST");
        String baseUrl         = optionalArgument(2, "http://localhost:8080/drools-guvnor");
        String brmsUserId      = optionalArgument(3, "admin");
        String brmsPassword    = optionalArgument(4, "admin");

        print("Copying BRMS package to S-RAMP using: ");
        print("   brmsPackageName..: %1$s", brmsPackageName);
        print("   tag .............: %1$s", tag);
        print("   baseUrl..........: %1$s", baseUrl);
        print("   brmsUserId.......: %1$s", brmsUserId);
        print("   brmsPassword.....: %1$s", brmsPassword);

		String brmsURLStr = baseUrl + "/rest/packages/";
		boolean brmsExists = urlExists(brmsURLStr, brmsUserId, brmsPassword);
		if (!brmsExists) {
			print("Can't find BRMS endpoint: " + brmsURLStr);
			return;
		}

		try {
            uploadBrmsPackage(baseUrl, brmsPackageName, tag, brmsUserId, brmsPassword, client);
		} catch (Exception e) {
			print("FAILED to copy the BRMS package.");
			print("\t" + e.getMessage());
		}
	}

    /**
     * Returns true if the given URL can be accessed.
     * @param checkUrl
     * @param user
     * @param password
     */
    public boolean urlExists(String checkUrl, String user, String password) {
        try {
            URL checkURL = new URL(checkUrl);
            HttpURLConnection checkConnection = (HttpURLConnection) checkURL.openConnection();
            checkConnection.setRequestMethod("GET");
            checkConnection.setRequestProperty("Accept", "application/xml");
            checkConnection.setConnectTimeout(10000);
            checkConnection.setReadTimeout(10000);
            applyAuth(checkConnection, user, password);
            checkConnection.connect();
            return (checkConnection.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Applies basic auth to the given HTTP URL connection.
     * @param connection
     * @param user
     * @param password
     */
    protected void applyAuth(HttpURLConnection connection, String user, String password) {
        String auth = user + ":" + password;
        connection.setRequestProperty("Authorization", "Basic "
                + new String(Base64.encodeBase64(auth.getBytes())));
    }

    /**
     * Uploads the BMRS package.
     * @param baseUrl
     * @param pkgName
     * @param tag
     * @param userId
     * @param password
     * @param client
     * @throws Exception
     */
	public void uploadBrmsPackage(String baseUrl, String pkgName, String tag, String userId, String password,
			SrampAtomApiClient client) throws Exception {
        // http://localhost:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/srampPackage/S_RAMP_0.0.3.0
        String urlStr = baseUrl + "/org.drools.guvnor.Guvnor/package/" + pkgName + "/" + tag;

        Credentials credentials = new UsernamePasswordCredentials(userId, password);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
        fac = new ClientRequestFactory(clientExecutor, new URI(baseUrl));

        Map<String, Packages.Package> brmsPkgMap = getPkgsFromBrms(baseUrl);
        if (! brmsPkgMap.containsKey(pkgName)) {
            print("Brms contains the following BRMS Packages");
            for (String name : brmsPkgMap.keySet()) {
                print(" * " + name);
            }
            throw new Exception ("Could not find package with name " + pkgName + " in BRMS");
        }
        Packages.Package brmsPkg = brmsPkgMap.get(pkgName);

        print("Located BRMS package '" + pkgName + "' :");
        print("   UUID ............: " + brmsPkg.getMetadata().getUuid());
        print("   Version .........: " + brmsPkg.getMetadata().getVersionNumber());
        print("   Author ..........: " + brmsPkg.getAuthor());
        print("   Last published ..: " + brmsPkg.getPublished());
        print("   Description .....: " + brmsPkg.getDescription());

        // now uploading this into s-ramp
        UserDefinedArtifactType userDefinedArtifactType =
            (UserDefinedArtifactType) ArtifactType.fromFileExtension("pkg").newArtifactInstance();
        userDefinedArtifactType.setUuid(brmsPkg.getMetadata().getUuid());
        userDefinedArtifactType.setName(pkgName + ".pkg");

        Property assetsProperty = new Property();
        assetsProperty.setPropertyName(BrmsConstants.ASSET_INFO_XML);
        String assetsXml = getAssetsStringFromBrms(baseUrl, pkgName);
        //update the links
        assetsXml = assetsXml.replaceAll(baseUrl, client + "/brms");
        assetsProperty.setPropertyValue(assetsXml);
        userDefinedArtifactType.getProperty().add(assetsProperty);

        print("Reading " + pkgName + " from url " + urlStr);
        ClientResponse<InputStream> pkgResponse = getInputStream(urlStr);
        InputStream content = pkgResponse.getEntity();

        BaseArtifactType artifact = client.uploadArtifact(userDefinedArtifactType, content);
        IOUtils.closeQuietly(content);
        print("Uploaded " + pkgName + " UUID=" + artifact.getUuid());

        // Now obtaining the assets in the this package, and upload those
        // TODO set relationship to parent pkg
        Assets assets = getAssetsFromBrms(baseUrl, pkgName);

        //Upload the process AND process-image, making sure the uuid is identical to the one mentioned
        for (Assets.Asset asset : assets.getAsset()) {
            if (!"package".equalsIgnoreCase(asset.getMetadata().getFormat())) {
                //Upload the asset
                String fileName = asset.getTitle() + "." + asset.getMetadata().getFormat().toLowerCase();
                String uuid = asset.getMetadata().getUuid();
                //reading the asset from disk
                //http://localhost:8080/drools-guvnor/rest/packages/srampPackage/assets/
                String assetURLStr = baseUrl + "/rest/packages/" + pkgName + "/assets/" + asset.getTitle() + "/binary";
                print("Reading asset " + asset.getTitle() + " from url " + assetURLStr );
                ClientResponse<InputStream> assetResponse = getInputStream(assetURLStr);
                InputStream assetInputStream = assetResponse.getEntity();

                //upload the asset using the uuid
                ArtifactType artifactType = ArtifactType.fromFileExtension(asset.getMetadata().getFormat());
                BaseArtifactType baseArtifactType = artifactType.newArtifactInstance();
                baseArtifactType.setName(fileName);
                baseArtifactType.setUuid(uuid);

                BaseArtifactType assetArtifact = client.uploadArtifact(baseArtifactType, assetInputStream);
                IOUtils.closeQuietly(assetInputStream);
                print("Uploaded asset " + assetArtifact.getName() + " " + assetArtifact.getUuid());
            }
        }

        print("OK");

    }

    /**
     * Gets the packages from BRMS at the given URL.
     * @param baseUrl
     * @throws Exception
     */
    protected Map<String, Packages.Package> getPkgsFromBrms(String baseUrl) throws Exception {
        String pkgsUrl = baseUrl + "/rest/packages/";
        print("Reading from " + pkgsUrl + " to find all packages in BRMS..");
        ClientRequest pkgsRequest = fac.createRequest(pkgsUrl);
        pkgsRequest.accept(MediaType.APPLICATION_XML);
        ClientResponse<Packages> pkgsResponse = pkgsRequest.get(Packages.class);
        if (pkgsResponse.getStatus() != 200) {
            throw new Exception("Failed : HTTP error code : "
                    + pkgsResponse.getStatus());
        }
        Packages packages = pkgsResponse.getEntity();
        Map<String, Packages.Package> brmsPkgMap = new HashMap<String, Packages.Package>();
        for (Packages.Package brmsPkg : packages.getPackage()) {
            brmsPkgMap.put(brmsPkg.getTitle(), brmsPkg);
        }
        return brmsPkgMap;
    }

    /**
     * Gets the assets XML as a string.
     * @param baseUrl
     * @param pkgName
     * @throws Exception
     */
    protected String getAssetsStringFromBrms(String baseUrl, String pkgName) throws Exception {
        String assetsUrl = baseUrl + "/rest/packages/" + pkgName + "/assets";
        print("Reading from " + assetsUrl + " to find all assets in package " + pkgName);
        ClientRequest assetsRequest = fac.createRequest(assetsUrl);
        assetsRequest.accept(MediaType.APPLICATION_XML);
        ClientResponse<String> assetsResponse = assetsRequest.get(String.class);
        if (assetsResponse.getStatus() != 200) {
            throw new Exception("Failed : HTTP error code : "
                    + assetsResponse.getStatus());
        }
        String assetsXml = assetsResponse.getEntity();
        return assetsXml;
    }

    /**
     * Gets the assets from BRMS.
     * @param baseUrl
     * @param pkgName
     * @throws Exception
     */
    protected Assets getAssetsFromBrms(String baseUrl, String pkgName) throws Exception {
        String assetsUrl = baseUrl + "/rest/packages/" + pkgName + "/assets";
        print("Reading from " + assetsUrl + " to find all assets in package " + pkgName);
        ClientRequest assetsRequest = fac.createRequest(assetsUrl);
        assetsRequest.accept(MediaType.APPLICATION_XML);
        ClientResponse<Assets> assetsResponse = assetsRequest.get(Assets.class);
        if (assetsResponse.getStatus() != 200) {
            throw new Exception("Failed : HTTP error code : "
                    + assetsResponse.getStatus());
        }
        Assets assets = assetsResponse.getEntity();
        return assets;
    }

    /**
     * Gets the input stream at the given URL.
     * @param url
     * @throws Exception
     */
    public ClientResponse<InputStream> getInputStream(String url) throws Exception {
        ClientRequest request = fac.createRequest(url);
        ClientResponse<InputStream> response = request.get(InputStream.class);
        if (response.getStatus() != 200) {
            throw new Exception("Failed : HTTP error code : "
                    + response.getStatus());
        }
        return response;
    }

}
