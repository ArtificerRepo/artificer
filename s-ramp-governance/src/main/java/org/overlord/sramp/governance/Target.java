package org.overlord.sramp.governance;

public class Target {

    public Target(String name, String deployDir) {
        super();
        this.name = name;
        this.deployDir = deployDir;
    }

    private String name;
    private String deployDir;
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setDeployDir(String deployDir) {
        this.deployDir = deployDir;
    }
    
    public String getDeployDir() {
        return deployDir;
    }

}
