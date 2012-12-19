package org.overlord.sramp.governance;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Governance {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Governance() {
        super();
        if (configuration == null) {
            read();
        }
    }

    private static Configuration configuration = null;

    public synchronized void read() {
        try {
            CompositeConfiguration config = new CompositeConfiguration();
            config.addConfiguration(new SystemConfiguration());
            //config.addConfiguration(new JNDIConfiguration("java:comp/env/overlord/s-ramp"));
            String configFile = config.getString(GovernanceConstants.GOVERNANCE_FILE_NAME, "governance.properties");
            Long refreshDelay = config.getLong(GovernanceConstants.GOVERNANCE_FILE_REFRESH, 5000l);
            URL url = Governance.class.getClassLoader().getResource(configFile);
            if (url==null) {
                log.warn("Cannot find " + configFile);
            } else {
                PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(url);
                FileChangedReloadingStrategy fileChangedReloadingStrategy = new FileChangedReloadingStrategy();
                fileChangedReloadingStrategy.setRefreshDelay(refreshDelay);
                propertiesConfiguration.setReloadingStrategy(fileChangedReloadingStrategy);
                config.addConfiguration(propertiesConfiguration);
            }
            configuration = config;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getJbpmUser() {
        return configuration.getString(GovernanceConstants.GOVERNANCE_JBPM_USER, "admin");
    }

    public String getJbpmPassword() {
        return configuration.getString(GovernanceConstants.GOVERNANCE_JBPM_PASSWORD, "admin");
    }

    public String getJbpmUrl() {
        return configuration.getString(GovernanceConstants.GOVERNANCE_JBPM_URL, "http://localhost:8080/gwt-console-server");
    }

    public String getSrampUrl() {
        return configuration.getString(GovernanceConstants.SRAMP_REPO_URL, "http://localhost:8080/s-ramp-atom") + "/s-ramp";
    }

    public Map<String,Target> getTargets() {
        Map<String,Target> targets = new HashMap<String,Target>();
        String[] targetStrings = configuration.getStringArray(GovernanceConstants.GOVERNANCE_TARGETS);
        for (String targetString : targetStrings) {
            String[] info = targetString.split("\\|");
            Target target = new Target(info[0],info[1]);
            targets.put(target.getName(), target);
        }
        return targets;
    }

    public Set<Workflow> getWorkflows() {
        Set<Workflow> workflows = new HashSet<Workflow>();
        String[] workflowStrings = configuration.getStringArray(GovernanceConstants.GOVERNANCE_WORKFLOWS);
        for (String workflowString : workflowStrings) {
            String[] info = workflowString.split("\\|");
            Workflow workflow = new Workflow(info[0],info[1],info[2]);
            workflows.add(workflow);
        }
        return workflows;
    }

}
