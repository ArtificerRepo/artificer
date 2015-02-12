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
package org.artificer.ui.server.servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 * A base class for upload servlets.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractUploadServlet extends HttpServlet {

	private static final long serialVersionUID = AbstractUploadServlet.class.hashCode();

	/**
	 * Constructor.
	 */
	public AbstractUploadServlet() {
	}

	/**
	 * Make a temporary copy of the resource by saving the content to a temp file.
	 * @param resourceInputStream
	 * @throws IOException
	 */
	protected File stashResourceContent(InputStream resourceInputStream) throws IOException {
		File resourceTempFile = null;
		OutputStream oStream = null;
		try {
			resourceTempFile = File.createTempFile("artificer-ui-upload", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
			oStream = FileUtils.openOutputStream(resourceTempFile);
            IOUtils.copy(resourceInputStream, oStream);
            return resourceTempFile;
		} catch (IOException e) {
			FileUtils.deleteQuietly(resourceTempFile);
			throw e;
		} finally {
			IOUtils.closeQuietly(resourceInputStream);
			IOUtils.closeQuietly(oStream);
		}
	}

	/**
	 * Writes the response values back to the http response.  This allows the calling code to
	 * parse the response values for display to the user.
	 *
	 * @param responseMap the response params to write to the http response
	 * @param response the http response
	 * @throws IOException
	 */
	protected static void writeToResponse(Map<String, String> responseMap, HttpServletResponse response) throws IOException {
        // Note: setting the content-type to text/html because otherwise IE prompt the user to download
        // the result rather than handing it off to the GWT form response handler.
        // See JIRA issue https://issues.jboss.org/browse/SRAMPUI-103
		response.setContentType("text/html; charset=UTF8"); //$NON-NLS-1$
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
        g.useDefaultPrettyPrinter();
        g.writeStartObject();
        for (java.util.Map.Entry<String, String> entry : responseMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            g.writeStringField(key, val);
        }
        g.writeEndObject();
        g.flush();
        g.close();
	}
}
