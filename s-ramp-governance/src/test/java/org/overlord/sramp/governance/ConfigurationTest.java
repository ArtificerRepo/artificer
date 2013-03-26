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

import junit.framework.Assert;

import org.junit.Test;


/**
 * Tests the Configuration.
 *
 * @author kurt.stam@redhat.com
 */
public class ConfigurationTest {

	/**
	 * @throws ConfigException 
	 */
    @Test
	public void testConfigure() throws ConfigException {
	    Governance governance = new Governance();
	    Map<String,Target> targets = governance.getTargets();
	    Assert.assertTrue(targets.size() > 0);
	    Set<Query> queries = governance.getQueries();
	    Assert.assertTrue(queries.size() > 0);
	    System.out.println(governance.validate());
	}
    /**
     * Add a malform url
     * @throws ConfigException
     */
    @Test
    public void testBad1UrlConfiguration() throws ConfigException {
        System.setProperty(GovernanceConstants.GOVERNANCE_FILE_NAME, "bad1-governance.config.txt");
        Governance governance = new Governance();
        governance.read();
        try {
            System.out.println(governance.validate());
            Assert.fail("Expecting exception");
        } catch (ConfigException e) {
            Assert.assertEquals("java.net.MalformedURLException: no protocol: http//localhost:8080/s-ramp-atom",e.getMessage());
        }
    }
    /**
     * Add a bad query
     * 
     * @throws ConfigException
     */
    @Test() 
    public void testBad2QueryConfiguration() throws ConfigException {
        System.setProperty(GovernanceConstants.GOVERNANCE_FILE_NAME, "bad2-governance.config.txt");
        Governance governance = new Governance();
        governance.read();
        try {
            governance.validate();
            Assert.fail("Expecting exception");
        } catch (ConfigException e) {
            Assert.assertTrue(e.getMessage().startsWith(Governance.QUERY_ERROR));
        }
    }
    /**
     * Add a bad target
     * 
     * @throws ConfigException
     */
    @Test() 
    public void testBad3TargetConfiguration() throws ConfigException {
        System.setProperty(GovernanceConstants.GOVERNANCE_FILE_NAME, "bad3-governance.config.txt");
        Governance governance = new Governance();
        governance.read();
        try {
            governance.validate();
            Assert.fail("Expecting exception");
        } catch (ConfigException e) {
            Assert.assertTrue(e.getMessage().startsWith(Governance.TARGET_ERROR));
        }
    }
}
