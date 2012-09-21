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
package org.overlord.sramp.wagontest;

import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
public class TestPullDependencyTest {

	/**
	 * Test method for {@link org.overlord.sramp.wagontest.TestPullDependency#doit()}.
	 */
	@Test
	public void testDoit() {
		TestPullDependency tpd = new TestPullDependency();
		tpd.doit();
		System.out.println("DONE: success");
	}

}
