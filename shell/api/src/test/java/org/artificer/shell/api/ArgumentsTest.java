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
package org.artificer.shell.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Arguments}.
 * @author eric.wittmann@redhat.com
 */
public class ArgumentsTest {

	private static String [][] TEST_DATA = {
		{
			"" //$NON-NLS-1$
		},
		{
			"s-ramp:connect", //$NON-NLS-1$
			"s-ramp:connect" //$NON-NLS-1$
		},
		{
			"s-ramp:connect http://localhost:8080", //$NON-NLS-1$
			"s-ramp:connect", "http://localhost:8080" //$NON-NLS-1$ //$NON-NLS-2$
		},
		{
			"cmd arg1 arg2 arg3 arg4", //$NON-NLS-1$
			"cmd", "arg1", "arg2", "arg3", "arg4" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		},
		{
			"cmd arg1 'arg2'", //$NON-NLS-1$
			"cmd", "arg1", "arg2" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		},
		{
			"cmd arg1 \"arg2\"", //$NON-NLS-1$
			"cmd", "arg1", "arg2" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		},
		{
			"cmd arg1 \"This is argument 2\"", //$NON-NLS-1$
			"cmd", "arg1", "This is argument 2" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		},
		{
			"cmd arg1 'This is argument 2'", //$NON-NLS-1$
			"cmd", "arg1", "This is argument 2" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		},
		{
			"cmd 'This is argument 1'", //$NON-NLS-1$
			"cmd", "This is argument 1" //$NON-NLS-1$ //$NON-NLS-2$
		},
		{
			"cmd 'This is argument 1' ''", //$NON-NLS-1$
			"cmd", "This is argument 1", "" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		},
		{
			"cmd 'quote \" within quote'", //$NON-NLS-1$
			"cmd", "quote \" within quote" //$NON-NLS-1$ //$NON-NLS-2$
		},
		{
			"cmd \"quote ' within quote\"", //$NON-NLS-1$
			"cmd", "quote ' within quote" //$NON-NLS-1$ //$NON-NLS-2$
		}
	};

	@Test
	public void testArguments() throws Exception {
		for (String [] testCaseData : TEST_DATA) {
			String argumentsLine = testCaseData[0];
			Arguments arguments = new Arguments(argumentsLine);
			Assert.assertEquals(testCaseData.length - 1, arguments.size());
			for (int idx = 0; idx < arguments.size(); idx++) {
				String expected = testCaseData[idx + 1];
				String actual = arguments.get(idx);
				Assert.assertEquals(expected, actual);
			}
		}

        Arguments arguments = null;
        try {
            arguments = new Arguments("query \"partial quoted arg"); //$NON-NLS-1$
            Assert.fail("Expected an InvalidCommandArgumentException here."); //$NON-NLS-1$
        } catch (InvalidCommandArgumentException e) {
            Assert.assertEquals("Invalid final argument - did you forget to close your quotes?", e.getMessage()); //$NON-NLS-1$
        }
        arguments = new Arguments("query \"partial quoted arg", true); //$NON-NLS-1$
        Assert.assertEquals(2, arguments.size());
	}

}
