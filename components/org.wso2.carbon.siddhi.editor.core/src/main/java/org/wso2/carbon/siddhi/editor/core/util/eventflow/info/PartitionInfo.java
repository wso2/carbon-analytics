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

package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

import java.util.ArrayList;
import java.util.List;

public class PartitionInfo extends Info {

    // Variables
    private List<QueryInfo> queries = new ArrayList<>();
    private List<PartitionTypeInfo> partitionTypes = new ArrayList<>();

    // Default Constructor
    public PartitionInfo() {
    }

    // Constructor
    public PartitionInfo(String id, String name, String definition, List<QueryInfo> queries,
                         List<PartitionTypeInfo> partitionTypes) {
        setId(id);
        setName(name);
        setDefinition(definition);
        setQueries(queries);
        setPartitionTypes(partitionTypes);
    }

    // Setters
    public void setQueries(List<QueryInfo> queries) {
        this.queries = queries;
    }

    public void setPartitionTypes(List<PartitionTypeInfo> partitionTypes) {
        this.partitionTypes = partitionTypes;
    }

    // Getters
    public List<QueryInfo> getQueries() {
        return queries;
    }

    public List<PartitionTypeInfo> getPartitionTypes() {
        return partitionTypes;
    }

    // Query List Handlers
    public void addQuery(QueryInfo queryInfo) {
        queries.add(queryInfo);
    }

    // Partition List Handlers
    public void addPartitionType(PartitionTypeInfo partitionTypeInfo) {
        partitionTypes.add(partitionTypeInfo);
    }

}
