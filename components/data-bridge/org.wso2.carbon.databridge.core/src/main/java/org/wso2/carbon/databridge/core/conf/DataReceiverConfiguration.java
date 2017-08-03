/*
*  Copyright (c) $today.year, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.databridge.core.conf;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data receiver configuration properties
 */
@Configuration(description = "Data receiver configuration")
public class DataReceiverConfiguration {

    @Element(description = "Data receiver type", required = true)
    private String type = "";

    @Element(description = "Data receiver properties")
    private LinkedHashMap<String, String> properties = new LinkedHashMap<>();

    public String getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return ", type : " + type + "properties : " + properties.toString();
    }


    public DataReceiverConfiguration(String type, LinkedHashMap<String, String> propertiesMap) {
        this.type = type;
        this.properties = propertiesMap;
    }
}
