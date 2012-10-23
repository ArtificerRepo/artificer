package org.overlord.sramp.govenance;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class JbpmRestClient {
    
    String jbpmUrl = null;
    HttpClient httpclient = null;
    
    public JbpmRestClient(HttpClient httpclient, String jbpmUrl) {
        super();
        this.jbpmUrl = jbpmUrl;
        this.httpclient = httpclient;
    }

    public void logon(String username, String password) throws ClientProtocolException, IOException {

        HttpGet getMethod = new HttpGet(jbpmUrl + "/rs/process/definitions");
        HttpResponse response = httpclient.execute(getMethod);
        InputStream is = response.getEntity().getContent();
        String responseStr = IOUtils.toString(is);
        is.close();
        if (responseStr.contains("j_security_check")) {
            HttpPost authMethod = new HttpPost(jbpmUrl + "/rs/process/j_security_check");
            List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
            parameters.add(new BasicNameValuePair("j_username", username));
            parameters.add(new BasicNameValuePair("j_password", password));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
            authMethod.setEntity(entity);
            response = httpclient.execute(authMethod);
            response.getEntity().getContent().close();
            response = httpclient.execute(getMethod);
            is = response.getEntity().getContent();
            System.out.println(IOUtils.toString(is));
            is.close();
        }
    }

    public void newProcessInstanceAndCompleteFirstTask(String processId, Map<String,String> params) throws URISyntaxException, ClientProtocolException, IOException {
        //"http://localhost:8080/gwt-console-server/rs/form/process/com.sample.evaluation/complete"
        URI startProcessUrl = new URL(jbpmUrl + "/rs/form/process/" + processId + "/complete").toURI();
        List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
        for (String key : params.keySet()) {
            parameters.add(new BasicNameValuePair(key, params.get(key)));
        }
        new Multipart().post(httpclient, startProcessUrl, parameters);
    }
    /**
     * Creates a new jBPM5 process instance, given the processId.
     * @param processId
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void newProcessInstance(String processId) throws URISyntaxException, ClientProtocolException, IOException {
        //"http://localhost:8080/gwt-console-server/rs/process/definition/{id}/new_instance"
        URI startProcessUrl = new URL(jbpmUrl + "/rs/process/definition/" + processId + "/new_instance").toURI();
        HttpPost newInstance = new HttpPost(startProcessUrl);
        httpclient.execute(newInstance);
    }

    public static void main(String [ ] args) throws Exception {
        
        HttpClient httpclient = new DefaultHttpClient();
        JbpmRestClient jbpmClient = new JbpmRestClient(httpclient, "http://localhost:8080/gwt-console-server");
        try {
            jbpmClient.logon("admin", "admin");
            //parameters that will be set in the jBPM context Map
            Map<String,String> parameters = new HashMap<String,String>();
            parameters.put("employee", "krisv");
            parameters.put("reason", "just bc");
            parameters.put("uuid", "some-uuid-lkjlkj");
            jbpmClient.newProcessInstanceAndCompleteFirstTask("com.sample.evaluation",parameters);
            jbpmClient.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jbpmClient.shutdown();
        }
    }
    
    // shuts down the httpclient in use by this client
    public void shutdown() {
        if (httpclient!=null) {
            httpclient.getConnectionManager().shutdown();
        }
    }
}
