/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.databridge.core.conf;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.Map;

public class DataReceiver {
    // TODO: 2/3/17 not used any more
//    private String name;
//    private List<Configuration> configurations;
    private Map<String, Object> thrift;
    private Map<String, Object> binary;

//    @XmlAttribute(name = "name")
//    public String getName() {
//        return name;
//    }

//    public void setName(String name) {
//        this.name = name;
//    }

//    @XmlElement(name = "config")
//    public List<Configuration> getConfigurations() {
//        return configurations;
//    }

//    public void setConfigurations(List<Configuration> configurations) {
//        this.configurations = configurations;
//    }

//    public Object getConfiguration(String configName, Object defaultValue){
//        for (Configuration configuration: configurations){
//            if (configuration.getName().equalsIgnoreCase(configName)){
//                return configuration.getValue();
//            }
//        }
//        return defaultValue;
//    }

    public void setThrift(Map<String,Object> thrift) {
        this.thrift = thrift;
    }

    public Map<String,Object> getThrift() {
        return thrift;
    }

    public void setBinary(Map<String,Object> binary) {
        this.binary = binary;
    }

    public Map<String,Object> getBinary() {
        return binary;
    }
}
