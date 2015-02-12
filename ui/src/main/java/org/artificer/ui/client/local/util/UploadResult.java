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

package org.artificer.ui.client.local.util;

import org.artificer.ui.client.shared.exceptions.ArtificerUiException;
import org.artificer.ui.server.servlets.ArtifactUploadServlet;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The upload servlets return a JSON map as the response.
 * @author eric.wittmann@redhat.com
 */
public class UploadResult extends JavaScriptObject {

    /**
     * Constructor.
     */
    protected UploadResult() {
    }

    /**
     * Convert the string returned by the {@link ArtifactUploadServlet} into JSON and
     * then from there into an {@link UploadResult} bean.
     * @param resultData
     */
    public static UploadResult fromResult(String resultData) {
        int startIdx = resultData.indexOf('{');
        int endIdx = resultData.lastIndexOf('}') + 1;
        resultData = "(" + resultData.substring(startIdx, endIdx) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        return fromJSON(UploadResult.class, resultData);
    }

    /**
     * Gets a value from the map.
     * @param key
     */
    public final native String get(String key) /*-{
        if (this[key])
            return this[key];
        else
            return null;
    }-*/;

    /**
     * Returns true if the response is an error response.
     */
    public final boolean isError() {
        return "true".equals(get("exception")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Gets the error.
     */
    public final ArtificerUiException getError() {
        String errorMessage = get("exception-message"); //$NON-NLS-1$
        ArtificerUiException error = new ArtificerUiException(errorMessage);
        return error;
    }

    /**
     * Convert a string of json data into a useful bean.
     * @param jsonData
     */
    public static final native <T extends UploadResult> T fromJSON(Class<T> rtype, String jsonData) /*-{ return eval(jsonData); }-*/;

}