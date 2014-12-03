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
package org.overlord.sramp.client.auth;

import org.apache.http.HttpRequest;

/**
 * Provides authentication via an HTTP header.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpHeaderAuthenticationProvider implements AuthenticationProvider {

    private String headerName;
    private String headerValue;

    /**
     * Constructor.
     * @param headerName
     */
    public HttpHeaderAuthenticationProvider(String headerName) {
        setHeaderName(headerName);
    }

    /**
     * Constructor.
     * @param headerName
     * @param headerValue
     */
    public HttpHeaderAuthenticationProvider(String headerName, String headerValue) {
        setHeaderName(headerName);
        setHeaderValue(headerValue);
    }

    @Override
    public void provideAuthentication(HttpRequest request) {
        request.setHeader(headerName, headerValue);
    }

    /**
     * @return the headerName
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * @param headerName the headerName to set
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * @return the headerValue
     */
    public String getHeaderValue() {
        return headerValue;
    }

    /**
     * @param headerValue the headerValue to set
     */
    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

}
