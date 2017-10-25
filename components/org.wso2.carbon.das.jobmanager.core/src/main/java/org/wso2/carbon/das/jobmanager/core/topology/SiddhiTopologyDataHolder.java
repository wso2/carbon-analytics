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

package org.wso2.carbon.das.jobmanager.core.topology;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Data holder for data required for {@link SiddhiTopologyCreatorImpl} which is responsible for creating
 * {@link SiddhiTopology}
 */
public class SiddhiTopologyDataHolder {

    private String siddhiAppName;
    private String userDefinedSiddhiApp;
    private Map<String, String> inMemoryMap;
    private Map<String, LinkedList<String>> partitionKeyMap;
    private Map<String, LinkedList<String>> partitionGroupMap;
    private Map<String, SiddhiQueryGroup> siddhiQueryGroupMap;

    public SiddhiTopologyDataHolder(String siddhiAppName, String userDefinedSiddhiApp) {
        this.siddhiAppName = siddhiAppName;
        this.userDefinedSiddhiApp = userDefinedSiddhiApp;
        this.siddhiQueryGroupMap = new LinkedHashMap<>();
        this.partitionKeyMap = new HashMap<>();
        this.partitionGroupMap = new HashMap<>();
        this.inMemoryMap = new HashMap<>();
    }

    public Map<String, String> getInMemoryMap() {
        return inMemoryMap;
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public String getUserDefinedSiddhiApp() {
        return userDefinedSiddhiApp;
    }

    public Map<String, SiddhiQueryGroup> getSiddhiQueryGroupMap() {
        return siddhiQueryGroupMap;
    }

    public Map<String, LinkedList<String>> getPartitionKeyMap() {
        return partitionKeyMap;
    }

    public Map<String, LinkedList<String>> getPartitionGroupMap() {
        return partitionGroupMap;
    }
}
