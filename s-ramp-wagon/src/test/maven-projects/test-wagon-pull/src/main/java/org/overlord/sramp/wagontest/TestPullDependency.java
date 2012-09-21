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

import org.overlord.sramp.test.wagon.Widget;

/**
 * Tests the ability to create a new instance of the generated class found in
 * the test-wagon-push project.
 *
 * @author eric.wittmann@redhat.com
 */
public class TestPullDependency {

	/**
	 * Constructor.
	 */
	public TestPullDependency() {
	}

	/**
	 * Do something with a Widget.
	 */
	public void doit() {
		Widget widget = new Widget();
		System.out.println("It's done!");
		System.out.println(widget);
	}

}
