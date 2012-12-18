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
package org.overlord.sramp.governance;

import java.util.Map;
import java.util.Set;

import org.junit.Test;


/**
 * Tests the Configuration.
 *
 * @author kurt.stam@redhat.com
 */
public class ConfigurationTest {

	/**
	 */
	@SuppressWarnings("unused")
    @Test
	public void testConfigure() {
	    Governance governance = new Governance();
	    Map<String,Target> targets = governance.getTargets();
	    Set<Workflow> workflows = governance.getWorkflows();
	    System.out.println("ok");
	}
}
