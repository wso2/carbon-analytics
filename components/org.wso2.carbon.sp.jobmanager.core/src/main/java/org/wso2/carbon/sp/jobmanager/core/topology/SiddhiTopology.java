/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.jobmanager.core.topology;

import java.util.List;

/**
 * Distributed Topology of a user defined distributed Siddhi App. Topology contains all the required details for the
 * underlying distribution provider to create new Apps and deploy them on available resources.
 */
public class SiddhiTopology {
    private String name;
    private List<SiddhiQueryGroup> queryGroupList;
    private boolean transportChannelCreationEnabled;

    public SiddhiTopology(String name,
                          List<SiddhiQueryGroup> queryGroupList, boolean transportChannelCreationEnabled) {
        this.name = name;
        this.queryGroupList = queryGroupList;
        this.transportChannelCreationEnabled = transportChannelCreationEnabled;
    }


    public String getName() {
        return name;
    }

    public List<SiddhiQueryGroup> getQueryGroupList() {
        return queryGroupList;
    }

    public boolean isTransportChannelCreationEnabled() {
        return transportChannelCreationEnabled;
    }
}
