package org.overlord.sramp.governance;

import java.util.ArrayList;
import java.util.List;

public class Workflows {

    List<Target> targets;
    List<Workflow> workflows;
    
    public List<Target> getTargets() {
        if (targets==null) targets = new ArrayList<Target>();
        return targets;
    }
    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }
    public List<Workflow> getWorkflows() {
        if (workflows==null) workflows = new ArrayList<Workflow>();
        return workflows;
    }
    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
    }
    
    
}
