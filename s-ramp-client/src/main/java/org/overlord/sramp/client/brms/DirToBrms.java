package org.overlord.sramp.client.brms;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
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
/**
 * Some good resources:
 * 
 * https://community.jboss.org/wiki/AtomPubInterfaceForGuvnor
 * http://docs.jboss.org/drools/release/5.5.0.Final/drools-guvnor-docs/html/ch09.html
 * https://github.com/droolsjbpm/guvnor/blob/master/guvnor-webapp-drools/src/test/java/org/drools/guvnor/server/jaxrs/BasicPackageResourceIntegrationTest.java
 * @author kstam
 *
 */
public class DirToBrms {

    ClientRequestFactory fac = null;

    public static void main(String [ ] args) {
        try {
            
            String brmsPackageName = "SRAMPPackage";
            String baseUrl         = "http://localhost:8080/drools-guvnor";
            String brmsUserId      = "admin";
            String brmsPassword    = "admin";
            
            if (args.length > 0) brmsPackageName = args[0];
            if (args.length > 1) baseUrl         = args[1];
            if (args.length > 2) brmsUserId      = args[2];
            if (args.length > 3) brmsPassword    = args[3];
            
            System.out.println("Using ");
            System.out.println("   brmsPackageName..: " + brmsPackageName);
            System.out.println("   baseUrl..........: " + baseUrl);
            System.out.println("   brmsUserId.......: " + brmsUserId);
            System.out.println("   brmsPassword.....: " + brmsPassword);
            
            DirToBrms brms = new DirToBrms();

            String brmsURLStr = baseUrl + "/rest/packages/";
            boolean brmsExists = brms.urlExists(brmsURLStr, brmsUserId, brmsPassword);
            if (! brmsExists) {
                System.out.println("Can't find BRMS endpoint: " + brmsURLStr);
                return;
            }
            //create the package if it does not exist
            if (! brms.urlExists(brmsURLStr + brmsPackageName, brmsUserId, brmsPassword)) {
                brms.createNewPackage(baseUrl, brmsPackageName, brmsUserId, brmsPassword);
            }
            //add the assets
            brms.addAssetsToPackageToBRMS(baseUrl, brmsPackageName, brmsUserId, brmsPassword);
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    protected void applyAuth(HttpURLConnection connection, String user, String password) {
        String auth = user + ":" + password;
        connection.setRequestProperty("Authorization", "Basic "
                + new String(Base64.encodeBase64(auth.getBytes())));
    }
    
    /**
     * A HTTP POST request to URL http://host:portnumber/repository/packages with the data:
     *
     * <entry xml:base="http://localhost:8080/repository/packages">
     *     <title>testPackage1</title>
     *     <summary>desc1</summary>
     * </entry>
     * @param baseUrl
     * @param pkgName
     * @param userId
     * @param password
     * @throws Exception 
     */
    public void createNewPackage(String baseUrl, String pkgName, String userId, String password) throws Exception {
        String urlStr = baseUrl + "/rest/packages";

        Credentials credentials = new UsernamePasswordCredentials(userId, password);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
        fac = new ClientRequestFactory(clientExecutor, new URI(baseUrl));
        ClientRequest createNewPackageRequest = fac.createRequest(urlStr);
        createNewPackageRequest.accept(MediaType.APPLICATION_ATOM_XML);
        Entry entry = new Entry();
        entry.setTitle(pkgName);
        entry.setSummary("S-RAMP Package containing Governance Workflows");
        createNewPackageRequest.body(MediaType.APPLICATION_ATOM_XML, entry);
        ClientResponse<Entry> newPackageResponse = createNewPackageRequest.post(Entry.class);
        System.out.println("response status=" + newPackageResponse.getResponseStatus());
        System.out.println("Create new package with id=" + newPackageResponse.getEntity().getId());
    }
    
    public void addAssetsToPackageToBRMS(String baseUrl, String pkgName, String userId, String password) throws Exception {
        String urlStr = baseUrl + "/rest/packages/" + pkgName + "/assets";
        String dir  = "/governance-workflows/" + pkgName;
        Credentials credentials = new UsernamePasswordCredentials(userId, password);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
        fac = new ClientRequestFactory(clientExecutor, new URI(urlStr));
        
        URL url = this.getClass().getResource(dir);
        if (url==null) throw new Exception ("Could not find " + dir + " on the classpath");
        String path = url.toURI().getSchemeSpecificPart();
        File srampPackageDir = new File(path);
        if (srampPackageDir.exists()) {
            //read all files from this directory
            FilenameFilter droolsFiles = new FilenameFilter() {
                public boolean accept(File file, String name) {
                  return !name.startsWith(".");
                }
            };
            File[] fileList = srampPackageDir.listFiles(droolsFiles);
            for (File file : fileList) {
                ClientRequest addAssetRequest = fac.createRequest(urlStr);
                InputStream is = file.toURI().toURL().openStream();
                System.out.println("uploading " + file.getName() + " -> " + urlStr );
                uploadToBrms(file.getName(), is, addAssetRequest);
            }
        } else if (path.indexOf("!") > 0) {
            //or read from a jar
            String[] paths = path.split("!");
            Enumeration<JarEntry> en = new JarFile(new File(new URI(paths[0]))).entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && !name.contains("/.") && name.startsWith(dir.substring(1))) {
                    String fileName = name.substring(name.lastIndexOf("/")+1,name.length());
                    InputStream is = this.getClass().getResourceAsStream("/" + name);
                    ClientRequest addAssetRequest = fac.createRequest(urlStr);
                    System.out.println("uploading " + name + " -> " + urlStr );
                    uploadToBrms(fileName, is, addAssetRequest);
                }
            }
        }
    }
    
    private void uploadToBrms(String fileName, InputStream is, ClientRequest addAssetRequest) throws Exception {
        addAssetRequest.body(MediaType.APPLICATION_OCTET_STREAM, is);
        addAssetRequest.accept(MediaType.APPLICATION_ATOM_XML);
        addAssetRequest.header("Slug", fileName);
        ClientResponse<String> uploadAssetResponse = addAssetRequest.post(String.class);
        int status = uploadAssetResponse.getStatus();
        String response = uploadAssetResponse.getEntity();
        if (200 == status) {
            System.out.println(response);
        } else {
            System.err.println("Upload to BRMS failed with response status = " + status);
        }
    }

}
