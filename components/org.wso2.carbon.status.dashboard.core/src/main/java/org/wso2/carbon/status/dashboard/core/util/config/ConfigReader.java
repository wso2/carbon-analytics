package org.wso2.carbon.status.dashboard.core.util.config;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Status Dashboard Configuration Reader.
 */
public class ConfigReader {
    static final Logger LOGGER = Logger.getLogger(ConfigReader .class);
    Map<String, String> propertyMap;

    public ConfigReader(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public String readConfig(String name, String defaultValue) {
        if (null != propertyMap && !propertyMap.isEmpty()) {
            String property = propertyMap.get(name);
            if (null != property && !property.isEmpty()) {
                return property;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Couldn't find a matching property for name: " + name + ", returning a default value: " +
                    defaultValue + "!");
        }
        return defaultValue;
    }

    public Map<String, String> getAllConfigs() {
        if (null != propertyMap) {
            return propertyMap;
        } else {
            return new HashMap<>();
        }
    }
}
