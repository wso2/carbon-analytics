/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.stream.processor.core.internal.beans;

import org.wso2.siddhi.query.api.execution.partition.PartitionType;

import java.util.Map;

/**
 * Bean class to hold the elements of siddhi application.
 */
public class SiddhiAppElements {
    private String inputStreamType;
    private String outputStreamType;
    private String inputStreamId;
    private String outputStreamId;
    private String inputStreamSiddhiApp;
    private String outputStreamSiddhiApp;
    private String query;
    private String queryName;
    private String partitionType;
    private String partitionTypeQuery;
    private String partitionQuery;
    private String partitions;
    private String function;
    private String functionQuery;
    private String source;
    private String sourceStream;
    private String sourceSiddhiApp;
    private String sink;
    private String sinkStream;
    private String sinkSiddhiApp;

    public String getPartitions() {
        return partitions;
    }

    public void setPartitions(String partitions) {
        this.partitions = partitions;
    }

    private Map<String, PartitionType> partition;

    public String getInputStreamId() {
        return inputStreamId;
    }

    public void setInputStreamId(String inputStreamId) {
        this.inputStreamId = inputStreamId;
    }

    public String getOutputStreamId() {
        return outputStreamId;
    }

    public void setOutputStreamId(String outputStreamId) {
        this.outputStreamId = outputStreamId;
    }

    public String getInputStreamSiddhiApp() {
        return inputStreamSiddhiApp;
    }

    public void setInputStreamSiddhiApp(String inputStreamSiddhiApp) {
        this.inputStreamSiddhiApp = inputStreamSiddhiApp;
    }

    public String getOutputStreamSiddhiApp() {
        return outputStreamSiddhiApp;
    }

    public void setOutputStreamSiddhiApp(String outputStreamSiddhiApp) {
        this.outputStreamSiddhiApp = outputStreamSiddhiApp;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, PartitionType> getPartition() {
        return partition;
    }

    public void setPartition(Map<String, PartitionType> partition) {
        this.partition = partition;
    }

    public String getPartitionType() {
        return partitionType;
    }

    public void setPartitionType(String partitionType) {
        this.partitionType = partitionType;
    }

    public String getPartitionQuery() {
        return partitionQuery;
    }

    public void setPartitionQuery(String partitionQuery) {
        this.partitionQuery = partitionQuery;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getPartitionTypeQuery() {
        return partitionTypeQuery;
    }

    public void setPartitionTypeQuery(String partitionTypeQuery) {
        this.partitionTypeQuery = partitionTypeQuery;
    }

    public String getInputStreamType() {
        return inputStreamType;
    }

    public void setInputStreamType(String inputStreamType) {
        this.inputStreamType = inputStreamType;
    }

    public String getOutputStreamType() {
        return outputStreamType;
    }

    public void setOutputStreamType(String outputStreamType) {
        this.outputStreamType = outputStreamType;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getFunctionQuery() {
        return functionQuery;
    }

    public void setFunctionQuery(String functionQuery) {
        this.functionQuery = functionQuery;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSink() {
        return sink;
    }

    public void setSink(String sink) {
        this.sink = sink;
    }

    public String getSourceStream() {
        return sourceStream;
    }

    public void setSourceStream(String sourceStream) {
        this.sourceStream = sourceStream;
    }

    public String getSinkStream() {
        return sinkStream;
    }

    public void setSinkStream(String sinkStream) {
        this.sinkStream = sinkStream;
    }

    public String getSourceSiddhiApp() {
        return sourceSiddhiApp;
    }

    public void setSourceSiddhiApp(String sourceSiddhiApp) {
        this.sourceSiddhiApp = sourceSiddhiApp;
    }

    public String getSinkSiddhiApp() {
        return sinkSiddhiApp;
    }

    public void setSinkSiddhiApp(String sinkSiddhiApp) {
        this.sinkSiddhiApp = sinkSiddhiApp;
    }
}
