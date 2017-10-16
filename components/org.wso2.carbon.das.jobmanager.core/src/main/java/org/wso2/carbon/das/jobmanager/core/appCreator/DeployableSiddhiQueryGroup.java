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

package org.wso2.carbon.das.jobmanager.core.appCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO Class to hold created Siddhi Apps belonging to a single query group.
 */
public class DeployableSiddhiQueryGroup {

    private String groupName;
    private List<String> queryList;

    public DeployableSiddhiQueryGroup(String groupName) {
        this.groupName = groupName;
        queryList = new ArrayList<>();
    }

    private DeployableSiddhiQueryGroup() {
        //Avoiding empty initialization
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getQueryList() {
        return queryList;
    }

    public void addQuery(String query) {
        queryList.add(query);
    }

    public void setQueryList(List<String> queryList) {
        this.queryList = queryList;
    }
}
