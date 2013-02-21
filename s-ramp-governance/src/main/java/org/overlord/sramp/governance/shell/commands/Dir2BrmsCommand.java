package org.overlord.sramp.governance.shell.commands;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
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
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.api.Arguments;
import org.overlord.sramp.shell.api.ShellContext;
import org.overlord.sramp.shell.api.SimpleShellContext;

/**
 * Some good resources:
 *
 * https://community.jboss.org/wiki/AtomPubInterfaceForGuvnor
 * http://docs.jboss.org/drools/release/5.5.0.Final/drools-guvnor-docs/html/ch09.html
 * https://github.com/droolsjbpm/guvnor/blob/master/guvnor-webapp-drools/src/test/java/org/drools/guvnor/server/jaxrs/BasicPackageResourceIntegrationTest.java
 * @author kstam
 *
 */
public class Dir2BrmsCommand extends AbstractShellCommand {

    /**
     * Main entry point - for use outside the interactive shell.
     * @param args
     * @throws Exception
     */
    public static void main(String [] args) throws Exception {
        String brmsPackageName = "SRAMPPackage";
        String baseUrl         = "http://localhost:8080/drools-guvnor";
        String brmsUserId      = "admin";
        String brmsPassword    = "admin";
        String packagePath     = null;
        if (args.length > 0) brmsPackageName = args[0];
        if (args.length > 1) baseUrl         = args[1];
        if (args.length > 2) brmsUserId      = args[2];
        if (args.length > 3) brmsPassword    = args[3];
        if (args.length > 4) packagePath     = args[4];
        StringBuilder argLine = new StringBuilder();
        argLine.append(brmsPackageName)
                .append(" ").append(baseUrl)
                .append(" ").append(brmsUserId)
                .append(" ").append(brmsPassword);
        if (packagePath != null)
            argLine.append(" ").append(packagePath);

        Dir2BrmsCommand cmd = new Dir2BrmsCommand();
        ShellContext context = new SimpleShellContext();
        cmd.setArguments(new Arguments(argLine.toString()));
        cmd.setContext(context);
        cmd.execute();
    }

    ClientRequestFactory fac = null;

