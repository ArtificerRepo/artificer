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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.governance.workflow.BpmManager;
import org.overlord.sramp.governance.workflow.WorkflowFactory;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;



/**
 * 
 * @author <a href="mailto:kstam@apache.org">Kurt T Stam</a>
 *
 */
public class QueryExecutor {
    
    Governance governance = new Governance();
    BpmManager bpmManager = WorkflowFactory.newInstance();

	public void execute() {
	    SrampAtomApiClient client = new SrampAtomApiClient(governance.getSrampUrl());
	    //for all queries defined in the governance.properties file
	    Iterator<Workflow> workflowIterator = governance.getWorkflows().iterator();
	    while (workflowIterator.hasNext()) {
	        try {
    	        Workflow workflow = workflowIterator.next();
    	        String srampQuery = workflow.getSrampQuery();
                QueryResultSet queryResultSet = client.query(srampQuery);
                if (queryResultSet.size() > 0) {
                    Iterator<ArtifactSummary> queryResultIterator = queryResultSet.iterator();
                    while (queryResultIterator.hasNext()) {
                        ArtifactSummary artifactSummary = queryResultIterator.next();
                        //have this cached?
                        BaseArtifactType artifactBaseType = client.getArtifactMetaData(artifactSummary.getType(), artifactSummary.getUuid());
                        List<Property> properties = artifactBaseType.getProperty();
                        String propertyName = "workflowProcessId=" + workflow.getWorkflowId();
                        boolean hasWorkflow = false;
                        for (Property property : properties) {
                            if (property.getPropertyName().equals(propertyName)) {
                                hasWorkflow = true;
                                continue;
                            }
                        }
                        if (!hasWorkflow) {
                            //start workflow for this artifact
                            bpmManager.newProcessInstance(workflow.getWorkflowId(), workflow.getParameters());
                        }
                    }
                    
                }
	        
    	    } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
    	    }
	    }
	}

}
