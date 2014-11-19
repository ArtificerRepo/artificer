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
package org.overlord.sramp.common.query.xpath;

import org.overlord.sramp.common.SrampConstants;


/**
 * A default namespace context for resolving prefixes to namespaces in an S-RAMP
 * X-Path formatted Query.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultNamespaceContext extends StaticNamespaceContext {

	/**
	 * Default constructor.
	 */
	public DefaultNamespaceContext() {
		addMapping(SrampConstants.SRAMP_PREFIX, SrampConstants.SRAMP_NS);
		addMapping("xs", "http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$ //$NON-NLS-2$
		addMapping("fn", "http://www.w3.org/2005/xpath-functions"); //$NON-NLS-1$ //$NON-NLS-2$
		addMapping("xp2", "http://www.w3.org/2005/xpath-functions"); //$NON-NLS-1$ //$NON-NLS-2$
		addMapping("err", "http://www.w3.org/2005/xqt-error"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
