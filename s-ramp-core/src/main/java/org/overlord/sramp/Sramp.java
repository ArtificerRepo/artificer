package org.overlord.sramp;

import java.net.URL;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Sramp {

    private Logger log = LoggerFactory.getLogger(this.getClass());
            
    public Sramp() {
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
            String configFile = config.getString(SrampConstants.SRAMP_CONFIG_FILE_NAME, "/sramp.properties");
            Long refreshDelay = config.getLong(SrampConstants.SRAMP_CONFIG_FILE_REFRESH, 5000l);
            URL url = Sramp.class.getClassLoader().getResource(configFile);
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
    
    public String getBaseUrl(String requestUrl) {
        String baseUrl = "http://localhost:8080/s-ramp-atom";
        if (requestUrl!=null) {
            int index = requestUrl.indexOf("/s-ramp/");
            if (index < 0) index = requestUrl.indexOf("/s-ramp");
            if (index > 0) {
                baseUrl = requestUrl.substring(0, index);
            }
        }
        return configuration.getString(SrampConstants.SRAMP_CONFIG_BASEURL, baseUrl);
    }
    
    
}
