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
package org.overlord.sramp.ui.client.views;

import org.overlord.sramp.ui.client.activities.IActivity;

/**
 * Views that show a paged result set should implement this interface.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IPagedResultView<A extends IActivity> extends IView<A> {

	/**
	 * Gets the default page size for the list of artifacts.
	 */
	public int getDefaultPageSize();
	
	/**
	 * Gets the default column name to order the results by (when no order-by is specified).
	 */
	public String getDefaultOrderBy();

}
