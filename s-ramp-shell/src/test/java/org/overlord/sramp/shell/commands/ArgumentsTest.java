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
package org.overlord.sramp.shell.commands;

import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.shell.commands.Arguments;

/**
 * Unit test for {@link Arguments}.
 * @author eric.wittmann@redhat.com
 */
public class ArgumentsTest {

	private static String [][] TEST_DATA = {
		{
			""
		},
		{
			"s-ramp:connect",
			"s-ramp:connect"
		},
		{
			"s-ramp:connect http://localhost:8080",
			"s-ramp:connect", "http://localhost:8080"
		},
		{
			"cmd arg1 arg2 arg3 arg4",
			"cmd", "arg1", "arg2", "arg3", "arg4"
		},
		{
			"cmd arg1 'arg2'",
			"cmd", "arg1", "arg2"
		},
		{
			"cmd arg1 \"arg2\"",
			"cmd", "arg1", "arg2"
		},
		{
			"cmd arg1 \"This is argument 2\"",
			"cmd", "arg1", "This is argument 2"
		},
		{
			"cmd arg1 'This is argument 2'",
			"cmd", "arg1", "This is argument 2"
		},
		{
			"cmd 'This is argument 1'",
			"cmd", "This is argument 1"
		},
		{
			"cmd 'This is argument 1' ''",
			"cmd", "This is argument 1", ""
		},
		{
			"cmd 'argument",
			"cmd"
		},
		{
			"cmd 'quote \" within quote'",
			"cmd", "quote \" within quote"
		},
		{
			"cmd \"quote ' within quote\"",
			"cmd", "quote ' within quote"
		}
	};

	/**
	 * Test method for {@link org.overlord.sramp.common.shell.commands.Arguments#Arguments(java.lang.String)}.
	 */
	@Test
	public void testArguments() {
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
	}

}
