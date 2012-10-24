package org.overlord.sramp.governance;

import java.net.URL;

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
            String configFile = config.getString(GovernanceConstants.GOVERNANCE_CONFIG_FILE_NAME, "/governance.properties");
            Long refreshDelay = config.getLong(GovernanceConstants.GOVERNANCE_CONFIG_FILE_REFRESH, 5000l);
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
        return configuration.getString(GovernanceConstants.GOVERNANCE_CONFIG_JBPM_USER, "admin");
    }
    
    public String getJbpmPassword() {
        return configuration.getString(GovernanceConstants.GOVERNANCE_CONFIG_JBPM_PASSWORD, "admin");
    }
    
    public String getJbpmUrl() {
        return configuration.getString(GovernanceConstants.GOVERNANCE_CONFIG_JBPM_URL, "http://localhost:8080/gwt-console-server");
    }
    
}
