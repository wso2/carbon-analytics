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

package io.siddhi.distribution.editor.core.core.util.designview.beans.configs.siddhielements.partition;

import io.siddhi.distribution.editor.core.core.util.designview.beans.configs.siddhielements.SiddhiElementConfig;
import io.siddhi.distribution.editor.core.core.util.designview.beans.configs.siddhielements.StreamConfig;
import io.siddhi.distribution.editor.core.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import io.siddhi.distribution.editor.core.core.util.designview.constants.query.QueryListType;
import org.wso2.siddhi.query.api.annotation.Annotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents Siddhi Partition
 */
public class PartitionConfig extends SiddhiElementConfig {
    private Map<QueryListType, List<QueryConfig>> queryLists;
    private List<StreamConfig> streamList;
    private List<PartitionWithElement> partitionWith;
    private List<String> annotationList;
    private List<Annotation> annotationListObjects;
    private Map<String, String> connectorsAndStreams = new HashMap<>();

    public Map<QueryListType, List<QueryConfig>> getQueryLists() {
        return queryLists;
    }

    public void setQueryLists(Map<QueryListType, List<QueryConfig>> queryLists) {
        this.queryLists = queryLists;
    }

    public List<StreamConfig> getStreamList() {
        return streamList;
    }

    public void setStreamList(List<StreamConfig> streamList) {
        this.streamList = streamList;
    }

    public List<PartitionWithElement> getPartitionWith() {
        return partitionWith;
    }

    public void setPartitionWith(List<PartitionWithElement> partitionWith) {
        this.partitionWith = partitionWith;
    }

    public List<String> getAnnotationList() {
        return annotationList;
    }

    public void setAnnotationList(List<String> annotationList) {
        this.annotationList = annotationList;
    }

    public List<Annotation> getAnnotationListObjects() {
        return annotationListObjects;
    }

    public void setAnnotationListObjects(List<Annotation> annotationListObjects) {
        this.annotationListObjects = annotationListObjects;
    }

    public Map<String, String> getConnectorsAndStreams() {
        return connectorsAndStreams;
    }

    public void setConnectorsAndStreams(Map<String, String> connectorsAndStreams) {
        this.connectorsAndStreams = connectorsAndStreams;
    }

}
