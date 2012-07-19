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
package org.overlord.sramp.repository.query;


/**
 * A single replacement value in an {@link SrampQuery}.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class QueryReplacementParam<T> {

	private T value;
	
	/**
	 * Constructor.
	 * @param value
	 */
	public QueryReplacementParam(T value) {
		setValue(value);
	}

	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(T value) {
		this.value = value;
	}
	
	/**
	 * Called to format and return the value of the replacement param.
	 */
	public abstract String getFormattedValue();
	
}