    /**
     * Constructor.
     */
    public Dir2BrmsCommand() {
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
     */
    @Override
    public void printUsage() {
        print("brms:dir2brms <brmsPackageName> <brmsBaseUrl> <brmsUserId> <brmsPassword> <packagePath>");
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
     */
    @Override
    public void printHelp() {
        print("The 'dir2brms' command copies the default set of governance");
        print("workflow artifacts into your BRMS system.  This should really");
        print("only be done when you first install BRMS for use with S-RAMP.");
        print("");
        print("Example usage:");
        print("> brms:dir2brms SRAMPPackage http://localhost:8080/drools-guvnor admin admin /home/user/s-ramp-workflows.jar");
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public void execute() throws Exception {
        try {
            String brmsPackageName = optionalArgument(0, "SRAMPPackage");
            String brmsBaseUrl     = optionalArgument(1, "http://localhost:8080/drools-guvnor");
            String brmsUserId      = optionalArgument(2, "admin");
            String brmsPassword    = optionalArgument(3, "admin");
            String packagePath     = optionalArgument(4);

            print("Copying default governance package to BRMS using: ");
            print("   brmsPackageName..: %1$s", brmsPackageName);
            print("   brmsBaseUrl......: %1$s", brmsBaseUrl);
            print("   brmsUserId.......: %1$s", brmsUserId);
            print("   brmsPassword.....: %1$s", brmsPassword);
            print("   packagePath......: %1$s", packagePath);

            String brmsURLStr = brmsBaseUrl + "/rest/packages/";
            boolean brmsExists = urlExists(brmsURLStr, brmsUserId, brmsPassword);
            if (! brmsExists) {
                print("Can't find BRMS endpoint: " + brmsURLStr);
                return;
            }
            //create the package if it does not exist
            if (! urlExists(brmsURLStr + brmsPackageName, brmsUserId, brmsPassword)) {
                createNewPackage(brmsBaseUrl, brmsPackageName, brmsUserId, brmsPassword);
            }
            //add the assets
            addAssetsToPackageToBRMS(brmsBaseUrl, brmsPackageName, brmsUserId, brmsPassword, packagePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        print("**********************************************************************");
    }

    /**
     * Returns true if the given URL is accessible.
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
     * Applies authentication to the given HTTP URL connection.
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
     * A HTTP POST request to URL http://host:portnumber/repository/packages with the data:
     *
     * <entry xml:base="http://localhost:8080/repository/packages">
     *     <title>testPackage1</title>
     *     <summary>desc1</summary>
     * </entry>
     * @param brmsBaseUrl
     * @param pkgName
     * @param userId
     * @param password
     * @throws Exception
     */
    public void createNewPackage(String brmsBaseUrl, String pkgName, String userId, String password) throws Exception {
        String urlStr = brmsBaseUrl + "/rest/packages";

        Credentials credentials = new UsernamePasswordCredentials(userId, password);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
        fac = new ClientRequestFactory(clientExecutor, new URI(brmsBaseUrl));
        ClientRequest createNewPackageRequest = fac.createRequest(urlStr);
        createNewPackageRequest.accept(MediaType.APPLICATION_ATOM_XML);
        Entry entry = new Entry();
        entry.setTitle(pkgName);
        entry.setSummary("S-RAMP Package containing Governance Workflows");
        createNewPackageRequest.body(MediaType.APPLICATION_ATOM_XML, entry);
        ClientResponse<Entry> newPackageResponse = createNewPackageRequest.post(Entry.class);
        print("response status=" + newPackageResponse.getResponseStatus());
        print("Create new package with id=" + newPackageResponse.getEntity().getId());
    }

    /**
     * Uploads assets to the correct package in BRMS.
     * @param brmsBaseUrl
     * @param pkgName
     * @param userId
     * @param password
     * @param packagePath
     * @throws Exception
     */
    public void addAssetsToPackageToBRMS(String brmsBaseUrl, String pkgName, String userId, String password,
            String packagePath) throws Exception {
        String urlStr = brmsBaseUrl + "/rest/packages/" + pkgName + "/assets";
        Credentials credentials = new UsernamePasswordCredentials(userId, password);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
        fac = new ClientRequestFactory(clientExecutor, new URI(urlStr));

        if (packagePath != null) {
            File packageJar = new File(packagePath);
            if (!packageJar.isFile())
                throw new Exception("Included path to workflows package (JAR) is invalid: " + packagePath);
            addAssetsToPackageToBRMSFromJar(brmsBaseUrl, pkgName, userId, password, packageJar);
        } else {
            addAssetsToPackageToBRMSFromClasspath(brmsBaseUrl, pkgName, userId, password);
        }

        //when done compile the package
        String urlCompile = brmsBaseUrl + "/rest/packages/" + pkgName + "/binary";
        ClientRequest compileRequest = fac.createRequest(urlCompile);
        ClientResponse<InputStream> compileResponse =compileRequest.get(InputStream.class);
        if (compileResponse.getStatus()==200) {
            print("Upload complete");
        } else {
            System.err.println(compileResponse.getStatus() + " " + compileResponse.getResponseStatus().getReasonPhrase());
        }
    }

    /**
     * Finds the assets from the given JAR file.
     * @param brmsBaseUrl
     * @param pkgName
     * @param userId
     * @param password
     * @param packageJar
     */
    private void addAssetsToPackageToBRMSFromJar(String brmsBaseUrl, String pkgName, String userId,
            String password, File packageJar) throws Exception {
        String urlStr = brmsBaseUrl + "/rest/packages/" + pkgName + "/assets";

        Set<String> exclusions = new HashSet<String>();
        exclusions.add(".gitignore");
        exclusions.add(".cvsignore");

        JarFile jarFile = null;
        String assetPrefix = "governance-workflows/" + pkgName;
        try {
            jarFile = new JarFile(packageJar);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(assetPrefix) && !entry.isDirectory()) {
                    String assetName = new File(entryName).getName();
                    if (!exclusions.contains(assetName)) {
                        print("Processing workflow asset: " + entryName);
                        ClientRequest addAssetRequest = fac.createRequest(urlStr);
                        InputStream is = null;
                        try {
                            is = jarFile.getInputStream(entry);
                            print("Uploading " + entryName + " -> " + urlStr );
                            uploadToBrms(assetName, is, addAssetRequest);
                        } finally {
                            is.close();
                        }
                    }
                }
            }
        } finally {
            if (jarFile != null)
                jarFile.close();
        }
    }

    /**
     * Finds the assets from the current classpath.
     * @param brmsBaseUrl
     * @param pkgName
     * @param userId
     * @param password
     */
    private void addAssetsToPackageToBRMSFromClasspath(String brmsBaseUrl, String pkgName, String userId,
            String password) throws Exception {
        String urlStr = brmsBaseUrl + "/rest/packages/" + pkgName + "/assets";
        String dir  = "/governance-workflows/" + pkgName;
        URL url = this.getClass().getResource(dir);
        if (url==null) throw new Exception ("Could not find " + dir + " on the classpath");
        String path = url.toURI().getSchemeSpecificPart();
        File srampPackageDir = new File(path);
        if (srampPackageDir.exists()) {
            //read all files from this directory
            FilenameFilter droolsFiles = new FilenameFilter() {
                @Override
                public boolean accept(File file, String name) {
                  return !name.startsWith(".");
                }
            };
            File[] fileList = srampPackageDir.listFiles(droolsFiles);
            for (File file : fileList) {
                ClientRequest addAssetRequest = fac.createRequest(urlStr);
                InputStream is = file.toURI().toURL().openStream();
                print("uploading " + file.getName() + " -> " + urlStr );
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
                    print("uploading " + name);
                    uploadToBrms(fileName, is, addAssetRequest);
                }
            }
        }
    }

    /**
     * Uploads the given asset to BRMS.
     * @param fileName
     * @param is
     * @param addAssetRequest
     * @throws Exception
     */
    private void uploadToBrms(String fileName, InputStream is, ClientRequest addAssetRequest) throws Exception {
        addAssetRequest.body(MediaType.APPLICATION_OCTET_STREAM, is);
        addAssetRequest.accept(MediaType.APPLICATION_ATOM_XML);
        addAssetRequest.header("Slug", fileName);
        ClientResponse<String> uploadAssetResponse = addAssetRequest.post(String.class);
        int status = uploadAssetResponse.getStatus();
        @SuppressWarnings("unused")
        String response = uploadAssetResponse.getEntity();
        if (200 != status) {
            System.err.println("Upload to BRMS failed with response status = " + status);
            //print(response);
        }
    }

}
