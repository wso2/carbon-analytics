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

package org.wso2.carbon.sp.jobmanager.core.appcreator;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO Class to hold created Siddhi Apps belonging to a single query group.
 */
public class DeployableSiddhiQueryGroup {

    private String groupName;
    private List<SiddhiQuery> siddhiQueries;
    private boolean isReceiverQueryGroup;

    public DeployableSiddhiQueryGroup(String groupName) {
        this.groupName = groupName;
        siddhiQueries = new ArrayList<>();
        isReceiverQueryGroup = false;
    }

    public List<SiddhiQuery> getSiddhiQueries() {
        return siddhiQueries;
    }

    public void setSiddhiQueries(List<SiddhiQuery> siddhiQueries) {
        this.siddhiQueries = siddhiQueries;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isReceiverQueryGroup() {
        return isReceiverQueryGroup;
    }

    public void setReceiverQueryGroup(boolean receiverQueryGroup) {
        isReceiverQueryGroup = receiverQueryGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeployableSiddhiQueryGroup that = (DeployableSiddhiQueryGroup) o;
        if (getGroupName() != null ? !getGroupName().equals(that.getGroupName()) : that.getGroupName() != null) {
            return false;
        }
        return getSiddhiQueries() != null ? getSiddhiQueries().equals(that.getSiddhiQueries())
                : that.getSiddhiQueries() == null;
    }

    @Override
    public int hashCode() {
        int result = getGroupName() != null ? getGroupName().hashCode() : 0;
        result = 31 * result + (getSiddhiQueries() != null ? getSiddhiQueries().hashCode() : 0);
        return result;
    }
}
