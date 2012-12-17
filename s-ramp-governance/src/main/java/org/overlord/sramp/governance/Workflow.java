package org.overlord.sramp.governance;

public class Workflow {

    private String workflowName;
    private String targetName;
    
    public Workflow(String workflowName, String targetName) {
        super();
        this.workflowName = workflowName;
        this.targetName = targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowName() {
        return workflowName;
    }
    
    
}
