package org.overlord.sramp.governance;

import java.util.HashMap;
import java.util.Map;

public class Workflow {

    private String srampQuery;
    private String workflowId;
    private Map<String,Object> parameters = new HashMap<String,Object>();
    
    public Workflow(String srampQuery, String workflowId,  String parameters) {
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
    
    public void setParameters(String parameters) {
        String[] paramStrs = parameters.split("\\:\\:");
        for (String paramStr : paramStrs) {
            String[] param = paramStr.split("=");
            this.parameters.put(param[0], param[1]);
        }
    }

    public void setParameters(Map<String,Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String,Object> getParameters() {
        return parameters;
    }



}
