/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.governance.workflow;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.overlord.sramp.governance.workflow.WorkflowFactory;
import org.overlord.sramp.governance.workflow.brms.JbpmManager;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class WorkflowFactoryTest {

    @Test
    public void testFindServiceConfig() {
        URL url = this.getClass().getClassLoader().getResource("META-INF/services/org.overlord.sramp.governance.workflow.BpmManager");
//        System.out.println("URL=" + url);
        Assert.assertNotNull(url);
    }

    @Test
    public void testPersistenceFactory() throws Exception {
        BpmManager bpmManager = WorkflowFactory.newInstance();
        Assert.assertEquals(JbpmManager.class, bpmManager.getClass());
    }

    @Test @Ignore  //the BPM engine needs to be running for this test to pass
    public void testNewProcessInstance() throws Exception {
        BpmManager bpmManager = WorkflowFactory.newInstance();
        String processId = "com.sample.evaluation";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("employee", "krisv");
        parameters.put("reason", "just bc");
        parameters.put("uuid", "some-uuid-" + new Date());
        bpmManager.newProcessInstance(processId, parameters);
    }
}
