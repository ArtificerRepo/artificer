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
package org.artificer.repository.test.query;

import org.artificer.common.error.ArtificerUserException;
import org.artificer.repository.query.AbstractArtificerQueryImpl;
import org.artificer.repository.query.NumberReplacementParam;
import org.artificer.repository.query.QueryReplacementParam;
import org.artificer.repository.query.StringReplacementParam;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Tests the {@link AbstractArtificerQueryImpl} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractArtificerQueryImplTest {

	@Test
	public void testFormatQuery() throws ArtificerUserException {
		doFormatQueryTest("/s-ramp/xsd/XsdDocument", "/s-ramp/xsd/XsdDocument"); //$NON-NLS-1$ //$NON-NLS-2$
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@prop = ?]", //$NON-NLS-1$
				"/s-ramp/xsd/XsdDocument[@prop = 'hello-world']",  //$NON-NLS-1$
				new StringReplacementParam("hello-world")); //$NON-NLS-1$
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@size = ?]", //$NON-NLS-1$
				"/s-ramp/xsd/XsdDocument[@size = 10]",  //$NON-NLS-1$
				new NumberReplacementParam(10));
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@size = ?]", //$NON-NLS-1$
				"/s-ramp/xsd/XsdDocument[@size = 1.0]",  //$NON-NLS-1$
				new NumberReplacementParam(1.0));
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@size = ?]", //$NON-NLS-1$
				"/s-ramp/xsd/XsdDocument[@size = 12345123451234512345123451234512345]",  //$NON-NLS-1$
				new NumberReplacementParam(new BigInteger("12345123451234512345123451234512345"))); //$NON-NLS-1$
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@size = ?]", //$NON-NLS-1$
				"/s-ramp/xsd/XsdDocument[@size = 123456789012345]",  //$NON-NLS-1$
				new NumberReplacementParam(123456789012345L));

		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@prop1 = ? and @prop2 = ? and @prop3 = ?]", //$NON-NLS-1$
				"/s-ramp/xsd/XsdDocument[@prop1 = 'val1' and @prop2 = 'val2' and @prop3 = 17]",  //$NON-NLS-1$
				new StringReplacementParam("val1"), //$NON-NLS-1$
				new StringReplacementParam("val2"), //$NON-NLS-1$
				new NumberReplacementParam(17));
	}

	@Test(expected=ArtificerUserException.class)
	public void testFormatQuery_tooManyParams() throws ArtificerUserException {
		doFormatQueryTest("/s-ramp/xsd/XsdDocument", null, new StringReplacementParam("val1")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test(expected=ArtificerUserException.class)
	public void testFormatQuery_notEnoughParams() throws ArtificerUserException {
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@prop1 = ? or @prop2 = ?]", null, new StringReplacementParam("val1")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Does a single formatQuery test case.
	 * @param xpathTemplate
	 * @param expectedXpath
	 * @param params
	 * @throws ArtificerUserException
	 */
	private void doFormatQueryTest(String xpathTemplate, String expectedXpath,
			QueryReplacementParam<?>... params) throws ArtificerUserException {
		String formattedQuery = AbstractArtificerQueryImpl.formatQuery(xpathTemplate, Arrays.asList(params));
		Assert.assertEquals(expectedXpath, formattedQuery);
	}

}
