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
package org.artificer.atom.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/**
 * Simple bean class that models an HTTP response.  This class is used by the Atom layer's
 * batch functionality in order to return the values defined in the S-RAMP specification (a
 * list of HTTP responses as multipart/mixed content).
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpResponseBean {

	private int code;
	private String status;
	private Map<String, String> headers = new HashMap<String, String>();
	private Object body;
	private MediaType bodyType;

	/**
	 * Constructor.
	 * @param code
	 * @param status
	 */
	public HttpResponseBean(int code, String status) {
		this.code = code;
		this.status = status;
		setHeader("Date", (new Date()).toString()); //$NON-NLS-1$
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Sets a single header.
	 * @param name
	 * @param value
	 */
	public void setHeader(String name, String value) {
		getHeaders().put(name, value);
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	/**
	 * @return the body
	 */
	public Object getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 * @param type the content type of the body
	 */
	public void setBody(Object body, MediaType type) {
		this.body = body;
		this.bodyType = type;
		setHeader("Content-Type", type.toString()); //$NON-NLS-1$
	}

	/**
	 * Returns the content type of the body.
	 */
	public MediaType getBodyType() {
		return bodyType;
	}

	/**
	 * Returns the content type of the body.
	 * @param type
	 */
	public void setBodyType(MediaType type) {
		bodyType = type;
	}
}
