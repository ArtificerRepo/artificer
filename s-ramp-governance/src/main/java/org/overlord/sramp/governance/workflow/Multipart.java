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

public class Multipart 
{
    public void post(HttpClient httpclient, URI uri, Map<String,Object> parameters) throws IOException  {
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
        System.out.println(IOUtils.toString(is));
        is.close();
    }
}
