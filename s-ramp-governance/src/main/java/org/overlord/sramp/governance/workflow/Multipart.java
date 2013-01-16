/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.governance.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Multipart 
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public void post(HttpClient httpclient, URI uri, Map<String,Object> parameters) throws IOException, WorkflowException {
        MultipartEntity multiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        
        for (String key : parameters.keySet()) {
            ContentBody content = null;
            Object param = parameters.get(key);
            if (param instanceof String) {
                StringBody stringBody = new StringBody((String) param, "text/plain", Charset.forName("UTF-8"));
                content = (ContentBody) stringBody;
            } else {
                //turn object into byteArray, or it also supports InputStreamBody or FileBody
                ByteArrayBody byteBody = new ByteArrayBody(null, key);
                content = (ContentBody) byteBody;
            }
            multiPartEntity.addPart(key, content);
        }
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(multiPartEntity);
        HttpResponse response = httpclient.execute(httpPost);
        InputStream is = response.getEntity().getContent();
        String responseStr = IOUtils.toString(is);
        if (response.getStatusLine().getStatusCode()==200 || response.getStatusLine().getStatusCode()==201) {
            logger.debug(responseStr);
        } else {
            throw new WorkflowException("Workflow ERROR - HTTP STATUS CODE " + response.getStatusLine().getStatusCode() + ". " 
                    + response.getStatusLine().getReasonPhrase() + ". " + responseStr);
        }
        is.close();
    }
}
