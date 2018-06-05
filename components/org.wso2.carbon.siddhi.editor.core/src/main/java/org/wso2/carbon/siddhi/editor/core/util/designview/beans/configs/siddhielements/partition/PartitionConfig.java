/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.SiddhiElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryListType;

import java.util.List;
import java.util.Map;

/**
 * Represents Siddhi Partition
 */
public class PartitionConfig extends SiddhiElementConfig {
    private Map<QueryListType, List<QueryConfig>> queryLists;
    private List<String> innerStreamList;
    private List<PartitionWithElement> partitionWith;
    private List<String> annotationList;

    public Map<QueryListType, List<QueryConfig>> getQueryLists() {
        return queryLists;
    }

    public List<String> getInnerStreamList() {
        return innerStreamList;
    }

    public List<PartitionWithElement> getPartitionWith() {
        return partitionWith;
    }

    public List<String> getAnnotationList() {
        return annotationList;
    }

    public void setQueryLists(Map<QueryListType, List<QueryConfig>> queryLists) {
        this.queryLists = queryLists;
    }

    public void setInnerStreamList(List<String> innerStreamList) {
        this.innerStreamList = innerStreamList;
    }

    public void setPartitionWith(List<PartitionWithElement> partitionWith) {
        this.partitionWith = partitionWith;
    }

    public void setAnnotationList(List<String> annotationList) {
        this.annotationList = annotationList;
    }
}
