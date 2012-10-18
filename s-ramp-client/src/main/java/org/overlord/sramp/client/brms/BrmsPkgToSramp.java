package org.overlord.sramp.client.brms;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

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
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.services.brms.Assets;
import org.overlord.sramp.atom.services.brms.BrmsConstants;
import org.overlord.sramp.atom.services.brms.Packages;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;

public class BrmsPkgToSramp {

    ClientRequestFactory fac = null;

    public static void main(String [ ] args) {
        try {
            String brmsPackageName = "defaultPackage";
            String baseUrl         = "http://localhost:8080/drools-guvnor";
            String tag             = "LATEST";
            String srampUrl        = "http://localhost:8880/s-ramp-atom";
            if (args.length > 0) brmsPackageName = args[0];
            if (args.length > 1) tag             = args[1];
            if (args.length > 2) baseUrl         = args[2];

            BrmsPkgToSramp sramp = new BrmsPkgToSramp();

            String srampURLStr = srampUrl + "/brms/rest/packages/";
            boolean srampExists = sramp.urlExists(srampURLStr, "", "");
            if (! srampExists) {
                System.out.println("Can't find S-RAMP endpoint: " + srampURLStr);
                return;
            }
            String brmsURLStr = baseUrl + "/rest/packages/";
            boolean brmsExists = sramp.urlExists(brmsURLStr, "admin", "admin");
            if (! brmsExists) {
                System.out.println("Can't find BRMS endpoint: " + brmsURLStr);
                return;
            }
            sramp.uploadBrmsPackage(baseUrl, brmsPackageName, tag);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean urlExists(String checkUrl, String user, String password) {
        //http://localhost:8880/s-ramp-atom/brms/rest/packages/
        //http://localhost:8880/s-ramp-atom/brms/rest/packages/


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

    protected void applyAuth(HttpURLConnection connection, String user, String password) {
        String auth = user + ":" + password;
        connection.setRequestProperty("Authorization", "Basic "
                + new String(Base64.encodeBase64(auth.getBytes())));
    }

    public void uploadBrmsPackage(String baseUrl, String pkgName, String tag) throws Exception {
        // http://localhost:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/srampPackage/S_RAMP_0.0.3.0
        String urlStr = baseUrl + "/org.drools.guvnor.Guvnor/package/" + pkgName + "/" + tag;

        String userId   = "admin";
        String password = "admin";

        Credentials credentials = new UsernamePasswordCredentials(userId, password);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
        fac = new ClientRequestFactory(clientExecutor, new URI(baseUrl));

        Map<String, Packages.Package> brmsPkgMap = getPkgsFromBrms(baseUrl);
        if (! brmsPkgMap.containsKey(pkgName)) {
            System.out.println("Brms contains the following BRMS Packages");
            for (String name : brmsPkgMap.keySet()) {
                System.out.println(" * " + name);
            }
            throw new Exception ("Could not find package with name " + pkgName + " in BRMS");
        }
        Packages.Package brmsPkg = brmsPkgMap.get(pkgName);

        System.out.println("Located BRMS package '" + pkgName + "' :");
        System.out.println("   UUID ..........: " + brmsPkg.getMetadata().getUuid());
        System.out.println("   Version .......: " + brmsPkg.getMetadata().getVersionNumber());
        System.out.println("   Author ........: " + brmsPkg.getAuthor());
        System.out.println("   Last published : " + brmsPkg.getPublished());
        System.out.println("   Description ...: " + brmsPkg.getDescription());

        // now uploading this into s-ramp
        UserDefinedArtifactType userDefinedArtifactType =
            (UserDefinedArtifactType) ArtifactType.fromFileExtension("pkg").newArtifactInstance();
        userDefinedArtifactType.setUuid(brmsPkg.getMetadata().getUuid());
        userDefinedArtifactType.setName(pkgName + ".pkg");

        Property assetsProperty = new Property();
        assetsProperty.setPropertyName(BrmsConstants.ASSET_INFO_XML);
        String assetsXml = getAssetsStringFromBrms(baseUrl, pkgName);
        //update the links
        assetsXml = assetsXml.replaceAll("http://localhost:8080/drools-guvnor", "http://localhost:8880/s-ramp-atom/brms");
        assetsProperty.setPropertyValue(assetsXml);
        userDefinedArtifactType.getProperty().add(assetsProperty);

        System.out.println("Reading " + pkgName + " from url " + urlStr );
        ClientResponse<InputStream> pkgResponse = getInputStream(urlStr);
        InputStream content = pkgResponse.getEntity();
        SrampAtomApiClient client = new SrampAtomApiClient("http://localhost:8880/s-ramp-atom/s-ramp");
        Entry entry = client.uploadArtifact(userDefinedArtifactType, content);
        IOUtils.closeQuietly(content);
        System.out.println("Uploaded " + pkgName + " UUID=" + entry.getId().toString());

        // Now obtaining the assets in the this package, and upload those
        // TODO set relationship to parent pkg
        Assets assets = getAssetsFromBrms(baseUrl, pkgName);

        //Upload the process AND process-image, making sure the uuid is identical to the one mentioned
        for (Assets.Asset asset : assets.getAsset()) {
                //Upload the asset
                String fileName = asset.getTitle() + "." + asset.getMetadata().getFormat().toLowerCase();
                String uuid = asset.getMetadata().getUuid();
                //reading the asset from disk
                //http://localhost:8080/drools-guvnor/rest/packages/srampPackage/assets/
                String assetURLStr = baseUrl + "/rest/packages/" + pkgName + "/assets/" + asset.getTitle() + "/binary";
                System.out.println("Reading asset " + asset.getTitle() + " from url " + assetURLStr );
                ClientResponse<InputStream> assetResponse = getInputStream(assetURLStr);
                InputStream assetInputStream = assetResponse.getEntity();

                //upload the asset using the uuid
                ArtifactType artifactType = ArtifactType.fromFileExtension(asset.getMetadata().getFormat());
                BaseArtifactType baseArtifactType = artifactType.newArtifactInstance();
                baseArtifactType.setName(fileName);
                baseArtifactType.setUuid(uuid);

                Entry assetEntry = client.uploadArtifact(baseArtifactType, assetInputStream);
                IOUtils.closeQuietly(assetInputStream);
                BaseArtifactType assetArtifact = SrampAtomUtils.unwrapSrampArtifact(assetEntry);
                System.out.println("Uploaded asset " + assetArtifact.getName() + " " + assetArtifact.getUuid());


        }

        System.out.println("OK");

    }

    protected Map<String, Packages.Package> getPkgsFromBrms(String baseUrl) throws Exception {
        String pkgsUrl = baseUrl + "/rest/packages/";
        System.out.println("Reading from " + pkgsUrl + " to find all packages in BRMS..");
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

    protected String getAssetsStringFromBrms(String baseUrl, String pkgName) throws Exception {
        String assetsUrl = baseUrl + "/rest/packages/" + pkgName + "/assets";
        System.out.println("Reading from " + assetsUrl + " to find all assets in package " + pkgName);
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

    protected Assets getAssetsFromBrms(String baseUrl, String pkgName) throws Exception {
        String assetsUrl = baseUrl + "/rest/packages/" + pkgName + "/assets";
        System.out.println("Reading from " + assetsUrl + " to find all assets in package " + pkgName);
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
