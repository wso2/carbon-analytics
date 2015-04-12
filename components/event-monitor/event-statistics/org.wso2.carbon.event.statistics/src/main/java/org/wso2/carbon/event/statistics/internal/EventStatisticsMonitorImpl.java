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
package org.wso2.carbon.event.statistics.internal;

import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.statistics.internal.counter.StatsCounter;

public class EventStatisticsMonitorImpl implements EventStatisticsMonitor {

    private StatsCounter tenantData;
    private StatsCounter categoryData;
    private StatsCounter deploymentData;
    private StatsCounter elementData;

    public EventStatisticsMonitorImpl(StatsCounter tenantData, StatsCounter categoryData, StatsCounter deploymentData, StatsCounter elementData) {
        this.tenantData = tenantData;
        this.categoryData = categoryData;
        this.deploymentData = deploymentData;
        this.elementData = elementData;
    }

    public void incrementRequest() {
        this.tenantData.incrementRequest();
        this.categoryData.incrementRequest();
        this.deploymentData.incrementRequest();
        if (elementData != null) {
            this.elementData.incrementRequest();
        }

    }

    public void incrementResponse() {
        this.tenantData.incrementResponse();
        this.categoryData.incrementResponse();
        this.deploymentData.incrementResponse();
        if (elementData != null) {
            this.elementData.incrementResponse();
        }
    }

}
