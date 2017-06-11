/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.stream.processor.common.utils.config;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.util.Map;

/**
 * Siddhi file configuration reader.
 */
public class FileConfigReader implements ConfigReader {
    static final Logger LOGGER = Logger.getLogger(FileConfigReader.class);
    Map<String, String> propertyMap;

    public FileConfigReader(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }

    @Override
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
}
