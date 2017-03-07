/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.publisher.core.config.mapping;

import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.OutputMapping;
import org.wso2.carbon.event.publisher.core.config.EventOutputProperty;

import java.util.ArrayList;
import java.util.List;

public class MapOutputMapping extends OutputMapping {

    List<EventOutputProperty> outputPropertyConfiguration;

    public MapOutputMapping() {
        this.outputPropertyConfiguration = new ArrayList<EventOutputProperty>();

    }

    public List<EventOutputProperty> getOutputPropertyConfiguration() {
        return outputPropertyConfiguration;
    }

    public void setOutputPropertyConfiguration(
            List<EventOutputProperty> outputPropertyConfiguration) {
        this.outputPropertyConfiguration = outputPropertyConfiguration;
    }

    public void addOutputPropertyConfiguration(
            EventOutputProperty eventOutputProperty) {
        this.outputPropertyConfiguration.add(eventOutputProperty);
    }

    @Override
    public String getMappingType() {
        return EventPublisherConstants.EF_MAP_MAPPING_TYPE;
    }
}
