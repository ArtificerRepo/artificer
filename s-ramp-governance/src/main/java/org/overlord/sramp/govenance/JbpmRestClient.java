package org.overlord.sramp.govenance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class JbpmRestClient {

    public boolean urlExists(String checkUrl, String user, String password) {
        
        try {
            URL checkURL = new URL(checkUrl);
            HttpURLConnection checkConnection = (HttpURLConnection) checkURL.openConnection();
            checkConnection.setRequestMethod("GET");
            checkConnection.setRequestProperty("Accept", "application/json");
            checkConnection.setConnectTimeout(10000);
            checkConnection.setReadTimeout(10000);
            //applyAuth(checkConnection, user, password);
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
    
private void logon(String jbpmUrl, Map<String,String> params) throws MalformedURLException {
        
        URL logonUrl = new URL(jbpmUrl + "/rs/identity/secure/j_security_check");
        postMultipart(logonUrl, params);
    }
    
    private void newProcessInstance(String jbpmUrl, Map<String,String> params, String processId) throws MalformedURLException {
        
        URL startProcessUrl = new URL(jbpmUrl + "/rs/form/process/" + processId + "/complete");
        postMultipart(startProcessUrl, params);
    }
    
    private void postMultipart(URL url, Map<String,String> params) {
        //need to make a mutipartForm request
        //http://localhost:8080/gwt-console-server/rs/form/process/com.sample.evaluation/render
        try {
            
            HttpURLConnection jbpmConnection = (HttpURLConnection) url.openConnection();
            jbpmConnection.setRequestMethod("POST");
            jbpmConnection.setRequestProperty("Accept", "text/html");
            
            Multipart mmp = new Multipart();
            for (String key : params.keySet()) {
                mmp.putStandardParam( key, params.get(key), "UTF-8" );
            }
            mmp.finish();
            jbpmConnection.setRequestProperty("Content-Length", 
                    Integer.toString(mmp.getLength()));
            jbpmConnection.setRequestProperty("Content-Type", 
                    "multipart/form-data, boundary="
                    +mmp.getBoundary());
            jbpmConnection.setConnectTimeout(10000);
            jbpmConnection.setReadTimeout(10000);
            jbpmConnection.setDoOutput(true);
            jbpmConnection.setUseCaches(false);
            //applyAuth(jbpmConnection, user, password);
            jbpmConnection.connect();
            System.out.println(url);
            System.out.println(mmp.getContent());
            
            OutputStream output = jbpmConnection.getOutputStream();
            output.write( mmp.getContent().getBytes() );
            output.flush();
            output.close();
            printResponse(jbpmConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void printResponse( 
            URLConnection conn )
        {
            try
            {
                InputStream is = conn.getInputStream();
                while ( is.available() != 0 )
                {
                    byte[] data = new byte[is.available()];
                    is.read( data );
                    System.out.println(new String(data,
                       "UTF-8"));
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace( System.out );
            }
        }
    
    public static void main(String [ ] args) {
        org.apache.http.client.HttpClient httpclient = new DefaultHttpClient();
        try {
        HttpGet getMethod = new HttpGet("http://localhost:8080/gwt-console-server/rs/process/definitions");
        
        HttpPost authMethod = new HttpPost("http://localhost:8080/gwt-console-server/rs/process/j_security_check");
        List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
        parameters.add(new BasicNameValuePair("j_username", "admin"));
        parameters.add(new BasicNameValuePair("j_password", "admin"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
        authMethod.setEntity(entity);
          
              HttpResponse response = httpclient.execute(getMethod);
              response.getEntity().getContent().close();
              response = httpclient.execute(authMethod);
              response.getEntity().getContent().close();
              response = httpclient.execute(getMethod);
              InputStream is = response.getEntity().getContent();
              System.out.println(IOUtils.toString(is));
              is.close();
              
              parameters = new ArrayList<BasicNameValuePair>();
              parameters.add(new BasicNameValuePair("employee", "krisv"));
              parameters.add(new BasicNameValuePair("reason", "just bc"));
              MultipartEntity multiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
              Iterator<BasicNameValuePair> iter = parameters.iterator();
              while (iter.hasNext()) {
                  BasicNameValuePair nvp = iter.next();
                  StringBody stringBody = new StringBody(nvp.getValue(), "text/plain", Charset.forName("UTF-8"));
                  multiPartEntity.addPart(nvp.getName(), (ContentBody) stringBody);
              }
              HttpPost httpPost = new HttpPost("http://localhost:8080/gwt-console-server/rs/form/process/com.sample.evaluation/complete");
              httpPost.setEntity(multiPartEntity);
              response = httpclient.execute(httpPost);
              is = response.getEntity().getContent();
              System.out.println(IOUtils.toString(is));
              is.close();
              httpclient.getConnectionManager().shutdown();
          } catch (IOException e) {
              e.printStackTrace();
          } finally {
              
          }
        
//        try {
//            String processId = "com.sample.evaluation";
//            String jbpmUrl   = "http://localhost:8080/gwt-console-server";
//            
//            if (args.length > 0) processId = args[0];
//            if (args.length > 1) jbpmUrl   = args[1];
//            
//            JbpmRestClient jbpm = new JbpmRestClient();
//            //http://localhost:8080/gwt-console-server/rs/process/definition/com.sample.evaluation/new_instance
//            
//            String jbpmURLStr = jbpmUrl + "/rs/process/definitions";
//            boolean jbpmExists = jbpm.urlExists(jbpmURLStr, "admin", "admin");
//            if (! jbpmExists) {
//                System.out.println("Can't find jBPM REST endpoint: " + jbpmURLStr);
//                return;
//            }
//            Map<String,String> requestMap = new HashMap<String,String>();
//            requestMap.put("j_username","admin");
//            requestMap.put("j_password","admin");
//            
//            jbpm.logon(jbpmUrl, requestMap);
//            
//            requestMap = new HashMap<String,String>();
//            requestMap.put("employee", "krisv");
//            requestMap.put("reason", "just because");
//            jbpm.newProcessInstance(jbpmUrl, requestMap, processId);
//       
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
