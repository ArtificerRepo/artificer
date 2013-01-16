/*
 * Copyright 2013 JBoss Inc
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

import java.util.HashMap;
import java.util.Map;

public class Query {

    private String srampQuery;
    private String workflowId;
    private String parameters;
    
    public Query(String srampQuery, String workflowId,  String parameters) {
        super();
        this.workflowId = workflowId;
        this.setSrampQuery(srampQuery);;
        setParameters(parameters);
    }

    public void setSrampQuery(String srampQuery) {
        this.srampQuery = srampQuery;
    }

    public String getSrampQuery() {
        return srampQuery;
    }
    
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowId() {
        return workflowId;
    }
    
    public Map<String,Object> getParsedParameters() {
        Map<String,Object> params = new HashMap<String,Object>();
        String[] paramStrs = parameters.split("\\:\\:");
        for (String paramStr : paramStrs) {
            String[] param = paramStr.split("=");
            params.put(param[0], param[1]);
        }
        return params;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getParameters() {
        return parameters;
    }
    
    @Override
    public String toString() {
        return "srampQuery=" + srampQuery + "\nworkflowId=" + workflowId + "\nparameters=" + getParsedParameters();
    }



}
