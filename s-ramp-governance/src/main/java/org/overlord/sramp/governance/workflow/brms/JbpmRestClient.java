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
package org.overlord.sramp.governance.workflow.brms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.overlord.sramp.governance.workflow.Multipart;
import org.overlord.sramp.governance.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmRestClient {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
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
            if (log.isDebugEnabled()) {
                log.debug(IOUtils.toString(is));
            }
            is.close();
        }
    }

    public void newProcessInstanceAndCompleteFirstTask(String processId, Map<String,Object> params) throws IOException, URISyntaxException, WorkflowException {
        //"http://localhost:8080/gwt-console-server/rs/form/process/com.sample.evaluation/complete"
        URI sendTaskFormUrl = new URL(jbpmUrl + "/rs/form/process/" + processId + "/complete").toURI();
        new Multipart().post(httpclient, sendTaskFormUrl, params);
    }
    /**
     * Creates a new jBPM5 process instance, given the processId.
     * @param processId
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void newProcessInstance(String processId) throws URISyntaxException, IOException {
        //"http://localhost:8080/gwt-console-server/rs/process/definition/{id}/new_instance"
        URI startProcessUrl = new URL(jbpmUrl + "/rs/process/definition/" + processId + "/new_instance").toURI();
        HttpPost newInstance = new HttpPost(startProcessUrl);
        httpclient.execute(newInstance);
    }
}
